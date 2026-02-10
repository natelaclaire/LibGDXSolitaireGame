package com.natelaclaire.solitaire.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.natelaclaire.solitaire.game.Card;
import com.natelaclaire.solitaire.game.GameEngine;
import com.natelaclaire.solitaire.game.GameState;
import com.natelaclaire.solitaire.game.Pile;
import com.natelaclaire.solitaire.game.PileType;

public class GameRenderer {
    private static final Color TABLE_COLOR = new Color(0.10f, 0.45f, 0.18f, 1f);
    private static final Color CARD_FACE = new Color(0.97f, 0.97f, 0.94f, 1f);
    private static final Color CARD_BACK = new Color(0.16f, 0.32f, 0.62f, 1f);
    private static final Color OUTLINE_COLOR = new Color(0f, 0f, 0f, 0.45f);

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final Assets assets;
    private final GameLayout layoutData;
    private final UiState ui;

    public GameRenderer(SpriteBatch batch, BitmapFont font, GlyphLayout layout, Assets assets, GameLayout layoutData,
                        UiState ui) {
        this.batch = batch;
        this.font = font;
        this.layout = layout;
        this.assets = assets;
        this.layoutData = layoutData;
        this.ui = ui;
    }

    public void render(GameEngine engine) {
        GameState state = engine.getState();
        drawPiles(state, engine);
        if (ui.dragging && ui.selectedPile != null && ui.selectedIndex >= 0) {
            drawDraggedCards(engine);
        }
    }

    public Color getTableColor() {
        return TABLE_COLOR;
    }

    private void drawPiles(GameState state, GameEngine engine) {
        drawPile(state.stock, engine);
        drawWastePile(state.waste, engine);
        for (Pile foundation : state.foundations) {
            drawPile(foundation, engine);
        }
        for (Pile pile : state.tableau) {
            drawPile(pile, engine);
        }

        if (!ui.dragging && ui.selectedPile != null && ui.selectedIndex >= 0) {
            drawSelection(engine);
        }

        if (engine.isWin()) {
            drawWinBanner();
        }

        drawNewGameButton();
        drawRulesButton();
        drawOptionsButton();
        drawUndoButton();
        drawScore(engine);
        if (ui.rulesVisible) {
            drawRulesOverlay();
        }
        if (ui.optionsVisible) {
            drawOptionsOverlay(engine);
        }
    }

    private void drawPile(Pile pile, GameEngine engine) {
        if (pile.type == PileType.TABLEAU) {
            drawTableauPile(pile, engine);
        } else {
            drawStackedPile(pile);
        }
    }

    private void drawStackedPile(Pile pile) {
        int effectiveSize = pile.cards.size;
        if (ui.dragging && pile == ui.selectedPile) {
            effectiveSize = Math.min(effectiveSize, ui.selectedIndex);
        }
        if (effectiveSize == 0) {
            drawOutline(pile.x, pile.y, layoutData.cardWidth, layoutData.cardHeight);
            return;
        }
        Card top = pile.cards.get(effectiveSize - 1);
        drawCard(pile.x, pile.y, top);
    }

    private void drawWastePile(Pile waste, GameEngine engine) {
        int size = waste.cards.size;
        if (ui.dragging && ui.selectedPile == waste) {
            size = Math.max(0, size - 1);
        }
        if (size == 0) {
            drawOutline(waste.x, waste.y, layoutData.cardWidth, layoutData.cardHeight);
            return;
        }
        int visible = engine.getDrawCount() == 3 ? 3 : 1;
        int start = Math.max(0, size - visible);
        float offset = engine.getDrawCount() == 3 ? layoutData.cardWidth * 0.3f : 0f;
        for (int i = start; i < size; i++) {
            float x = waste.x + (i - start) * offset;
            drawCard(x, waste.y, waste.cards.get(i));
        }
    }

    private void drawTableauPile(Pile pile, GameEngine engine) {
        if (pile.cards.size == 0) {
            drawOutline(pile.x, pile.y, layoutData.cardWidth, layoutData.cardHeight);
            return;
        }

        float y = pile.y;
        for (int i = 0; i < pile.cards.size; i++) {
            if (ui.dragging && pile == ui.selectedPile && i >= ui.selectedIndex) {
                break;
            }
            Card card = pile.cards.get(i);
            drawCard(pile.x, y, card);
            y -= card.faceUp ? layoutData.tableauSpacingFaceUp : layoutData.tableauSpacingFaceDown;
        }
    }

    private void drawCard(float x, float y, Card card) {
        if (card.faceUp) {
            TextureRegion region = assets.getCardRegion(card, ui.frontPrefix);
            if (region != null) {
                batch.draw(region, x, y, layoutData.cardWidth, layoutData.cardHeight);
            } else {
                batch.setColor(CARD_FACE);
                batch.draw(assets.getWhiteTex(), x, y, layoutData.cardWidth, layoutData.cardHeight);
                batch.setColor(Color.WHITE);
            }
        } else if (assets.getBackRegion() != null) {
            batch.draw(assets.getBackRegion(), x, y, layoutData.cardWidth, layoutData.cardHeight);
        } else {
            batch.setColor(CARD_BACK);
            batch.draw(assets.getWhiteTex(), x, y, layoutData.cardWidth, layoutData.cardHeight);
            batch.setColor(Color.WHITE);
        }
        drawOutline(x, y, layoutData.cardWidth, layoutData.cardHeight);
    }

    private void drawOutline(float x, float y, float width, float height) {
        batch.setColor(OUTLINE_COLOR);
        batch.draw(assets.getWhiteTex(), x, y, width, 2f);
        batch.draw(assets.getWhiteTex(), x, y + height - 2f, width, 2f);
        batch.draw(assets.getWhiteTex(), x, y, 2f, height);
        batch.draw(assets.getWhiteTex(), x + width - 2f, y, 2f, height);
        batch.setColor(Color.WHITE);
    }

    private void drawSelection(GameEngine engine) {
        if (ui.selectedPile == null) {
            return;
        }
        float x = ui.selectedPile.x;
        float y = ui.selectedPile.y;
        float height = layoutData.cardHeight;
        if (ui.selectedPile.type == PileType.TABLEAU) {
            float[] positions = layoutData.buildTableauCardPositions(ui.selectedPile);
            float minY = positions[ui.selectedIndex];
            float maxY = positions[ui.selectedIndex] + layoutData.cardHeight;
            for (int i = ui.selectedIndex + 1; i < ui.selectedPile.cards.size; i++) {
                float cy = positions[i];
                minY = Math.min(minY, cy);
                maxY = Math.max(maxY, cy + layoutData.cardHeight);
            }
            y = minY;
            height = maxY - minY;
        } else if (ui.selectedPile.type == PileType.WASTE) {
            int size = ui.selectedPile.cards.size;
            int visible = engine.getDrawCount() == 3 ? 3 : 1;
            int start = Math.max(0, size - visible);
            float offset = engine.getDrawCount() == 3 ? layoutData.cardWidth * 0.3f : 0f;
            x = ui.selectedPile.x + (size - 1 - start) * offset;
            height = layoutData.cardHeight;
        }
        batch.setColor(1f, 1f, 0.3f, 0.4f);
        batch.draw(assets.getWhiteTex(), x - 4f, y - 4f, layoutData.cardWidth + 8f, height + 8f);
        batch.setColor(Color.WHITE);
    }

    private void drawDraggedCards(GameEngine engine) {
        Array<Card> moving = getSelectedCards();
        if (moving.size == 0) {
            return;
        }
        float y = ui.dragY;
        for (int i = 0; i < moving.size; i++) {
            Card card = moving.get(i);
            drawCard(ui.dragX, y, card);
            y -= layoutData.tableauSpacingFaceUp;
        }
    }

    private Array<Card> getSelectedCards() {
        Array<Card> moving = new Array<>();
        if (ui.selectedPile == null || ui.selectedIndex < 0) {
            return moving;
        }
        for (int i = ui.selectedIndex; i < ui.selectedPile.cards.size; i++) {
            moving.add(ui.selectedPile.cards.get(i));
        }
        return moving;
    }

    private void drawWinBanner() {
        float bannerWidth = layoutData.cardWidth * 5.2f;
        float bannerHeight = layoutData.cardHeight * 0.9f;
        float x = (layoutData.worldWidth - bannerWidth) * 0.5f;
        float y = (layoutData.worldHeight - bannerHeight) * 0.5f;
        batch.setColor(0f, 0f, 0f, 0.55f);
        batch.draw(assets.getWhiteTex(), x, y, bannerWidth, bannerHeight);
        batch.setColor(Color.WHITE);
        drawOutline(x, y, bannerWidth, bannerHeight);
    }

    private void drawNewGameButton() {
        drawButton(layoutData.newGameX, layoutData.newGameY, layoutData.newGameWidth, layoutData.newGameHeight, "New Game");
    }

    private void drawRulesButton() {
        drawButton(layoutData.rulesButtonX, layoutData.rulesButtonY, layoutData.rulesButtonWidth, layoutData.rulesButtonHeight, "Rules");
    }

    private void drawOptionsButton() {
        drawButton(layoutData.optionsButtonX, layoutData.optionsButtonY, layoutData.optionsButtonWidth, layoutData.optionsButtonHeight, "Options");
    }

    private void drawUndoButton() {
        drawButton(layoutData.undoButtonX, layoutData.undoButtonY, layoutData.undoButtonWidth, layoutData.undoButtonHeight, "Undo");
    }

    private void drawButton(float x, float y, float w, float h, String text) {
        batch.setColor(0f, 0f, 0f, 0.4f);
        batch.draw(assets.getWhiteTex(), x, y, w, h);
        batch.setColor(Color.WHITE);
        drawOutline(x, y, w, h);
        font.setColor(Color.WHITE);
        layout.setText(font, text);
        float textX = x + (w - layout.width) * 0.5f;
        float textY = y + (h + layout.height) * 0.5f;
        font.draw(batch, layout, textX, textY);
    }

    private void drawScore(GameEngine engine) {
        font.setColor(Color.WHITE);
        String text = "Score: " + engine.getScore();
        layout.setText(font, text);
        font.draw(batch, layout, layoutData.scoreX, layoutData.scoreY);
    }

    private void drawRulesOverlay() {
        batch.setColor(0f, 0f, 0f, 0.75f);
        batch.draw(assets.getWhiteTex(), layoutData.rulesX, layoutData.rulesY, layoutData.rulesWidth, layoutData.rulesHeight);
        batch.setColor(Color.WHITE);
        drawOutline(layoutData.rulesX, layoutData.rulesY, layoutData.rulesWidth, layoutData.rulesHeight);

        float padding = layoutData.rulesWidth * 0.05f;
        float titleY = layoutData.rulesY + layoutData.rulesHeight - padding;
        font.setColor(Color.WHITE);
        layout.setText(font, "Rules");
        font.draw(batch, layout, layoutData.rulesX + padding, titleY);

        layout.setText(font, "Close");
        float closeX = layoutData.rulesX + layoutData.rulesWidth - padding - layout.width;
        float closeY = titleY;
        font.draw(batch, layout, closeX, closeY);

        float contentTop = titleY - layout.height - padding * 0.5f;
        float contentBottom = layoutData.rulesY + padding;
        float lineHeight = font.getLineHeight();
        if (ui.rulesLines != null) {
            float y = contentTop - ui.rulesScroll;
            for (int i = 0; i < ui.rulesLines.size; i++) {
                String line = ui.rulesLines.get(i);
                if (y < contentBottom - lineHeight) {
                    break;
                }
                if (y <= contentTop + lineHeight) {
                    font.draw(batch, line, layoutData.rulesX + padding, y);
                }
                y -= lineHeight * 1.05f;
            }
        }
    }

    private void drawOptionsOverlay(GameEngine engine) {
        batch.setColor(0f, 0f, 0f, 0.78f);
        batch.draw(assets.getWhiteTex(), layoutData.optionsX, layoutData.optionsY, layoutData.optionsWidth, layoutData.optionsHeight);
        batch.setColor(Color.WHITE);
        drawOutline(layoutData.optionsX, layoutData.optionsY, layoutData.optionsWidth, layoutData.optionsHeight);

        float y = layoutData.optionsY + layoutData.optionsHeight - layoutData.optionsPadding;
        font.setColor(Color.WHITE);
        layout.setText(font, "Options");
        font.draw(batch, layout, layoutData.optionsX + layoutData.optionsPadding, y);

        y -= layoutData.optionsRowHeight;
        layout.setText(font, "Draw");
        font.draw(batch, layout, layoutData.optionsX + layoutData.optionsPadding, y);
        drawOptionButton(layoutData.draw1X, layoutData.draw1Y, layoutData.draw1W, layoutData.draw1H, "1",
            engine.getDrawCount() == 1);
        drawOptionButton(layoutData.draw3X, layoutData.draw3Y, layoutData.draw3W, layoutData.draw3H, "3",
            engine.getDrawCount() == 3);

        y -= layoutData.optionsRowHeight;
        layout.setText(font, "Front");
        font.draw(batch, layout, layoutData.optionsX + layoutData.optionsPadding, y);
        drawOptionButton(layoutData.frontClassicX, layoutData.frontClassicY, layoutData.frontClassicW,
            layoutData.frontClassicH, "Classic", "card".equals(ui.frontPrefix));
        drawOptionButton(layoutData.frontSimpleX, layoutData.frontSimpleY, layoutData.frontSimpleW,
            layoutData.frontSimpleH, "Simple", "simplecard".equals(ui.frontPrefix));

        y -= layoutData.optionsRowHeight;
        layout.setText(font, "Back");
        font.draw(batch, layout, layoutData.optionsX + layoutData.optionsPadding, y);
        drawOptionButton(layoutData.backPrevX, layoutData.backPrevY, layoutData.backPrevW, layoutData.backPrevH, "<", false);
        drawOptionButton(layoutData.backNextX, layoutData.backNextY, layoutData.backNextW, layoutData.backNextH, ">", false);
        String backLabel = formatBackName(ui.backName);
        layout.setText(font, backLabel);
        float labelX = layoutData.backPrevX + layoutData.backPrevW + layoutData.optionsPadding * 0.5f;
        float labelY = layoutData.backPrevY + (layoutData.backPrevH + layout.height) * 0.5f;
        font.draw(batch, layout, labelX, labelY);
    }

    private void drawOptionButton(float x, float y, float w, float h, String text, boolean selected) {
        if (selected) {
            batch.setColor(1f, 1f, 1f, 0.25f);
        } else {
            batch.setColor(0f, 0f, 0f, 0.4f);
        }
        batch.draw(assets.getWhiteTex(), x, y, w, h);
        batch.setColor(Color.WHITE);
        drawOutline(x, y, w, h);
        layout.setText(font, text);
        float textX = x + (w - layout.width) * 0.5f;
        float textY = y + (h + layout.height) * 0.5f;
        font.draw(batch, layout, textX, textY);
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
}
