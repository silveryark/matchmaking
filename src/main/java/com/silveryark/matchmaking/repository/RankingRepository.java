package com.silveryark.matchmaking.repository;

import com.silveryark.matchmaking.model.Ranking;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface RankingRepository extends ReactiveSortingRepository<Ranking, String> {

    Mono<Ranking> findFirstByUidAndGame(String uid, String game);

    Flux<Ranking> findAllByUidAndGame(Collection<String> uids, String game);

}
