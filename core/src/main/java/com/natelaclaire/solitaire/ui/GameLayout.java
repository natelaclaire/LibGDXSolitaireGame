package com.natelaclaire.solitaire.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.natelaclaire.solitaire.game.Card;
import com.natelaclaire.solitaire.game.GameState;
import com.natelaclaire.solitaire.game.Pile;

public class GameLayout {
    public float worldWidth;
    public float worldHeight;
    public float cardWidth;
    public float cardHeight;
    public float tableauSpacingFaceDown;
    public float tableauSpacingFaceUp;
    public float gutter;

    public float newGameX;
    public float newGameY;
    public float newGameWidth;
    public float newGameHeight;
    public float rulesButtonX;
    public float rulesButtonY;
    public float rulesButtonWidth;
    public float rulesButtonHeight;
    public float optionsButtonX;
    public float optionsButtonY;
    public float optionsButtonWidth;
    public float optionsButtonHeight;
    public float undoButtonX;
    public float undoButtonY;
    public float undoButtonWidth;
    public float undoButtonHeight;
    public float scoreX;
    public float scoreY;

    public float rulesX;
    public float rulesY;
    public float rulesWidth;
    public float rulesHeight;

    public float optionsX;
    public float optionsY;
    public float optionsWidth;
    public float optionsHeight;
    public float optionsPadding;
    public float optionsRowHeight;

    public float draw1X;
    public float draw1Y;
    public float draw1W;
    public float draw1H;
    public float draw3X;
    public float draw3Y;
    public float draw3W;
    public float draw3H;
    public float frontClassicX;
    public float frontClassicY;
    public float frontClassicW;
    public float frontClassicH;
    public float frontSimpleX;
    public float frontSimpleY;
    public float frontSimpleW;
    public float frontSimpleH;
    public float backPrevX;
    public float backPrevY;
    public float backPrevW;
    public float backPrevH;
    public float backNextX;
    public float backNextY;
    public float backNextW;
    public float backNextH;

    public void update(FitViewport viewport, GameState state, BitmapFont font, GlyphLayout glyphLayout) {
        worldWidth = viewport.getWorldWidth();
        worldHeight = viewport.getWorldHeight();

        gutter = Math.max(12f, Math.min(worldWidth, worldHeight) * 0.025f);
        float maxCardWidth = (worldWidth - gutter * 8f) / 7f;
        float maxCardHeight = (worldHeight - gutter * 6f) / 3f;
        cardWidth = Math.min(maxCardWidth, maxCardHeight * 0.72f);
        cardHeight = cardWidth / 0.72f;

        float topRowY = worldHeight - gutter - cardHeight;
        state.stock.setPosition(gutter, topRowY);
        state.waste.setPosition(gutter * 2f + cardWidth, topRowY);
        for (int i = 0; i < state.foundations.size; i++) {
            float x = worldWidth - gutter - cardWidth - i * (cardWidth + gutter);
            state.foundations.get(i).setPosition(x, topRowY);
        }

        float tableauY = topRowY - gutter - cardHeight;
        for (int i = 0; i < state.tableau.size; i++) {
            float x = gutter + i * (cardWidth + gutter);
            state.tableau.get(i).setPosition(x, tableauY);
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
        for (Pile pile : state.tableau) {
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
            glyphLayout.setText(font, "Score: 99999");
            scoreX = gutter;
            scoreY = newGameY + (newGameHeight + glyphLayout.height) * 0.5f;
            float scoreRight = scoreX + glyphLayout.width + gutter;
            if (scoreRight > undoButtonX) {
                scoreY = newGameY + newGameHeight + glyphLayout.height + gutter * 0.5f;
            }
        }

        updateOptionsLayout();
    }

    private void updateOptionsLayout() {
        float y = optionsY + optionsHeight - optionsPadding;
        y -= optionsRowHeight;
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

        y -= optionsRowHeight;
        frontClassicX = optionsX + optionsWidth * 0.35f;
        frontClassicY = y - buttonH * 0.65f;
        frontClassicW = buttonW;
        frontClassicH = buttonH;
        frontSimpleX = frontClassicX + buttonW + optionsPadding * 0.6f;
        frontSimpleY = frontClassicY;
        frontSimpleW = buttonW;
        frontSimpleH = buttonH;

        y -= optionsRowHeight;
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
    }

    public float[] buildTableauCardPositions(Pile pile) {
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
