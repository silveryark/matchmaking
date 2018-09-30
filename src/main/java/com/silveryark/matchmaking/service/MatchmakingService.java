package com.silveryark.matchmaking.service;

import com.silveryark.matchmaking.MatchableGame;
import com.silveryark.matchmaking.ScoreCalculator;
import com.silveryark.matchmaking.model.Ranking;
import com.silveryark.matchmaking.repository.RankingRepository;
import com.silveryark.rpc.Brokers;
import com.silveryark.rpc.GenericResponse;
import com.silveryark.rpc.gateway.OutboundMessage;
import com.silveryark.rpc.matching.GameMatching;
import com.silveryark.utils.Dates;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.EdmondsMaximumCardinalityMatching;
import org.jgrapht.alg.matching.GreedyWeightedMatching;
import org.jgrapht.graph.AsWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;


@Service
public class MatchmakingService {

    private static final int MAX_WIDTH = 400;

    private static final String MATCHING_PREFIX = "matchmaking_";
    private final Brokers brokers;
    private final ScoreCalculator calculator;
    private final ReactiveRedisOperations<String, String> redisOps;
    private final RankingRepository rankingRepository;
    private final Dates dates;

    @Autowired
    public MatchmakingService(Brokers brokers, ScoreCalculator calculator,
                              ReactiveRedisOperations<String, String> redisOps,
                              RankingRepository rankingRepository,
                              Dates dates) {
        this.brokers = brokers;
        this.calculator = calculator;
        this.redisOps = redisOps;
        this.rankingRepository = rankingRepository;
        this.dates = dates;
    }

    public Mono<Boolean> joinMatching(String uid, MatchableGame game) {
        ReactiveHashOperations<String, String, Long> hashOperations = redisOps.opsForHash();
        return hashOperations.put(MATCHING_PREFIX + game.name(), uid, dates.currentMillis());
    }

    public Mono<Long> quitMatching(String uid, MatchableGame game) {
        ReactiveHashOperations<String, Object, Object> hashOperations = redisOps.opsForHash();
        return hashOperations.remove(MATCHING_PREFIX + game.name(), uid);
    }

    //每1秒一次匹配
    @Scheduled(fixedRate = 1000l)
    private void matchMaking() {
        ReactiveHashOperations<String, String, Long> hashOperations = redisOps.opsForHash();
        Flux flux = Flux.empty();
        for (MatchableGame game : MatchableGame.values()) {
            Flux<Map.Entry<String, Long>> playerPool = hashOperations.entries(MATCHING_PREFIX + game.name());
            flux = Flux.concat(flux, playerPool.collectList()
                    //entries 数据的key是 uid， Value是加入队列的时间
                    .flatMapMany((List<Map.Entry<String, Long>> entries) -> {
                        List<String> uids = new ArrayList<>();
                        for (Map.Entry<String, Long> entry : entries) {
                            uids.add(entry.getKey());
                        }
                        Flux<Ranking> rankingFlux = rankingRepository.findAllByUidAndGame(uids, game.name());
                        return rankingFlux.collectMap(Ranking::getUid)
                                .flatMapMany((Map<String, Ranking> uidRankingMap) -> {
                                    Graph<String, DefaultWeightedEdge> weightedGraph = buildGraph(entries,
                                            uidRankingMap);
                                    MatchingAlgorithm.Matching<String, DefaultWeightedEdge> matching =
                                            matching(weightedGraph);
                                    Set<DefaultWeightedEdge> edges = matching.getEdges();
                                    return sendTo(weightedGraph, edges, new GameMatching(game.name(),
                                            UUID.randomUUID().toString()));
                                });
                    }));
        }
        flux.blockLast();
    }

    /**
     * @param entries       Key是uid, value是用户开始加入队列的时间
     * @param uidRankingMap Key是uid, value是Ranking
     * @return
     */
    private Graph<String, DefaultWeightedEdge> buildGraph(List<Map.Entry<String, Long>> entries,
                                                          Map<String, Ranking> uidRankingMap) {
        Graph<String, DefaultWeightedEdge> graph =
                new SimpleGraph<>(DefaultWeightedEdge.class);
        Map<DefaultWeightedEdge, Double> weightedMap = new HashMap<>();
        for (Map.Entry<String, Long> entry : entries) {
            graph.addVertex(entry.getKey());
        }
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, Long> entry1 = entries.get(i);
            for (int j = i + 1; j < entries.size(); j++) {
                Map.Entry<String, Long> entry2 = entries.get(j);
                int score =
                        calculator.matchScore(new ScoreCalculator.Point(uidRankingMap.get(entry1.getKey()).getRank()
                                        , (int) (dates.currentMillis() - entry1.getValue())),
                                new ScoreCalculator.Point(uidRankingMap.get(entry2.getKey()).getRank(),
                                        (int) (dates.currentMillis() - entry2.getValue())));
                if (score > 0) {
                    weightedMap.put(graph.addEdge(entry1.getKey(), entry2.getKey()),
                            (double) score);
                }
            }
        }
        return new AsWeightedGraph<>(graph, weightedMap);
    }

    private MatchingAlgorithm.Matching<String, DefaultWeightedEdge> matching(Graph<String, DefaultWeightedEdge> g) {
        EdmondsMaximumCardinalityMatching<String, DefaultWeightedEdge> weightedMatching =
                new EdmondsMaximumCardinalityMatching<>(g,
                        new GreedyWeightedMatching<>(g, true));
        return weightedMatching.getMatching();
    }

    private Flux<GenericResponse> sendTo(Graph<String, DefaultWeightedEdge> weightedGraph,
                                         Collection<DefaultWeightedEdge> matchedPlayer, GameMatching aMatch) {
        return Flux.just(matchedPlayer.parallelStream().map((DefaultWeightedEdge edge) -> {
            String source = weightedGraph.getEdgeSource(edge);
            String target = weightedGraph.getEdgeTarget(edge);
            Mono<GenericResponse> matchingSource =
                    brokers.create().body(new OutboundMessage<GameMatching>("matching", source, aMatch
                    )).retrieve(null);
            Mono<GenericResponse> matchingTarget =
                    brokers.create().body(new OutboundMessage<GameMatching>("matching", target,
                            aMatch)).retrieve(null);
            return Flux.concat(matchingSource, matchingTarget);
        }).toArray(GenericResponse[]::new));
    }
}
