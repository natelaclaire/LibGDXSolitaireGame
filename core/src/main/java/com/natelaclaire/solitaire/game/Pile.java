package com.natelaclaire.solitaire.game;

import com.badlogic.gdx.utils.Array;

public class Pile {
    public final PileType type;
    public final Array<Card> cards = new Array<>();
    public float x;
    public float y;

    public Pile(PileType type) {
        this.type = type;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
