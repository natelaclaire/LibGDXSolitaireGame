package com.natelaclaire.solitaire.game;

import com.badlogic.gdx.utils.Array;

public class GameState {
    public Pile stock;
    public Pile waste;
    public Array<Pile> foundations;
    public Array<Pile> tableau;
    public int score;
    public boolean winState;

    public static GameState newGame() {
        GameState state = new GameState();
        state.stock = new Pile(PileType.STOCK);
        state.waste = new Pile(PileType.WASTE);
        state.foundations = new Array<>();
        state.tableau = new Array<>();
        for (int i = 0; i < 4; i++) {
            state.foundations.add(new Pile(PileType.FOUNDATION));
        }
        for (int i = 0; i < 7; i++) {
            state.tableau.add(new Pile(PileType.TABLEAU));
        }

        Array<Card> deck = createDeck();
        deck.shuffle();

        for (int i = 0; i < 7; i++) {
            Pile pile = state.tableau.get(i);
            for (int j = 0; j <= i; j++) {
                Card card = deck.pop();
                card.faceUp = j == i;
                pile.cards.add(card);
            }
        }

        while (deck.size > 0) {
            Card card = deck.pop();
            card.faceUp = false;
            state.stock.cards.add(card);
        }

        state.score = 0;
        state.winState = false;
        return state;
    }

    public static Array<Card> createDeck() {
        Array<Card> deck = new Array<>(52);
        for (Suit suit : Suit.values()) {
            for (int rank = 1; rank <= 13; rank++) {
                deck.add(new Card(suit, rank));
            }
        }
        return deck;
    }

    public GameState copy() {
        GameState state = new GameState();
        state.score = score;
        state.winState = winState;
        state.stock = copyPile(stock);
        state.waste = copyPile(waste);
        state.foundations = new Array<>();
        for (Pile pile : foundations) {
            state.foundations.add(copyPile(pile));
        }
        state.tableau = new Array<>();
        for (Pile pile : tableau) {
            state.tableau.add(copyPile(pile));
        }
        return state;
    }

    private Pile copyPile(Pile source) {
        Pile pile = new Pile(source.type);
        pile.x = source.x;
        pile.y = source.y;
        for (Card card : source.cards) {
            Card copy = new Card(card.suit, card.rank);
            copy.faceUp = card.faceUp;
            pile.cards.add(copy);
        }
        return pile;
    }
}
