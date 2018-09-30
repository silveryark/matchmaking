package com.silveryark.matchmaking;

import com.silveryark.matchmaking.service.MatchmakingService;
import com.silveryark.rpc.*;
import com.silveryark.rpc.quartz.QuartzRequest;
import com.silveryark.utils.Dates;
import com.silveryark.utils.Services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController("/matchmaking")
public class MatchmakingController {


    private final MatchmakingService matchmakingService;

    @Autowired
    public MatchmakingController(MatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

    @PostMapping("/{game}")
    public Mono<GenericResponse> matchMaking(Authentication user, @PathVariable("game") String game,
                                             @RequestHeader(RPCHttpHeaders.REQUEST_ID) String requestId){
        return matchmakingService.joinMatching(user.getName(), MatchableGame.valueOf(game))
                .map(aBoolean -> new GenericResponse(requestId, RPCResponse.STATUS.OK, aBoolean));

    }

    @DeleteMapping("/{game}")
    public Mono<GenericResponse> quitMatching(Authentication user, @PathVariable("game") String game,
                                              @RequestHeader(RPCHttpHeaders.REQUEST_ID) String requestId){
        return matchmakingService.quitMatching(user.getName(), MatchableGame.valueOf(game))
                .map(aLong -> new GenericResponse(requestId, RPCResponse.STATUS.OK, aLong > 0));
    }
}
