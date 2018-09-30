package com.silveryark.matchmaking;

import com.silveryark.matchmaking.model.Ranking;
import com.silveryark.matchmaking.service.RankingService;
import com.silveryark.rpc.GenericResponse;
import com.silveryark.rpc.RPCRequest;
import com.silveryark.rpc.RPCResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

@RestController("/ranking")
public class RankingController {

    private final RankingService rankingService;

    @Autowired
    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @PostMapping("/{game}")
    public Mono<GenericResponse> updateRank(@PathVariable("game")String game, @RequestBody RPCRequest<Map<String,
                Integer>> request){

        Mono<Map<String, Ranking>> updatedRanking = rankingService.updateRanking(game,
                request.getPayload().entrySet()
                        .stream()
                        .collect(Collectors
                                .toMap(Map.Entry::getKey, entry -> MatchResult.parseFrom(entry.getValue()))));
        return updatedRanking
                .thenReturn(new GenericResponse(request.getRequestId(), RPCResponse.STATUS.OK, Boolean.TRUE))
                .onErrorReturn(new GenericResponse(request.getRequestId(), RPCResponse.STATUS.SERVER_ERROR,
                        Boolean.FALSE));
    }
}
