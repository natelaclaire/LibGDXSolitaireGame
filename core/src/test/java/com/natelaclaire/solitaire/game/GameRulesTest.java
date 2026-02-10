package com.natelaclaire.solitaire.game;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameRulesTest {
    @Test
    public void foundationAcceptsAce() {
        Pile foundation = new Pile(PileType.FOUNDATION);
        Card ace = new Card(Suit.SPADES, 1);
        assertTrue(GameRules.canPlaceOnFoundation(foundation, ace));
    }

    @Test
    public void foundationRequiresSameSuitAscending() {
        Pile foundation = new Pile(PileType.FOUNDATION);
        foundation.cards.add(new Card(Suit.HEARTS, 5));
        assertTrue(GameRules.canPlaceOnFoundation(foundation, new Card(Suit.HEARTS, 6)));
        assertFalse(GameRules.canPlaceOnFoundation(foundation, new Card(Suit.HEARTS, 7)));
        assertFalse(GameRules.canPlaceOnFoundation(foundation, new Card(Suit.SPADES, 6)));
    }

    @Test
    public void tableauAcceptsKingOnEmpty() {
        Pile tableau = new Pile(PileType.TABLEAU);
        assertTrue(GameRules.canPlaceOnTableau(tableau, new Card(Suit.CLUBS, 13)));
        assertFalse(GameRules.canPlaceOnTableau(tableau, new Card(Suit.CLUBS, 12)));
    }

    @Test
    public void tableauRequiresAlternatingColorDescending() {
        Pile tableau = new Pile(PileType.TABLEAU);
        Card top = new Card(Suit.HEARTS, 9);
        top.faceUp = true;
        tableau.cards.add(top);
        assertTrue(GameRules.canPlaceOnTableau(tableau, new Card(Suit.SPADES, 8)));
        assertFalse(GameRules.canPlaceOnTableau(tableau, new Card(Suit.DIAMONDS, 8)));
        assertFalse(GameRules.canPlaceOnTableau(tableau, new Card(Suit.SPADES, 7)));
    }
}
