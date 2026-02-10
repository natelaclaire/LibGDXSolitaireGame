package com.natelaclaire.solitaire;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class SolitaireGame extends ApplicationAdapter {
    private static final Color TABLE_COLOR = new Color(0.10f, 0.45f, 0.18f, 1f);
    private static final Color CARD_FACE = new Color(0.97f, 0.97f, 0.94f, 1f);
    private static final Color CARD_BACK = new Color(0.16f, 0.32f, 0.62f, 1f);
    private static final Color OUTLINE_COLOR = new Color(0f, 0f, 0f, 0.45f);

    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout layout;
    private OrthographicCamera camera;
    private ScreenViewport viewport;
    private Texture whiteTex;
    private Texture backTex;

    private float worldWidth;
    private float worldHeight;
    private float cardWidth;
    private float cardHeight;
    private float tableauSpacingFaceDown;
    private float tableauSpacingFaceUp;
    private float gutter;

    private Pile stock;
    private Pile waste;
    private Array<Pile> foundations;
    private Array<Pile> tableau;

    private Pile selectedPile;
    private int selectedIndex = -1;
    private final Vector2 tmp = new Vector2();

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        layout = new GlyphLayout();

        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.apply(true);

        whiteTex = createSolidTexture(Color.WHITE);
        backTex = createSolidTexture(CARD_BACK);

        setupGame();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                return handleTouch(screenX, screenY);
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        updateLayout();
    }

    @Override
    public void render() {
        ScreenUtils.clear(TABLE_COLOR);
        if (worldWidth == 0f || worldHeight == 0f) {
            updateLayout();
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawPiles();
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        whiteTex.dispose();
        backTex.dispose();
    }

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void setupGame() {
        stock = new Pile(PileType.STOCK);
        waste = new Pile(PileType.WASTE);
        foundations = new Array<>();
        tableau = new Array<>();
        for (int i = 0; i < 4; i++) {
            foundations.add(new Pile(PileType.FOUNDATION));
        }
        for (int i = 0; i < 7; i++) {
            tableau.add(new Pile(PileType.TABLEAU));
        }

        Array<Card> deck = createDeck();
        deck.shuffle();

        for (int i = 0; i < 7; i++) {
            Pile pile = tableau.get(i);
            for (int j = 0; j <= i; j++) {
                Card card = deck.pop();
                card.faceUp = j == i;
                pile.cards.add(card);
            }
        }

        while (deck.size > 0) {
            Card card = deck.pop();
            card.faceUp = false;
            stock.cards.add(card);
        }
    }

    private Array<Card> createDeck() {
        Array<Card> deck = new Array<>(52);
        for (Suit suit : Suit.values()) {
            for (int rank = 1; rank <= 13; rank++) {
                deck.add(new Card(suit, rank));
            }
        }
        return deck;
    }

    private void updateLayout() {
        worldWidth = viewport.getWorldWidth();
        worldHeight = viewport.getWorldHeight();

        gutter = Math.max(12f, Math.min(worldWidth, worldHeight) * 0.025f);
        float maxCardWidth = (worldWidth - gutter * 8f) / 7f;
        float maxCardHeight = (worldHeight - gutter * 6f) / 3f;
        cardWidth = Math.min(maxCardWidth, maxCardHeight * 0.72f);
        cardHeight = cardWidth / 0.72f;

        tableauSpacingFaceDown = cardHeight * 0.18f;
        tableauSpacingFaceUp = cardHeight * 0.28f;

        float topRowY = worldHeight - gutter - cardHeight;
        stock.setPosition(gutter, topRowY);
        waste.setPosition(gutter * 2f + cardWidth, topRowY);
        for (int i = 0; i < foundations.size; i++) {
            float x = worldWidth - gutter - cardWidth - i * (cardWidth + gutter);
            foundations.get(i).setPosition(x, topRowY);
        }

        float tableauY = topRowY - gutter - cardHeight;
        for (int i = 0; i < tableau.size; i++) {
            float x = gutter + i * (cardWidth + gutter);
            tableau.get(i).setPosition(x, tableauY);
        }

        float fontScale = Math.max(0.5f, cardHeight / 220f);
        font.getData().setScale(fontScale);
    }

    private void drawPiles() {
        drawPile(stock);
        drawPile(waste);
        for (Pile foundation : foundations) {
            drawPile(foundation);
        }
        for (Pile pile : tableau) {
            drawPile(pile);
        }

        if (selectedPile != null && selectedIndex >= 0) {
            drawSelection();
        }
    }

    private void drawPile(Pile pile) {
        if (pile.type == PileType.TABLEAU) {
            drawTableauPile(pile);
        } else {
            drawStackedPile(pile);
        }
    }

    private void drawStackedPile(Pile pile) {
        if (pile.cards.size == 0) {
            drawOutline(pile.x, pile.y, cardWidth, cardHeight);
            return;
        }
        Card top = pile.cards.peek();
        drawCard(pile.x, pile.y, top);
    }

    private void drawTableauPile(Pile pile) {
        if (pile.cards.size == 0) {
            drawOutline(pile.x, pile.y, cardWidth, cardHeight);
            return;
        }

        float y = pile.y;
        for (int i = 0; i < pile.cards.size; i++) {
            Card card = pile.cards.get(i);
            drawCard(pile.x, y, card);
            y -= card.faceUp ? tableauSpacingFaceUp : tableauSpacingFaceDown;
        }
    }

    private void drawCard(float x, float y, Card card) {
        Texture tex = card.faceUp ? whiteTex : backTex;
        Color color = card.faceUp ? CARD_FACE : CARD_BACK;
        batch.setColor(color);
        batch.draw(tex, x, y, cardWidth, cardHeight);
        batch.setColor(Color.WHITE);
        drawOutline(x, y, cardWidth, cardHeight);

        if (card.faceUp) {
            font.setColor(card.isRed() ? Color.FIREBRICK : Color.BLACK);
            String label = card.label();
            layout.setText(font, label);
            float textX = x + cardWidth * 0.08f;
            float textY = y + cardHeight * 0.92f;
            font.draw(batch, layout, textX, textY);
            font.setColor(Color.WHITE);
        }
    }

    private void drawOutline(float x, float y, float width, float height) {
        batch.setColor(OUTLINE_COLOR);
        batch.draw(whiteTex, x, y, width, 2f);
        batch.draw(whiteTex, x, y + height - 2f, width, 2f);
        batch.draw(whiteTex, x, y, 2f, height);
        batch.draw(whiteTex, x + width - 2f, y, 2f, height);
        batch.setColor(Color.WHITE);
    }

    private void drawSelection() {
        if (selectedPile == null) {
            return;
        }
        float x = selectedPile.x;
        float y = selectedPile.y;
        if (selectedPile.type == PileType.TABLEAU) {
            for (int i = 0; i < selectedIndex; i++) {
                Card card = selectedPile.cards.get(i);
                y -= card.faceUp ? tableauSpacingFaceUp : tableauSpacingFaceDown;
            }
        }
        float height = cardHeight;
        if (selectedPile.type == PileType.TABLEAU) {
            for (int i = selectedIndex + 1; i < selectedPile.cards.size; i++) {
                height += selectedPile.cards.get(i).faceUp ? tableauSpacingFaceUp : tableauSpacingFaceDown;
            }
        }
        batch.setColor(1f, 1f, 0.3f, 0.4f);
        batch.draw(whiteTex, x - 4f, y - 4f, cardWidth + 8f, height + 8f);
        batch.setColor(Color.WHITE);
    }

    private boolean handleTouch(int screenX, int screenY) {
        tmp.set(screenX, screenY);
        viewport.unproject(tmp);

        Pile hit = findPileAt(tmp.x, tmp.y);
        if (hit == null) {
            clearSelection();
            return false;
        }

        if (hit.type == PileType.STOCK) {
            handleStockClick();
            return true;
        }

        if (selectedPile == null) {
            return handleSelect(hit, tmp.x, tmp.y);
        }

        if (selectedPile == hit && hit.type == PileType.TABLEAU) {
            return handleSelect(hit, tmp.x, tmp.y);
        }

        if (tryMoveSelection(hit)) {
            clearSelection();
            revealTableauTop();
            return true;
        }

        clearSelection();
        return false;
    }

    private void handleStockClick() {
        clearSelection();
        if (stock.cards.size > 0) {
            Card card = stock.cards.pop();
            card.faceUp = true;
            waste.cards.add(card);
        } else if (waste.cards.size > 0) {
            while (waste.cards.size > 0) {
                Card card = waste.cards.pop();
                card.faceUp = false;
                stock.cards.add(card);
            }
        }
    }

    private boolean handleSelect(Pile pile, float x, float y) {
        if (pile.cards.size == 0) {
            clearSelection();
            return false;
        }

        if (pile.type == PileType.WASTE || pile.type == PileType.FOUNDATION) {
            selectedPile = pile;
            selectedIndex = pile.cards.size - 1;
            return true;
        }

        if (pile.type == PileType.TABLEAU) {
            int index = findTableauCardIndex(pile, y);
            if (index < 0) {
                clearSelection();
                return false;
            }
            Card card = pile.cards.get(index);
            if (!card.faceUp) {
                if (index == pile.cards.size - 1) {
                    card.faceUp = true;
                }
                clearSelection();
                return true;
            }
            selectedPile = pile;
            selectedIndex = index;
            return true;
        }

        return false;
    }

    private int findTableauCardIndex(Pile pile, float y) {
        float currentY = pile.y;
        for (int i = 0; i < pile.cards.size; i++) {
            Card card = pile.cards.get(i);
            float nextY = currentY - (card.faceUp ? tableauSpacingFaceUp : tableauSpacingFaceDown);
            boolean isLast = i == pile.cards.size - 1;
            float cardBottom = isLast ? currentY - cardHeight : nextY;
            float minY = Math.min(currentY, cardBottom);
            float maxY = Math.max(currentY, cardBottom);
            if (y >= minY && y <= maxY) {
                return i;
            }
            currentY = nextY;
        }
        return -1;
    }

    private boolean tryMoveSelection(Pile destination) {
        if (selectedPile == null) {
            return false;
        }
        Array<Card> moving = getSelectedCards();
        if (moving.size == 0) {
            return false;
        }

        if (destination.type == PileType.FOUNDATION) {
            if (moving.size != 1) {
                return false;
            }
            Card card = moving.first();
            if (canPlaceOnFoundation(destination, card)) {
                removeSelectedCards();
                destination.cards.add(card);
                return true;
            }
            return false;
        }

        if (destination.type == PileType.TABLEAU) {
            if (canPlaceOnTableau(destination, moving.first())) {
                removeSelectedCards();
                for (Card card : moving) {
                    destination.cards.add(card);
                }
                return true;
            }
            return false;
        }

        return false;
    }

    private Array<Card> getSelectedCards() {
        Array<Card> moving = new Array<>();
        if (selectedPile == null || selectedIndex < 0) {
            return moving;
        }
        for (int i = selectedIndex; i < selectedPile.cards.size; i++) {
            moving.add(selectedPile.cards.get(i));
        }
        return moving;
    }

    private void removeSelectedCards() {
        if (selectedPile == null || selectedIndex < 0) {
            return;
        }
        for (int i = selectedPile.cards.size - 1; i >= selectedIndex; i--) {
            selectedPile.cards.removeIndex(i);
        }
    }

    private boolean canPlaceOnFoundation(Pile foundation, Card card) {
        if (foundation.cards.size == 0) {
            return card.rank == 1;
        }
        Card top = foundation.cards.peek();
        return top.suit == card.suit && card.rank == top.rank + 1;
    }

    private boolean canPlaceOnTableau(Pile pile, Card card) {
        if (pile.cards.size == 0) {
            return card.rank == 13;
        }
        Card top = pile.cards.peek();
        return top.faceUp && top.isRed() != card.isRed() && card.rank == top.rank - 1;
    }

    private void revealTableauTop() {
        for (Pile pile : tableau) {
            if (pile.cards.size > 0) {
                Card top = pile.cards.peek();
                if (!top.faceUp) {
                    top.faceUp = true;
                }
            }
        }
    }

    private void clearSelection() {
        selectedPile = null;
        selectedIndex = -1;
    }

    private Pile findPileAt(float x, float y) {
        if (hitStack(stock, x, y)) return stock;
        if (hitStack(waste, x, y)) return waste;
        for (Pile foundation : foundations) {
            if (hitStack(foundation, x, y)) return foundation;
        }
        for (Pile pile : tableau) {
            if (hitTableau(pile, x, y)) return pile;
        }
        return null;
    }

    private boolean hitStack(Pile pile, float x, float y) {
        return x >= pile.x && x <= pile.x + cardWidth && y >= pile.y && y <= pile.y + cardHeight;
    }

    private boolean hitTableau(Pile pile, float x, float y) {
        if (pile.cards.size == 0) {
            return hitStack(pile, x, y);
        }
        float currentY = pile.y;
        for (int i = 0; i < pile.cards.size; i++) {
            Card card = pile.cards.get(i);
            float nextY = currentY - (card.faceUp ? tableauSpacingFaceUp : tableauSpacingFaceDown);
            float bottomY = (i == pile.cards.size - 1) ? currentY - cardHeight : nextY;
            float minY = Math.min(currentY, bottomY);
            float maxY = Math.max(currentY, bottomY);
            if (x >= pile.x && x <= pile.x + cardWidth && y >= minY && y <= maxY) {
                return true;
            }
            currentY = nextY;
        }
        return false;
    }

    private static class Pile {
        final PileType type;
        final Array<Card> cards = new Array<>();
        float x;
        float y;

        Pile(PileType type) {
            this.type = type;
        }

        void setPosition(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class Card {
        final Suit suit;
        final int rank;
        boolean faceUp;

        Card(Suit suit, int rank) {
            this.suit = suit;
            this.rank = rank;
        }

        boolean isRed() {
            return suit == Suit.HEARTS || suit == Suit.DIAMONDS;
        }

        String label() {
            String rankLabel;
            switch (rank) {
                case 1:
                    rankLabel = "A";
                    break;
                case 11:
                    rankLabel = "J";
                    break;
                case 12:
                    rankLabel = "Q";
                    break;
                case 13:
                    rankLabel = "K";
                    break;
                default:
                    rankLabel = Integer.toString(rank);
                    break;
            }
            return rankLabel + suit.symbol();
        }
    }

    private enum Suit {
        CLUBS("C"),
        DIAMONDS("D"),
        HEARTS("H"),
        SPADES("S");

        private final String symbol;

        Suit(String symbol) {
            this.symbol = symbol;
        }

        String symbol() {
            return symbol;
        }
    }

    private enum PileType {
        STOCK,
        WASTE,
        FOUNDATION,
        TABLEAU
    }
}
