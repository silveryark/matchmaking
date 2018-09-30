package com.silveryark.matchmaking.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class EloFactor {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column
    private String game;

    @Column
    private int gor;

    @Column
    private int con;

    @Column
    private int a;

    protected EloFactor() {
    }

    public EloFactor(String game, int gor, int con, int a) {
        this.game = game;
        this.gor = gor;
        this.con = con;
        this.a = a;
    }

    public String getId() {
        return id;
    }

    public String getGame() {
        return game;
    }

    public int getGor() {
        return gor;
    }

    public int getCon() {
        return con;
    }

    public int getA() {
        return a;
    }
}
