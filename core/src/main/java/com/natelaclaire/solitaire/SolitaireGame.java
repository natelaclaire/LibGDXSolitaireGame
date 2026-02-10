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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.natelaclaire.solitaire.game.Card;
import com.natelaclaire.solitaire.game.GameEngine;
import com.natelaclaire.solitaire.game.GameState;
import com.natelaclaire.solitaire.game.Pile;
import com.natelaclaire.solitaire.game.PileType;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class SolitaireGame extends ApplicationAdapter {
    private static final Color TABLE_COLOR = new Color(0.10f, 0.45f, 0.18f, 1f);
    private static final Color CARD_FACE = new Color(0.97f, 0.97f, 0.94f, 1f);
    private static final Color CARD_BACK = new Color(0.16f, 0.32f, 0.62f, 1f);
    private static final Color OUTLINE_COLOR = new Color(0f, 0f, 0f, 0.45f);

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private Texture whiteTex;
    private Texture backTexture;
    private TextureRegion backRegion;
    private ObjectMap<String, TextureRegion> cardRegions;
    private Array<Texture> cardTextures;
    private BitmapFont font;
    private GlyphLayout layout;

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
    private String frontPrefix = "card";
    private String backName = "purple_back_dark_inner.png";
    private GameEngine engine;
    private GameState state;
    private static final String[] BACK_CHOICES = {
        "black_back_dark_inner.png",
        "black_back_intricate.png",
        "black_back_light_inner.png",
        "black_back_line.png",
        "black_back_line_light.png",
        "black_back_plain.png",
        "black_back_suits.png",
        "black_back_suits_dark.png",
        "blue_back_dark_inner.png",
        "blue_back_intricate.png",
        "blue_back_light_inner.png",
        "blue_back_line.png",
        "blue_back_line_light.png",
        "blue_back_plain.png",
        "blue_back_suits.png",
        "blue_back_suits_dark.png",
        "green_back_dark_inner.png",
        "green_back_intricate.png",
        "green_back_light_inner.png",
        "green_back_line.png",
        "green_back_line_light.png",
        "green_back_plain.png",
        "green_back_suits.png",
        "green_back_suits_dark.png",
        "orange_back_dark_inner.png",
        "orange_back_intricate.png",
        "orange_back_light_inner.png",
        "orange_back_line.png",
        "orange_back_line_light.png",
        "orange_back_plain.png",
        "orange_back_suits.png",
        "orange_back_suits_dark.png",
        "purple_back_dark_inner.png",
        "purple_back_intricate.png",
        "purple_back_light_inner.png",
        "purple_back_line.png",
        "purple_back_line_light.png",
        "purple_back_plain.png",
        "purple_back_suits.png",
        "purple_back_suits_dark.png",
        "red_back_dark_inner.png",
        "red_back_intricate.png",
        "red_back_light_inner.png",
        "red_back_line.png",
        "red_back_line_light.png",
        "red_back_plain.png",
        "red_back_suits.png",
        "red_back_suits_dark.png"
    };
    private float newGameX;
    private float newGameY;
    private float newGameWidth;
    private float newGameHeight;
    private float undoButtonX;
    private float undoButtonY;
    private float undoButtonWidth;
    private float undoButtonHeight;
    private float scoreX;
    private float scoreY;
    private float optionsButtonX;
    private float optionsButtonY;
    private float optionsButtonWidth;
    private float optionsButtonHeight;
    private boolean optionsVisible;
    private float optionsX;
    private float optionsY;
    private float optionsWidth;
    private float optionsHeight;
    private float optionsPadding;
    private float optionsRowHeight;
    private float draw1X;
    private float draw1Y;
    private float draw1W;
    private float draw1H;
    private float draw3X;
    private float draw3Y;
    private float draw3W;
    private float draw3H;
    private float frontClassicX;
    private float frontClassicY;
    private float frontClassicW;
    private float frontClassicH;
    private float frontSimpleX;
    private float frontSimpleY;
    private float frontSimpleW;
    private float frontSimpleH;
    private float backPrevX;
    private float backPrevY;
    private float backPrevW;
    private float backPrevH;
    private float backNextX;
    private float backNextY;
    private float backNextW;
    private float backNextH;
    private float rulesButtonX;
    private float rulesButtonY;
    private float rulesButtonWidth;
    private float rulesButtonHeight;
    private boolean rulesVisible;
    private float rulesX;
    private float rulesY;
    private float rulesWidth;
    private float rulesHeight;
    private float rulesScroll;
    private float rulesMaxScroll;
    private boolean rulesDragging;
    private float rulesDragStartY;
    private boolean rulesDragMoved;
    private boolean rulesOpenedThisTap;
    private Array<String> rulesLines;
    private final String rulesText =
        "Goal: Move all cards to the four foundation piles (one per suit), building from Ace to King.\n\n"
            + "Setup: Seven tableau piles are dealt. The first has 1 card, the second 2, up to 7. "
            + "Only the top card in each pile is face up. The rest form the stock.\n\n"
            + "Moves:\n"
            + "- Tableau: Build down in alternating colors (red/black).\n"
            + "- Foundation: Build up in the same suit from Ace to King.\n"
            + "- Empty tableau slots accept only Kings.\n"
            + "- Turn over a face-down tableau card when it becomes the top card.\n\n"
            + "Stock: Draw 1 or 3 cards to the waste (set in Options). Only the top waste card is playable.\n"
            + "Recycle the waste back to the stock when empty (score penalty applies).\n\n"
            + "Scoring (standard draw-3):\n"
            + "+10 to foundation, +5 waste to tableau, +5 flip a tableau card, "
            + "-15 foundation to tableau, -100 recycle waste.";
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
        font = new BitmapFont();
        layout = new GlyphLayout();

        camera = new OrthographicCamera();
        viewport = new FitViewport(1024f, 768f, camera);
        viewport.apply(true);

        whiteTex = createSolidTexture(Color.WHITE);
        reloadCardArt();

        engine = new GameEngine();
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

            @Override
            public boolean scrolled(float amountX, float amountY) {
                return handleScroll(amountY);
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
        font.dispose();
        disposeCardArt();
    }

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void reloadCardArt() {
        disposeCardArt();
        cardRegions = new ObjectMap<>();
        cardTextures = new Array<>();

        backTexture = new Texture("Card_Game_GFX/Cards/card_backs/" + backName);
        backTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        backRegion = new TextureRegion(backTexture);

        boolean simple = "simplecard".equals(frontPrefix);
        String clubsToken = simple ? "club" : "clubs";
        String[] suits = {clubsToken, "diamond", "heart", "spade"};
        for (String suit : suits) {
            for (int rank = 1; rank <= 13; rank++) {
                String name = frontPrefix + "_" + suit + "_" + rank;
                String path = "Card_Game_GFX/Cards/" + name + ".png";
                Texture texture = new Texture(path);
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                cardTextures.add(texture);
                cardRegions.put(name, new TextureRegion(texture));
            }
        }
    }

    private void disposeCardArt() {
        if (backTexture != null) {
            backTexture.dispose();
            backTexture = null;
        }
        if (cardTextures != null) {
            for (Texture texture : cardTextures) {
                texture.dispose();
            }
        }
        cardTextures = null;
        cardRegions = null;
    }

    private void setupGame() {
        engine.newGame();
        refreshStateRefs();
        clearSelection();
    }

    private void refreshStateRefs() {
        state = engine.getState();
        stock = state.stock;
        waste = state.waste;
        foundations = state.foundations;
        tableau = state.tableau;
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
        newGameWidth = cardWidth * 1.6f;
        newGameHeight = cardHeight * 0.55f;
        float maxRowWidth = worldWidth - gutter * 2f;
        float desiredRowWidth = newGameWidth * 4f + gutter * 3f;
        if (desiredRowWidth > maxRowWidth) {
            newGameWidth = (maxRowWidth - gutter * 3f) / 4f;
        }
        newGameHeight = Math.min(newGameHeight, cardHeight * 0.55f);
        newGameX = worldWidth - gutter - newGameWidth;
        newGameY = gutter * 0.6f;
        rulesButtonWidth = newGameWidth;
        rulesButtonHeight = newGameHeight;
        rulesButtonX = newGameX - gutter - rulesButtonWidth;
        rulesButtonY = newGameY;
        optionsButtonWidth = newGameWidth;
        optionsButtonHeight = newGameHeight;
        optionsButtonX = rulesButtonX - gutter - optionsButtonWidth;
        optionsButtonY = newGameY;
        undoButtonWidth = newGameWidth;
        undoButtonHeight = newGameHeight;
        undoButtonX = optionsButtonX - gutter - undoButtonWidth;
        undoButtonY = newGameY;
        rulesWidth = worldWidth * 0.72f;
        rulesHeight = worldHeight * 0.72f;
        rulesX = (worldWidth - rulesWidth) * 0.5f;
        rulesY = (worldHeight - rulesHeight) * 0.5f;
        optionsWidth = rulesWidth;
        optionsHeight = rulesHeight;
        optionsX = rulesX;
        optionsY = rulesY;
        optionsPadding = optionsWidth * 0.06f;
        float baseFaceDown = cardHeight * 0.12f;
        float baseFaceUp = cardHeight * 0.30f;
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
        tableauSpacingFaceDown = Math.min(baseFaceDown, maxSpacing * 0.6f);
        tableauSpacingFaceUp = Math.min(maxSpacing, Math.max(tableauSpacingFaceUp, tableauSpacingFaceDown * 1.5f));
        if (font != null) {
            float fontScale = Math.max(0.6f, cardHeight / 220f) * 2f;
            font.getData().setScale(fontScale);
            optionsRowHeight = font.getLineHeight() * 1.8f;
            layout.setText(font, "Score: 99999");
            scoreX = gutter;
            scoreY = newGameY + (newGameHeight + layout.height) * 0.5f;
            float scoreRight = scoreX + layout.width + gutter;
            if (scoreRight > undoButtonX) {
                scoreY = newGameY + newGameHeight + layout.height + gutter * 0.5f;
            }
        }
        updateRulesLayout();
    }

    private void drawPiles() {
        drawPile(stock);
        drawWastePile();
        for (Pile foundation : foundations) {
            drawPile(foundation);
        }
        for (Pile pile : tableau) {
            drawPile(pile);
        }

        if (!dragging && selectedPile != null && selectedIndex >= 0) {
            drawSelection();
        }

        if (engine.isWin()) {
            drawWinBanner();
        }

        drawNewGameButton();
        drawRulesButton();
        drawOptionsButton();
        drawUndoButton();
        drawScore();
        if (rulesVisible) {
            drawRulesOverlay();
        }
        if (optionsVisible) {
            drawOptionsOverlay();
        }
    }

    private void drawPile(Pile pile) {
        if (pile.type == PileType.TABLEAU) {
            drawTableauPile(pile);
        } else {
            drawStackedPile(pile);
        }
    }

    private void drawWastePile() {
        int size = waste.cards.size;
        if (dragging && selectedPile == waste) {
            size = Math.max(0, size - 1);
        }
        if (size == 0) {
            drawOutline(waste.x, waste.y, cardWidth, cardHeight);
            return;
        }
        int visible = engine.getDrawCount() == 3 ? 3 : 1;
        int start = Math.max(0, size - visible);
        float offset = engine.getDrawCount() == 3 ? cardWidth * 0.3f : 0f;
        for (int i = start; i < size; i++) {
            float x = waste.x + (i - start) * offset;
            drawCard(x, waste.y, waste.cards.get(i));
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
            TextureRegion region = cardRegions.get(card.assetKey(frontPrefix));
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
        float height = cardHeight;
        if (selectedPile.type == PileType.TABLEAU) {
            float[] positions = buildTableauCardPositions(selectedPile);
            float minY = positions[selectedIndex];
            float maxY = positions[selectedIndex] + cardHeight;
            for (int i = selectedIndex + 1; i < selectedPile.cards.size; i++) {
                float cy = positions[i];
                minY = Math.min(minY, cy);
                maxY = Math.max(maxY, cy + cardHeight);
            }
            y = minY;
            height = maxY - minY;
        } else if (selectedPile.type == PileType.WASTE) {
            int size = selectedPile.cards.size;
            int visible = engine.getDrawCount() == 3 ? 3 : 1;
            int start = Math.max(0, size - visible);
            float offset = engine.getDrawCount() == 3 ? cardWidth * 0.3f : 0f;
            x = selectedPile.x + (size - 1 - start) * offset;
            height = cardHeight;
        }
        batch.setColor(1f, 1f, 0.3f, 0.4f);
        batch.draw(whiteTex, x - 4f, y - 4f, cardWidth + 8f, height + 8f);
        batch.setColor(Color.WHITE);
    }

    private void drawWinBanner() {
        float bannerWidth = cardWidth * 5.2f;
        float bannerHeight = cardHeight * 0.9f;
        float x = (worldWidth - bannerWidth) * 0.5f;
        float y = (worldHeight - bannerHeight) * 0.5f;
        batch.setColor(0f, 0f, 0f, 0.55f);
        batch.draw(whiteTex, x, y, bannerWidth, bannerHeight);
        batch.setColor(Color.WHITE);
        drawOutline(x, y, bannerWidth, bannerHeight);
    }

    private void drawNewGameButton() {
        batch.setColor(0f, 0f, 0f, 0.4f);
        batch.draw(whiteTex, newGameX, newGameY, newGameWidth, newGameHeight);
        batch.setColor(Color.WHITE);
        drawOutline(newGameX, newGameY, newGameWidth, newGameHeight);
        font.setColor(Color.WHITE);
        layout.setText(font, "New Game");
        float textX = newGameX + (newGameWidth - layout.width) * 0.5f;
        float textY = newGameY + (newGameHeight + layout.height) * 0.5f;
        font.draw(batch, layout, textX, textY);
    }

    private void drawRulesButton() {
        batch.setColor(0f, 0f, 0f, 0.4f);
        batch.draw(whiteTex, rulesButtonX, rulesButtonY, rulesButtonWidth, rulesButtonHeight);
        batch.setColor(Color.WHITE);
        drawOutline(rulesButtonX, rulesButtonY, rulesButtonWidth, rulesButtonHeight);
        font.setColor(Color.WHITE);
        layout.setText(font, "Rules");
        float textX = rulesButtonX + (rulesButtonWidth - layout.width) * 0.5f;
        float textY = rulesButtonY + (rulesButtonHeight + layout.height) * 0.5f;
        font.draw(batch, layout, textX, textY);
    }

    private void drawOptionsButton() {
        batch.setColor(0f, 0f, 0f, 0.4f);
        batch.draw(whiteTex, optionsButtonX, optionsButtonY, optionsButtonWidth, optionsButtonHeight);
        batch.setColor(Color.WHITE);
        drawOutline(optionsButtonX, optionsButtonY, optionsButtonWidth, optionsButtonHeight);
        font.setColor(Color.WHITE);
        layout.setText(font, "Options");
        float textX = optionsButtonX + (optionsButtonWidth - layout.width) * 0.5f;
        float textY = optionsButtonY + (optionsButtonHeight + layout.height) * 0.5f;
        font.draw(batch, layout, textX, textY);
    }

    private void drawUndoButton() {
        batch.setColor(0f, 0f, 0f, 0.4f);
        batch.draw(whiteTex, undoButtonX, undoButtonY, undoButtonWidth, undoButtonHeight);
        batch.setColor(Color.WHITE);
        drawOutline(undoButtonX, undoButtonY, undoButtonWidth, undoButtonHeight);
        font.setColor(Color.WHITE);
        layout.setText(font, "Undo");
        float textX = undoButtonX + (undoButtonWidth - layout.width) * 0.5f;
        float textY = undoButtonY + (undoButtonHeight + layout.height) * 0.5f;
        font.draw(batch, layout, textX, textY);
    }

    private void drawScore() {
        font.setColor(Color.WHITE);
        String text = "Score: " + engine.getScore();
        layout.setText(font, text);
        font.draw(batch, layout, scoreX, scoreY);
    }

    private void drawRulesOverlay() {
        batch.setColor(0f, 0f, 0f, 0.75f);
        batch.draw(whiteTex, rulesX, rulesY, rulesWidth, rulesHeight);
        batch.setColor(Color.WHITE);
        drawOutline(rulesX, rulesY, rulesWidth, rulesHeight);

        float padding = rulesWidth * 0.05f;
        float titleY = rulesY + rulesHeight - padding;
        font.setColor(Color.WHITE);
        layout.setText(font, "Rules");
        font.draw(batch, layout, rulesX + padding, titleY);

        layout.setText(font, "Close");
        float closeX = rulesX + rulesWidth - padding - layout.width;
        float closeY = titleY;
        font.draw(batch, layout, closeX, closeY);

        float contentTop = titleY - layout.height - padding * 0.5f;
        float contentBottom = rulesY + padding;
        float lineHeight = font.getLineHeight();
        if (rulesLines != null) {
            float y = contentTop - rulesScroll;
            for (int i = 0; i < rulesLines.size; i++) {
                String line = rulesLines.get(i);
                if (y < contentBottom - lineHeight) {
                    break;
                }
                if (y <= contentTop + lineHeight) {
                    font.draw(batch, line, rulesX + padding, y);
                }
                y -= lineHeight * 1.05f;
            }
        }
    }

    private void drawOptionsOverlay() {
        batch.setColor(0f, 0f, 0f, 0.78f);
        batch.draw(whiteTex, optionsX, optionsY, optionsWidth, optionsHeight);
        batch.setColor(Color.WHITE);
        drawOutline(optionsX, optionsY, optionsWidth, optionsHeight);

        float y = optionsY + optionsHeight - optionsPadding;
        font.setColor(Color.WHITE);
        layout.setText(font, "Options");
        font.draw(batch, layout, optionsX + optionsPadding, y);

        y -= optionsRowHeight;
        layout.setText(font, "Draw");
        font.draw(batch, layout, optionsX + optionsPadding, y);
        float buttonW = optionsWidth * 0.18f;
        float buttonH = optionsRowHeight * 0.7f;
        draw1X = optionsX + optionsWidth * 0.35f;
        draw1Y = y - buttonH * 0.65f;
        draw1W = buttonW;
        draw1H = buttonH;
        draw3X = draw1X + buttonW + optionsPadding * 0.6f;
        draw3Y = draw1Y;
        draw3W = buttonW;
        draw3H = buttonH;
        drawOptionButton(draw1X, draw1Y, draw1W, draw1H, "1", engine.getDrawCount() == 1);
        drawOptionButton(draw3X, draw3Y, draw3W, draw3H, "3", engine.getDrawCount() == 3);

        y -= optionsRowHeight;
        layout.setText(font, "Front");
        font.draw(batch, layout, optionsX + optionsPadding, y);
        frontClassicX = optionsX + optionsWidth * 0.35f;
        frontClassicY = y - buttonH * 0.65f;
        frontClassicW = buttonW;
        frontClassicH = buttonH;
        frontSimpleX = frontClassicX + buttonW + optionsPadding * 0.6f;
        frontSimpleY = frontClassicY;
        frontSimpleW = buttonW;
        frontSimpleH = buttonH;
        drawOptionButton(frontClassicX, frontClassicY, frontClassicW, frontClassicH, "Classic",
            "card".equals(frontPrefix));
        drawOptionButton(frontSimpleX, frontSimpleY, frontSimpleW, frontSimpleH, "Simple",
            "simplecard".equals(frontPrefix));

        y -= optionsRowHeight;
        layout.setText(font, "Back");
        font.draw(batch, layout, optionsX + optionsPadding, y);
        float arrowW = buttonH;
        float arrowH = buttonH;
        backPrevX = optionsX + optionsWidth * 0.35f;
        backPrevY = y - arrowH * 0.65f;
        backPrevW = arrowW;
        backPrevH = arrowH;
        backNextX = backPrevX + optionsWidth * 0.45f;
        backNextY = backPrevY;
        backNextW = arrowW;
        backNextH = arrowH;
        drawOptionButton(backPrevX, backPrevY, backPrevW, backPrevH, "<", false);
        drawOptionButton(backNextX, backNextY, backNextW, backNextH, ">", false);
        String backLabel = formatBackName(backName);
        layout.setText(font, backLabel);
        float labelX = backPrevX + arrowW + optionsPadding * 0.5f;
        float labelY = backPrevY + (arrowH + layout.height) * 0.5f;
        font.draw(batch, layout, labelX, labelY);
    }

    private void drawOptionButton(float x, float y, float w, float h, String text, boolean selected) {
        if (selected) {
            batch.setColor(1f, 1f, 1f, 0.25f);
        } else {
            batch.setColor(0f, 0f, 0f, 0.4f);
        }
        batch.draw(whiteTex, x, y, w, h);
        batch.setColor(Color.WHITE);
        drawOutline(x, y, w, h);
        layout.setText(font, text);
        float textX = x + (w - layout.width) * 0.5f;
        float textY = y + (h + layout.height) * 0.5f;
        font.draw(batch, layout, textX, textY);
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

        if (optionsVisible) {
            if (!handleOptionsClick(tmp.x, tmp.y)) {
                optionsVisible = false;
            }
            return true;
        }

        if (rulesVisible) {
            rulesDragging = true;
            rulesDragMoved = false;
            rulesDragStartY = tmp.y;
            rulesOpenedThisTap = false;
            return true;
        }

        if (hitNewGame(tmp.x, tmp.y)) {
            setupGame();
            updateLayout();
            return true;
        }

        if (hitRulesButton(tmp.x, tmp.y)) {
            rulesVisible = true;
            rulesScroll = 0f;
            rulesDragging = true;
            rulesDragStartY = tmp.y;
            rulesDragMoved = false;
            rulesOpenedThisTap = true;
            return true;
        }

        if (hitUndoButton(tmp.x, tmp.y)) {
            engine.undoLast();
            refreshStateRefs();
            updateLayout();
            clearSelection();
            return true;
        }

        if (hitOptionsButton(tmp.x, tmp.y)) {
            optionsVisible = true;
            rulesVisible = false;
            return true;
        }

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
        if (optionsVisible) {
            return true;
        }
        if (rulesVisible && rulesDragging) {
            tmp.set(screenX, screenY);
            viewport.unproject(tmp);
            float dy = tmp.y - rulesDragStartY;
            rulesDragStartY = tmp.y;
            rulesScroll = clamp(rulesScroll - dy, 0f, rulesMaxScroll);
            if (Math.abs(dy) > 2f) {
                rulesDragMoved = true;
            }
            return true;
        }
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
        if (optionsVisible) {
            return true;
        }
        if (rulesVisible) {
            rulesDragging = false;
            if (rulesOpenedThisTap) {
                rulesOpenedThisTap = false;
                return true;
            }
            if (!rulesDragMoved) {
                rulesVisible = false;
            }
            return true;
        }
        if (engine.isWin()) {
            clearSelection();
            return true;
        }
        tmp.set(screenX, screenY);
        viewport.unproject(tmp);

        if (dragging) {
            Pile destination = findPileAt(tmp.x, tmp.y);
            if (destination != null && tryMoveSelection(destination)) {
                engine.revealTopAfterMove(selectedPile);
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
                engine.revealTopAfterMove(selectedPile);
                clearSelection();
                return true;
            }
        }

        return true;
    }

    private void handleStockClick() {
        if (engine.isWin()) {
            return;
        }
        clearSelection();
        engine.drawFromStock();
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
                engine.flipTopIfNeeded(pile, index);
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
        return engine.tryMove(selectedPile, selectedIndex, destination);
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

    private void clearSelection() {
        selectedPile = null;
        selectedIndex = -1;
        dragging = false;
        pointerDown = false;
        justSelected = false;
    }

    private boolean hitNewGame(float x, float y) {
        return x >= newGameX && x <= newGameX + newGameWidth
            && y >= newGameY && y <= newGameY + newGameHeight;
    }

    private boolean hitRulesButton(float x, float y) {
        return x >= rulesButtonX && x <= rulesButtonX + rulesButtonWidth
            && y >= rulesButtonY && y <= rulesButtonY + rulesButtonHeight;
    }

    private boolean hitUndoButton(float x, float y) {
        return x >= undoButtonX && x <= undoButtonX + undoButtonWidth
            && y >= undoButtonY && y <= undoButtonY + undoButtonHeight;
    }

    private boolean hitOptionsButton(float x, float y) {
        return x >= optionsButtonX && x <= optionsButtonX + optionsButtonWidth
            && y >= optionsButtonY && y <= optionsButtonY + optionsButtonHeight;
    }

    private boolean handleScroll(float amountY) {
        if (optionsVisible || !rulesVisible) {
            return false;
        }
        rulesScroll = clamp(rulesScroll + amountY * 24f, 0f, rulesMaxScroll);
        return true;
    }

    private boolean handleOptionsClick(float x, float y) {
        if (hitRect(x, y, draw1X, draw1Y, draw1W, draw1H)) {
            engine.setDrawCount(1);
            return true;
        }
        if (hitRect(x, y, draw3X, draw3Y, draw3W, draw3H)) {
            engine.setDrawCount(3);
            return true;
        }
        if (hitRect(x, y, frontClassicX, frontClassicY, frontClassicW, frontClassicH)) {
            if (!"card".equals(frontPrefix)) {
                frontPrefix = "card";
                reloadCardArt();
            }
            return true;
        }
        if (hitRect(x, y, frontSimpleX, frontSimpleY, frontSimpleW, frontSimpleH)) {
            if (!"simplecard".equals(frontPrefix)) {
                frontPrefix = "simplecard";
                reloadCardArt();
            }
            return true;
        }
        if (hitRect(x, y, backPrevX, backPrevY, backPrevW, backPrevH)) {
            int index = findBackIndex();
            index = (index - 1 + BACK_CHOICES.length) % BACK_CHOICES.length;
            backName = BACK_CHOICES[index];
            reloadCardArt();
            return true;
        }
        if (hitRect(x, y, backNextX, backNextY, backNextW, backNextH)) {
            int index = findBackIndex();
            index = (index + 1) % BACK_CHOICES.length;
            backName = BACK_CHOICES[index];
            reloadCardArt();
            return true;
        }
        return false;
    }


    private boolean hitRect(float x, float y, float rx, float ry, float rw, float rh) {
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }

    private int findBackIndex() {
        for (int i = 0; i < BACK_CHOICES.length; i++) {
            if (BACK_CHOICES[i].equals(backName)) {
                return i;
            }
        }
        return 0;
    }

    private String formatBackName(String name) {
        String label = name.replace(".png", "").replace("_back_", " ");
        label = label.replace("_", " ");
        String[] parts = label.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }

    private void updateRulesLayout() {
        if (font == null) {
            return;
        }
        rulesLines = new Array<>();
        float padding = rulesWidth * 0.05f;
        float maxWidth = rulesWidth - padding * 2f;
        String[] paragraphs = rulesText.split("\\n");
        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                rulesLines.add("");
                continue;
            }
            String[] words = paragraph.split("\\s+");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String test = line.length() == 0 ? word : line + " " + word;
                layout.setText(font, test);
                if (layout.width > maxWidth && line.length() > 0) {
                    rulesLines.add(line.toString());
                    line.setLength(0);
                    line.append(word);
                } else {
                    line.setLength(0);
                    line.append(test);
                }
            }
            if (line.length() > 0) {
                rulesLines.add(line.toString());
            }
        }

        float lineHeight = font.getLineHeight() * 1.05f;
        float contentHeight = rulesLines.size * lineHeight;
        float contentTop = rulesY + rulesHeight - padding - font.getLineHeight();
        float contentBottom = rulesY + padding;
        float available = Math.max(0f, contentTop - contentBottom);
        rulesMaxScroll = Math.max(0f, contentHeight - available);
        rulesScroll = clamp(rulesScroll, 0f, rulesMaxScroll);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private Pile findPileAt(float x, float y) {
        if (hitWaste(x, y)) return waste;
        if (hitStack(stock, x, y)) return stock;
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

    private boolean hitWaste(float x, float y) {
        if (waste.cards.size == 0) {
            return hitStack(waste, x, y);
        }
        int size = waste.cards.size;
        int visible = engine.getDrawCount() == 3 ? 3 : 1;
        int start = Math.max(0, size - visible);
        float offset = engine.getDrawCount() == 3 ? cardWidth * 0.3f : 0f;
        for (int i = start; i < size; i++) {
            float cardX = waste.x + (i - start) * offset;
            if (x >= cardX && x <= cardX + cardWidth && y >= waste.y && y <= waste.y + cardHeight) {
                return true;
            }
        }
        return false;
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
}
