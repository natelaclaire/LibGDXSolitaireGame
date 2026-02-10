package com.natelaclaire.solitaire.game;

public class Card {
    public final Suit suit;
    public final int rank;
    public boolean faceUp;

    public Card(Suit suit, int rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public boolean isRed() {
        return suit == Suit.HEARTS || suit == Suit.DIAMONDS;
    }

    public String assetKey(String prefix) {
        String suitName;
        switch (suit) {
            case CLUBS:
                suitName = "simplecard".equals(prefix) ? "club" : "clubs";
                break;
            case DIAMONDS:
                suitName = "diamond";
                break;
            case HEARTS:
                suitName = "heart";
                break;
            case SPADES:
                suitName = "spade";
                break;
            default:
                suitName = "spade";
                break;
        }

        return prefix + "_" + suitName + "_" + rank;
    }
}
