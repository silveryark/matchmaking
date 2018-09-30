package com.silveryark.matchmaking.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class Ranking {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column
    private String game;

    @Column
    private String uid;

    @Column
    private int rank;

    protected Ranking(){}

    public Ranking(String game, String uid, int rank) {
        this.game = game;
        this.uid = uid;
        this.rank = rank;
    }

    public String getId() {
        return id;
    }

    public String getGame() {
        return game;
    }

    public String getUid() {
        return uid;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
