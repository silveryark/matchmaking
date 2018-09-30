package com.silveryark.matchmaking;

public enum MatchResult {
    WIN, DRAW, LOSE;
    public static MatchResult parseFrom(Integer result){
        if(result > 0){
            return WIN;
        }else if (result == 0){
            return DRAW;
        }else {
            return LOSE;
        }
    }
}
