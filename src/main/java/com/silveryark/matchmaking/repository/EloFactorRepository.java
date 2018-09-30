package com.silveryark.matchmaking.repository;

import com.silveryark.matchmaking.model.EloFactor;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;

public interface EloFactorRepository extends ReactiveSortingRepository<EloFactor, String> {

    Flux<EloFactor> findAllByGame(String game);

}
