package com.silveryark.matchmaking.service;

import com.silveryark.matchmaking.MatchResult;
import com.silveryark.matchmaking.model.EloFactor;
import com.silveryark.matchmaking.model.Ranking;
import com.silveryark.matchmaking.repository.EloFactorRepository;
import com.silveryark.matchmaking.repository.RankingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;

@Service
public class RankingService {

    //保底成绩
    private static final int MINIMAL_SCORE = 100;

    private static final double BASELINE_E = 0.014;

    private final RankingRepository rankingRepository;

    private final EloFactorRepository eloFactorRepository;

    @Autowired
    public RankingService(RankingRepository rankingRepository, EloFactorRepository eloFactorRepository) {
        this.rankingRepository = rankingRepository;
        this.eloFactorRepository = eloFactorRepository;
    }

    //elo 算法，参考：http://hkga.org.hk/rating.backup/intro.php
    public Mono<Map<String, Ranking>> updateRanking(String game, Map<String, MatchResult> matchResult) {
        ArrayList<String> ids = new ArrayList<>(matchResult.keySet());
        String player1 = ids.get(0);
        String player2 = ids.get(1);
        Flux<EloFactor> factorFlux = eloFactorRepository.findAllByGame(game);
        Flux<Ranking> rankingFlux = rankingRepository.findAllByUidAndGame(matchResult.keySet(), game);
        return null;
//        return factorFlux
//                .collectMap(EloFactor::getGor, eloFactor -> eloFactor, TreeMap::new)
//                .cast(SortedMap.class)
//                .flatMap((Function<SortedMap<Integer, EloFactor>, Mono<Map<String, Ranking>>>) factorMap -> rankingFlux
//                        .collectMap((Ranking::getUid))
//                        .flatMap((Map<String, Ranking> uidRankingMap) -> {
//                            Ranking ranking1 = uidRankingMap.get(player1);
//                            Ranking ranking2 = uidRankingMap.get(player2);
//                            int d = ranking2.getRank() - ranking1.getRank();
//                            EloFactor factor1 = forScore(factorMap, ranking1.getRank());
//                            EloFactor factor2 = forScore(factorMap, ranking2.getRank());
//                            //双方获胜期望
//                            double E1 = 1 / (Math.exp((float)d / factor1.getA()) + 1);
//                            double E2 = 1 - BASELINE_E - E1;
//                            //双方赛果
//                            double SA1 = getSa(matchResult.get(player1));
//                            double SA2 = getSa(matchResult.get(player2));
//                            //双方con
//                            double con1 = factor1.getCon();
//                            double con2 = factor2.getCon();
//                            //双方得分
//                            int score1 = (int) (con1 * (SA1 - E1));
//                            int score2 = (int) (con2 * (SA2 - E2));
//                            //更新得分
//                            ranking1.setRank(ranking1.getRank() + score1 < MINIMAL_SCORE? 100:
//                                     ranking1.getRank() + score1);
//                            ranking2.setRank(ranking2.getRank() + score2 < MINIMAL_SCORE? 100:
//                                     ranking2.getRank() + score2);
//                            return rankingRepository
//                                    .saveAll(Arrays.asList(ranking1, ranking2))
//                                    .collectMap(Ranking::getUid);
//                        }));
    }

    private double getSa(MatchResult result) {
        switch (result) {
            case WIN:
                return 1;
            case DRAW:
                return 0.5;
            case LOSE:
                return 0;
            default:
                return 0.5;
        }
    }

    private EloFactor forScore(SortedMap<Integer, EloFactor> factorMap, int rank) {
        Integer factorKey = factorMap.headMap(rank).firstKey();
        return factorMap.get(factorKey);
    }
}
