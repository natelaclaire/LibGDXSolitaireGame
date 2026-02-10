package com.natelaclaire.solitaire;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class SolitaireGame extends ApplicationAdapter {
    private static final Color TABLE_COLOR = new Color(0.10f, 0.45f, 0.18f, 1f);
    private static final Color CARD_FACE = new Color(0.97f, 0.97f, 0.94f, 1f);
    private static final Color CARD_BACK = new Color(0.16f, 0.32f, 0.62f, 1f);
    private static final Color OUTLINE_COLOR = new Color(0f, 0f, 0f, 0.45f);

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private ScreenViewport viewport;
    private Texture whiteTex;
    private Texture tilesheet;
    private TextureRegion backRegion;
    private ObjectMap<String, TextureRegion> cardRegions;

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
    private boolean dragging;
    private boolean pointerDown;
    private boolean justSelected;
    private float dragX;
    private float dragY;
    private float dragOffsetX;
    private float dragOffsetY;
    private float downX;
    private float downY;
    private final Vector2 tmp = new Vector2();

    @Override
    public void create() {
        batch = new SpriteBatch();

        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.apply(true);

        whiteTex = createSolidTexture(Color.WHITE);
        loadCardArt();

        setupGame();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                return handleTouchDown(screenX, screenY);
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return handleTouchDragged(screenX, screenY);
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                return handleTouchUp(screenX, screenY);
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
        if (dragging && selectedPile != null && selectedIndex >= 0) {
            drawDraggedCards();
        }
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        whiteTex.dispose();
        if (tilesheet != null) {
            tilesheet.dispose();
        }
    }

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void loadCardArt() {
        cardRegions = new ObjectMap<>();
        tilesheet = new Texture("cards/Tilesheet/cardsLarge_tilemap.png");
        tilesheet.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Array<String> names = new Array<>();
        String csv = Gdx.files.internal("cards/PNG/Cards (large)/_cards.csv").readString("UTF-8");
        for (String line : csv.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                names.add(trimmed);
            }
        }

        int columns = 14;
        int tile = 64;
        int gap = 1;
        int sheetHeight = tilesheet.getHeight();

        for (int i = 0; i < names.size; i++) {
            int col = i % columns;
            int row = i / columns;
            int x = col * (tile + gap);
            int y = sheetHeight - (row + 1) * tile - row * gap;
            TextureRegion region = new TextureRegion(tilesheet, x, y, tile, tile);
            String name = names.get(i);
            cardRegions.put(name, region);
            if ("card_back".equals(name)) {
                backRegion = region;
            }
        }
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
        float baseFaceDown = cardHeight * 0.14f;
        float baseFaceUp = cardHeight * 0.22f;
        float maxSpacing = baseFaceUp;
        int maxStack = 0;
        for (Pile pile : tableau) {
            maxStack = Math.max(maxStack, pile.cards.size);
        }
        if (maxStack > 1) {
            float available = Math.max(1f, tableauY - gutter - cardHeight);
            maxSpacing = Math.max(6f, available / (maxStack - 1));
        }
        tableauSpacingFaceUp = Math.min(baseFaceUp, maxSpacing);
        tableauSpacingFaceDown = Math.min(baseFaceDown, maxSpacing * 0.7f);
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

        if (!dragging && selectedPile != null && selectedIndex >= 0) {
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
        int effectiveSize = pile.cards.size;
        if (dragging && pile == selectedPile) {
            effectiveSize = Math.min(effectiveSize, selectedIndex);
        }
        if (effectiveSize == 0) {
            drawOutline(pile.x, pile.y, cardWidth, cardHeight);
            return;
        }
        Card top = pile.cards.get(effectiveSize - 1);
        drawCard(pile.x, pile.y, top);
    }

    private void drawTableauPile(Pile pile) {
        if (pile.cards.size == 0) {
            drawOutline(pile.x, pile.y, cardWidth, cardHeight);
            return;
        }

        float y = pile.y;
        for (int i = 0; i < pile.cards.size; i++) {
            if (dragging && pile == selectedPile && i >= selectedIndex) {
                break;
            }
            Card card = pile.cards.get(i);
            drawCard(pile.x, y, card);
            y -= card.faceUp ? tableauSpacingFaceUp : tableauSpacingFaceDown;
        }
    }

    private void drawCard(float x, float y, Card card) {
        if (card.faceUp) {
            TextureRegion region = cardRegions.get(card.assetKey());
            if (region != null) {
                batch.draw(region, x, y, cardWidth, cardHeight);
            } else {
                batch.setColor(CARD_FACE);
                batch.draw(whiteTex, x, y, cardWidth, cardHeight);
                batch.setColor(Color.WHITE);
            }
        } else if (backRegion != null) {
            batch.draw(backRegion, x, y, cardWidth, cardHeight);
        } else {
            batch.setColor(CARD_BACK);
            batch.draw(whiteTex, x, y, cardWidth, cardHeight);
            batch.setColor(Color.WHITE);
        }
        drawOutline(x, y, cardWidth, cardHeight);
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

    private void drawDraggedCards() {
        Array<Card> moving = getSelectedCards();
        if (moving.size == 0) {
            return;
        }
        float y = dragY;
        for (int i = 0; i < moving.size; i++) {
            Card card = moving.get(i);
            drawCard(dragX, y, card);
            y -= tableauSpacingFaceUp;
        }
    }

    private boolean handleTouchDown(int screenX, int screenY) {
        tmp.set(screenX, screenY);
        viewport.unproject(tmp);
        downX = tmp.x;
        downY = tmp.y;
        pointerDown = true;
        justSelected = false;

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
            if (handleSelect(hit, tmp.x, tmp.y)) {
                justSelected = true;
                return true;
            }
            return false;
        }

        if (hit == selectedPile && hit.type == PileType.TABLEAU) {
            if (handleSelect(hit, tmp.x, tmp.y)) {
                justSelected = true;
                return true;
            }
        }

        // Keep current selection; allow click-to-move on touch up.
        return true;
    }

    private boolean handleTouchDragged(int screenX, int screenY) {
        if (!pointerDown || selectedPile == null) {
            return false;
        }

        tmp.set(screenX, screenY);
        viewport.unproject(tmp);

        if (!dragging) {
            float dx = tmp.x - downX;
            float dy = tmp.y - downY;
            if (dx * dx + dy * dy < 100f) {
                return true;
            }
            beginDragFromSelection(tmp.x, tmp.y);
        }
        dragX = tmp.x - dragOffsetX;
        dragY = tmp.y - dragOffsetY;
        return true;
    }

    private boolean handleTouchUp(int screenX, int screenY) {
        tmp.set(screenX, screenY);
        viewport.unproject(tmp);

        if (dragging) {
            Pile destination = findPileAt(tmp.x, tmp.y);
            if (destination != null && tryMoveSelection(destination)) {
                revealTableauTop();
                clearSelection();
            }
            dragging = false;
            pointerDown = false;
            return true;
        }

        pointerDown = false;
        if (selectedPile == null) {
            return false;
        }

        Pile destination = findPileAt(tmp.x, tmp.y);
        if (destination == null) {
            clearSelection();
            return false;
        }

        if (!justSelected && destination != selectedPile) {
            if (tryMoveSelection(destination)) {
                revealTableauTop();
                clearSelection();
                return true;
            }
        }

        return true;
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

    private void beginDragFromSelection(float pointerX, float pointerY) {
        if (selectedPile == null || selectedIndex < 0) {
            return;
        }
        float originX = selectedPile.x;
        float originY = selectedPile.y;
        if (selectedPile.type == PileType.TABLEAU) {
            originY = buildTableauCardPositions(selectedPile)[selectedIndex];
        }
        dragging = true;
        dragOffsetX = pointerX - originX;
        dragOffsetY = pointerY - originY;
        dragX = originX;
        dragY = originY;
    }

    private int findTableauCardIndex(Pile pile, float y) {
        float[] positions = buildTableauCardPositions(pile);
        for (int i = positions.length - 1; i >= 0; i--) {
            float cardY = positions[i];
            if (y >= cardY && y <= cardY + cardHeight) {
                return i;
            }
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
        dragging = false;
        pointerDown = false;
        justSelected = false;
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
        if (x < pile.x || x > pile.x + cardWidth) {
            return false;
        }
        float[] positions = buildTableauCardPositions(pile);
        for (int i = positions.length - 1; i >= 0; i--) {
            float cardY = positions[i];
            if (y >= cardY && y <= cardY + cardHeight) {
                return true;
            }
        }
        return false;
    }

    private float[] buildTableauCardPositions(Pile pile) {
        float[] positions = new float[pile.cards.size];
        float currentY = pile.y;
        for (int i = 0; i < pile.cards.size; i++) {
            positions[i] = currentY;
            Card card = pile.cards.get(i);
            currentY -= card.faceUp ? tableauSpacingFaceUp : tableauSpacingFaceDown;
        }
        return positions;
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

        String assetKey() {
            String suitName;
            switch (suit) {
                case CLUBS:
                    suitName = "clubs";
                    break;
                case DIAMONDS:
                    suitName = "diamonds";
                    break;
                case HEARTS:
                    suitName = "hearts";
                    break;
                case SPADES:
                    suitName = "spades";
                    break;
                default:
                    suitName = "spades";
                    break;
            }

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
                case 10:
                    rankLabel = "10";
                    break;
                default:
                    rankLabel = String.format("%02d", rank);
                    break;
            }
            return "card_" + suitName + "_" + rankLabel;
        }
    }

    private enum Suit {
        CLUBS,
        DIAMONDS,
        HEARTS,
        SPADES
    }

    private enum PileType {
        STOCK,
        WASTE,
        FOUNDATION,
        TABLEAU
    }
}
