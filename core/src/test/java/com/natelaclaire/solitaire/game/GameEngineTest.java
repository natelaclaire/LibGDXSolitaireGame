package com.natelaclaire.solitaire.game;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameEngineTest {
    @Test
    public void drawCountThreeDrawsThreeCards() {
        GameEngine engine = new GameEngine();
        GameState state = engine.getState();
        int stockBefore = state.stock.cards.size;
        int wasteBefore = state.waste.cards.size;

        engine.setDrawCount(3);
        engine.drawFromStock();

        assertEquals(stockBefore - 3, state.stock.cards.size);
        assertEquals(wasteBefore + 3, state.waste.cards.size);
    }

    @Test
    public void drawCountOneDrawsOneCard() {
        GameEngine engine = new GameEngine();
        GameState state = engine.getState();
        int stockBefore = state.stock.cards.size;
        int wasteBefore = state.waste.cards.size;

        engine.setDrawCount(1);
        engine.drawFromStock();

        assertEquals(stockBefore - 1, state.stock.cards.size);
        assertEquals(wasteBefore + 1, state.waste.cards.size);
    }

    @Test
    public void moveToFoundationScoresTen() {
        GameEngine engine = new GameEngine();
        GameState state = engine.getState();
        state.stock.cards.clear();
        state.waste.cards.clear();
        state.foundations.get(0).cards.clear();

        Card ace = new Card(Suit.CLUBS, 1);
        ace.faceUp = true;
        state.waste.cards.add(ace);

        boolean moved = engine.tryMove(state.waste, 0, state.foundations.get(0));
        assertTrue(moved);
        assertEquals(10, engine.getScore());
    }

    @Test
    public void flipTopTableauAddsScore() {
        GameEngine engine = new GameEngine();
        GameState state = engine.getState();
        Pile pile = state.tableau.get(0);
        pile.cards.clear();
        Card card = new Card(Suit.HEARTS, 5);
        card.faceUp = false;
        pile.cards.add(card);

        boolean flipped = engine.flipTopIfNeeded(pile, 0);
        assertTrue(flipped);
        assertTrue(pile.cards.peek().faceUp);
        assertEquals(5, engine.getScore());
    }

    @Test
    public void undoRestoresPreviousState() {
        GameEngine engine = new GameEngine();
        GameState state = engine.getState();
        int stockBefore = state.stock.cards.size;

        engine.drawFromStock();
        assertEquals(stockBefore - engine.getDrawCount(), state.stock.cards.size);

        engine.undoLast();
        assertEquals(stockBefore, engine.getState().stock.cards.size);
    }
}
