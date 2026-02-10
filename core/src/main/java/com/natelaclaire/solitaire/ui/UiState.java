package com.natelaclaire.solitaire.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Array;
import com.natelaclaire.solitaire.game.Pile;

public class UiState {
    public Pile selectedPile;
    public int selectedIndex = -1;
    public boolean dragging;
    public boolean pointerDown;
    public boolean justSelected;
    public float dragX;
    public float dragY;
    public float dragOffsetX;
    public float dragOffsetY;
    public float downX;
    public float downY;

    public boolean rulesVisible;
    public float rulesScroll;
    public float rulesMaxScroll;
    public float rulesMinScroll;
    public boolean rulesDragging;
    public float rulesDragStartY;
    public boolean rulesDragMoved;
    public boolean rulesOpenedThisTap;
    public Array<String> rulesLines;

    public boolean optionsVisible;

    public String frontPrefix = "card";
    public String backName = "purple_back_dark_inner.png";

    public static final String RULES_TEXT =
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
            + "-15 foundation to tableau, -100 recycle waste.\n\n"
            + "Credits:\n"
            + "Card designs from https://ci.itch.io/card-games-graphics-pack";

    public void clearSelection() {
        selectedPile = null;
        selectedIndex = -1;
        dragging = false;
        pointerDown = false;
        justSelected = false;
    }

    public void updateRulesLayout(GameLayout layout, BitmapFont font, GlyphLayout glyphLayout) {
        if (font == null || layout == null) {
            return;
        }
        rulesLines = new Array<>();
        float padding = layout.rulesWidth * 0.05f;
        float maxWidth = layout.rulesWidth - padding * 2f;
        String[] paragraphs = RULES_TEXT.split("\\n");
        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                rulesLines.add("");
                continue;
            }
            String[] words = paragraph.split("\\s+");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String test = line.length() == 0 ? word : line + " " + word;
                glyphLayout.setText(font, test);
                if (glyphLayout.width > maxWidth && line.length() > 0) {
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
        float contentTop = layout.rulesY + layout.rulesHeight - padding - font.getLineHeight();
        float contentBottom = layout.rulesY + padding;
        float available = Math.max(0f, contentTop - contentBottom);
        rulesMaxScroll = Math.max(0f, contentHeight - available);
        rulesMinScroll = -rulesMaxScroll;
        rulesScroll = clamp(rulesScroll, this.rulesMinScroll, rulesMaxScroll);
    }

    public void resetRulesScroll(float pointerY) {
        rulesScroll = 0f;
        rulesDragging = true;
        rulesDragStartY = pointerY;
        rulesDragMoved = false;
        rulesOpenedThisTap = true;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
