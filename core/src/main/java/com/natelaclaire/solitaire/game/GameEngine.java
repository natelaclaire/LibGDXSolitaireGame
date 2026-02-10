package com.natelaclaire.solitaire.game;

import com.badlogic.gdx.utils.Array;

public class GameEngine {
    private GameState state;
    private final Array<GameState> undoStack = new Array<>();
    private int drawCount = 3;

    public GameEngine() {
        newGame();
    }

    public void newGame() {
        state = GameState.newGame();
        undoStack.clear();
    }

    public GameState getState() {
        return state;
    }

    public int getDrawCount() {
        return drawCount;
    }

    public void setDrawCount(int drawCount) {
        this.drawCount = drawCount;
    }

    public boolean isWin() {
        return state.winState;
    }

    public int getScore() {
        return state.score;
    }

    public void drawFromStock() {
        if (state.stock.cards.size > 0) {
            pushUndoState();
            for (int i = 0; i < drawCount && state.stock.cards.size > 0; i++) {
                Card card = state.stock.cards.pop();
                card.faceUp = true;
                state.waste.cards.add(card);
            }
            return;
        }
        if (state.waste.cards.size > 0) {
            pushUndoState();
            while (state.waste.cards.size > 0) {
                Card card = state.waste.cards.pop();
                card.faceUp = false;
                state.stock.cards.add(card);
            }
            addScore(-100);
        }
    }

    public boolean flipTopIfNeeded(Pile pile, int index) {
        if (pile == null || pile.type != PileType.TABLEAU) {
            return false;
        }
        if (index != pile.cards.size - 1) {
            return false;
        }
        Card card = pile.cards.get(index);
        if (card.faceUp) {
            return false;
        }
        pushUndoState();
        card.faceUp = true;
        addScore(5);
        return true;
    }

    public boolean tryMove(Pile from, int startIndex, Pile to) {
        if (from == null || to == null) {
            return false;
        }
        if (startIndex < 0 || startIndex >= from.cards.size) {
            return false;
        }
        Array<Card> moving = new Array<>();
        for (int i = startIndex; i < from.cards.size; i++) {
            moving.add(from.cards.get(i));
        }
        if (moving.size == 0) {
            return false;
        }

        if (to.type == PileType.FOUNDATION) {
            if (moving.size != 1) {
                return false;
            }
            Card card = moving.first();
            if (!GameRules.canPlaceOnFoundation(to, card)) {
                return false;
            }
            pushUndoState();
            removeFromPile(from, startIndex);
            to.cards.add(card);
            applyMoveScore(from.type, to.type);
            checkWinState();
            return true;
        }

        if (to.type == PileType.TABLEAU) {
            if (!GameRules.canPlaceOnTableau(to, moving.first())) {
                return false;
            }
            pushUndoState();
            removeFromPile(from, startIndex);
            for (Card card : moving) {
                to.cards.add(card);
            }
            applyMoveScore(from.type, to.type);
            checkWinState();
            return true;
        }

        return false;
    }

    public boolean revealTopAfterMove(Pile pile) {
        if (pile == null || pile.type != PileType.TABLEAU) {
            return false;
        }
        if (pile.cards.size == 0) {
            return false;
        }
        Card top = pile.cards.peek();
        if (top.faceUp) {
            return false;
        }
        top.faceUp = true;
        addScore(5);
        return true;
    }

    public void undoLast() {
        if (undoStack.size == 0) {
            return;
        }
        state = undoStack.pop();
    }

    private void pushUndoState() {
        undoStack.add(state.copy());
    }

    private void removeFromPile(Pile pile, int startIndex) {
        for (int i = pile.cards.size - 1; i >= startIndex; i--) {
            pile.cards.removeIndex(i);
        }
    }

    private void applyMoveScore(PileType from, PileType to) {
        if (to == PileType.FOUNDATION) {
            addScore(10);
            return;
        }
        if (from == PileType.WASTE && to == PileType.TABLEAU) {
            addScore(5);
            return;
        }
        if (from == PileType.FOUNDATION && to == PileType.TABLEAU) {
            addScore(-15);
        }
    }

    private void addScore(int delta) {
        state.score = Math.max(0, state.score + delta);
    }

    private void checkWinState() {
        int count = 0;
        for (Pile foundation : state.foundations) {
            count += foundation.cards.size;
        }
        state.winState = count == 52;
    }
}
