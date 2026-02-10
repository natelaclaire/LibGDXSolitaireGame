package com.natelaclaire.solitaire.game;

public final class GameRules {
    private GameRules() {}

    public static boolean canPlaceOnFoundation(Pile foundation, Card card) {
        if (foundation.cards.size == 0) {
            return card.rank == 1;
        }
        Card top = foundation.cards.peek();
        return top.suit == card.suit && card.rank == top.rank + 1;
    }

    public static boolean canPlaceOnTableau(Pile pile, Card card) {
        if (pile.cards.size == 0) {
            return card.rank == 13;
        }
        Card top = pile.cards.peek();
        return top.faceUp && top.isRed() != card.isRed() && card.rank == top.rank - 1;
    }
}
