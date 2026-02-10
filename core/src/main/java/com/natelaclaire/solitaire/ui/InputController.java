package com.natelaclaire.solitaire.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.natelaclaire.solitaire.game.Card;
import com.natelaclaire.solitaire.game.GameEngine;
import com.natelaclaire.solitaire.game.GameState;
import com.natelaclaire.solitaire.game.Pile;
import com.natelaclaire.solitaire.game.PileType;

public class InputController {
    private final FitViewport viewport;
    private final Vector2 tmp = new Vector2();
    private final GameLayout layout;
    private final UiState ui;
    private final GameEngine engine;
    private final Assets assets;
    private final Callbacks callbacks;

    public interface Callbacks {
        void onStateChanged();
    }

    public InputController(FitViewport viewport, GameLayout layout, UiState ui, GameEngine engine, Assets assets,
                           Callbacks callbacks) {
        this.viewport = viewport;
        this.layout = layout;
        this.ui = ui;
        this.engine = engine;
        this.assets = assets;
        this.callbacks = callbacks;
    }

    public boolean touchDown(int screenX, int screenY) {
        tmp.set(screenX, screenY);
        viewport.unproject(tmp);
        ui.downX = tmp.x;
        ui.downY = tmp.y;
        ui.pointerDown = true;
        ui.justSelected = false;

        if (ui.optionsVisible) {
            if (!handleOptionsClick(tmp.x, tmp.y)) {
                ui.optionsVisible = false;
            }
            return true;
        }

        if (ui.rulesVisible) {
            ui.rulesDragging = true;
            ui.rulesDragMoved = false;
            ui.rulesDragStartY = tmp.y;
            ui.rulesOpenedThisTap = false;
            return true;
        }

        if (hitRect(tmp.x, tmp.y, layout.newGameX, layout.newGameY, layout.newGameWidth, layout.newGameHeight)) {
            engine.newGame();
            ui.clearSelection();
            callbacks.onStateChanged();
            return true;
        }

        if (hitRect(tmp.x, tmp.y, layout.rulesButtonX, layout.rulesButtonY, layout.rulesButtonWidth,
            layout.rulesButtonHeight)) {
            ui.rulesVisible = true;
            ui.resetRulesScroll(tmp.y);
            return true;
        }

        if (hitRect(tmp.x, tmp.y, layout.undoButtonX, layout.undoButtonY, layout.undoButtonWidth, layout.undoButtonHeight)) {
            engine.undoLast();
            ui.clearSelection();
            callbacks.onStateChanged();
            return true;
        }

        if (hitRect(tmp.x, tmp.y, layout.optionsButtonX, layout.optionsButtonY, layout.optionsButtonWidth,
            layout.optionsButtonHeight)) {
            ui.optionsVisible = true;
            ui.rulesVisible = false;
            return true;
        }

        GameState state = engine.getState();
        Pile hit = findPileAt(state, tmp.x, tmp.y);
        if (hit == null) {
            ui.clearSelection();
            return false;
        }

        if (hit.type == PileType.STOCK) {
            handleStockClick();
            return true;
        }

        if (ui.selectedPile == null) {
            if (handleSelect(hit, tmp.x, tmp.y)) {
                ui.justSelected = true;
                return true;
            }
            return false;
        }

        if (hit == ui.selectedPile && hit.type == PileType.TABLEAU) {
            if (handleSelect(hit, tmp.x, tmp.y)) {
                ui.justSelected = true;
                return true;
            }
        }

        return true;
    }

    public boolean touchDragged(int screenX, int screenY) {
        if (ui.optionsVisible) {
            return true;
        }
        if (ui.rulesVisible && ui.rulesDragging) {
            tmp.set(screenX, screenY);
            viewport.unproject(tmp);
            float dy = tmp.y - ui.rulesDragStartY;
            ui.rulesDragStartY = tmp.y;
            ui.rulesScroll = UiState.clamp(ui.rulesScroll - dy, ui.rulesMinScroll, ui.rulesMaxScroll);
            if (Math.abs(dy) > 2f) {
                ui.rulesDragMoved = true;
            }
            return true;
        }
        if (!ui.pointerDown || ui.selectedPile == null) {
            return false;
        }

        tmp.set(screenX, screenY);
        viewport.unproject(tmp);

        if (!ui.dragging) {
            float dx = tmp.x - ui.downX;
            float dy = tmp.y - ui.downY;
            if (dx * dx + dy * dy < 100f) {
                return true;
            }
            beginDragFromSelection(tmp.x, tmp.y);
        }
        ui.dragX = tmp.x - ui.dragOffsetX;
        ui.dragY = tmp.y - ui.dragOffsetY;
        return true;
    }

    public boolean touchUp(int screenX, int screenY) {
        if (ui.optionsVisible) {
            return true;
        }
        if (ui.rulesVisible) {
            ui.rulesDragging = false;
            if (ui.rulesOpenedThisTap) {
                ui.rulesOpenedThisTap = false;
                return true;
            }
            if (!ui.rulesDragMoved) {
                ui.rulesVisible = false;
            }
            return true;
        }
        if (engine.isWin()) {
            ui.clearSelection();
            return true;
        }
        tmp.set(screenX, screenY);
        viewport.unproject(tmp);

        if (ui.dragging) {
            GameState state = engine.getState();
            Pile destination = findPileAt(state, tmp.x, tmp.y);
            if (destination != null && engine.tryMove(ui.selectedPile, ui.selectedIndex, destination)) {
                engine.revealTopAfterMove(ui.selectedPile);
                ui.clearSelection();
            }
            ui.dragging = false;
            ui.pointerDown = false;
            return true;
        }

        ui.pointerDown = false;
        if (ui.selectedPile == null) {
            return false;
        }

        GameState state = engine.getState();
        Pile destination = findPileAt(state, tmp.x, tmp.y);
        if (destination == null) {
            ui.clearSelection();
            return false;
        }

        if (!ui.justSelected && destination != ui.selectedPile) {
            if (engine.tryMove(ui.selectedPile, ui.selectedIndex, destination)) {
                engine.revealTopAfterMove(ui.selectedPile);
                ui.clearSelection();
                return true;
            }
        }

        return true;
    }

    public boolean scrolled(float amountY) {
        if (ui.optionsVisible || !ui.rulesVisible) {
            return false;
        }
        ui.rulesScroll = UiState.clamp(ui.rulesScroll + amountY * 24f, ui.rulesMinScroll, ui.rulesMaxScroll);
        return true;
    }

    private void handleStockClick() {
        if (engine.isWin()) {
            return;
        }
        ui.clearSelection();
        engine.drawFromStock();
    }

    private boolean handleSelect(Pile pile, float x, float y) {
        if (pile.cards.size == 0) {
            ui.clearSelection();
            return false;
        }

        if (pile.type == PileType.WASTE || pile.type == PileType.FOUNDATION) {
            ui.selectedPile = pile;
            ui.selectedIndex = pile.cards.size - 1;
            return true;
        }

        if (pile.type == PileType.TABLEAU) {
            int index = findTableauCardIndex(pile, y);
            if (index < 0) {
                ui.clearSelection();
                return false;
            }
            Card card = pile.cards.get(index);
            if (!card.faceUp) {
                engine.flipTopIfNeeded(pile, index);
                ui.clearSelection();
                return true;
            }
            ui.selectedPile = pile;
            ui.selectedIndex = index;
            return true;
        }

        return false;
    }

    private void beginDragFromSelection(float pointerX, float pointerY) {
        if (ui.selectedPile == null || ui.selectedIndex < 0) {
            return;
        }
        float originX = ui.selectedPile.x;
        float originY = ui.selectedPile.y;
        if (ui.selectedPile.type == PileType.TABLEAU) {
            originY = layout.buildTableauCardPositions(ui.selectedPile)[ui.selectedIndex];
        }
        ui.dragging = true;
        ui.dragOffsetX = pointerX - originX;
        ui.dragOffsetY = pointerY - originY;
        ui.dragX = originX;
        ui.dragY = originY;
    }

    private int findTableauCardIndex(Pile pile, float y) {
        float[] positions = layout.buildTableauCardPositions(pile);
        for (int i = positions.length - 1; i >= 0; i--) {
            float cardY = positions[i];
            if (y >= cardY && y <= cardY + layout.cardHeight) {
                return i;
            }
        }
        return -1;
    }

    private Pile findPileAt(GameState state, float x, float y) {
        if (hitWaste(state.waste, x, y)) return state.waste;
        if (hitStack(state.stock, x, y)) return state.stock;
        for (Pile foundation : state.foundations) {
            if (hitStack(foundation, x, y)) return foundation;
        }
        for (Pile pile : state.tableau) {
            if (hitTableau(pile, x, y)) return pile;
        }
        return null;
    }

    private boolean hitStack(Pile pile, float x, float y) {
        return x >= pile.x && x <= pile.x + layout.cardWidth && y >= pile.y && y <= pile.y + layout.cardHeight;
    }

    private boolean hitWaste(Pile waste, float x, float y) {
        if (waste.cards.size == 0) {
            return hitStack(waste, x, y);
        }
        int size = waste.cards.size;
        int visible = engine.getDrawCount() == 3 ? 3 : 1;
        int start = Math.max(0, size - visible);
        float offset = engine.getDrawCount() == 3 ? layout.cardWidth * 0.3f : 0f;
        for (int i = start; i < size; i++) {
            float cardX = waste.x + (i - start) * offset;
            if (x >= cardX && x <= cardX + layout.cardWidth && y >= waste.y && y <= waste.y + layout.cardHeight) {
                return true;
            }
        }
        return false;
    }

    private boolean hitTableau(Pile pile, float x, float y) {
        if (pile.cards.size == 0) {
            return hitStack(pile, x, y);
        }
        if (x < pile.x || x > pile.x + layout.cardWidth) {
            return false;
        }
        float[] positions = layout.buildTableauCardPositions(pile);
        for (int i = positions.length - 1; i >= 0; i--) {
            float cardY = positions[i];
            if (y >= cardY && y <= cardY + layout.cardHeight) {
                return true;
            }
        }
        return false;
    }

    private boolean handleOptionsClick(float x, float y) {
        if (hitRect(x, y, layout.draw1X, layout.draw1Y, layout.draw1W, layout.draw1H)) {
            engine.setDrawCount(1);
            return true;
        }
        if (hitRect(x, y, layout.draw3X, layout.draw3Y, layout.draw3W, layout.draw3H)) {
            engine.setDrawCount(3);
            return true;
        }
        if (hitRect(x, y, layout.frontClassicX, layout.frontClassicY, layout.frontClassicW, layout.frontClassicH)) {
            if (!"card".equals(ui.frontPrefix)) {
                ui.frontPrefix = "card";
                assets.reloadCardArt(ui.frontPrefix, ui.backName);
            }
            return true;
        }
        if (hitRect(x, y, layout.frontSimpleX, layout.frontSimpleY, layout.frontSimpleW, layout.frontSimpleH)) {
            if (!"simplecard".equals(ui.frontPrefix)) {
                ui.frontPrefix = "simplecard";
                assets.reloadCardArt(ui.frontPrefix, ui.backName);
            }
            return true;
        }
        if (hitRect(x, y, layout.backPrevX, layout.backPrevY, layout.backPrevW, layout.backPrevH)) {
            int index = findBackIndex(ui.backName);
            index = (index - 1 + SolitaireGameOptions.BACK_CHOICES.length) % SolitaireGameOptions.BACK_CHOICES.length;
            ui.backName = SolitaireGameOptions.BACK_CHOICES[index];
            assets.reloadCardArt(ui.frontPrefix, ui.backName);
            return true;
        }
        if (hitRect(x, y, layout.backNextX, layout.backNextY, layout.backNextW, layout.backNextH)) {
            int index = findBackIndex(ui.backName);
            index = (index + 1) % SolitaireGameOptions.BACK_CHOICES.length;
            ui.backName = SolitaireGameOptions.BACK_CHOICES[index];
            assets.reloadCardArt(ui.frontPrefix, ui.backName);
            return true;
        }
        return false;
    }

    private int findBackIndex(String name) {
        for (int i = 0; i < SolitaireGameOptions.BACK_CHOICES.length; i++) {
            if (SolitaireGameOptions.BACK_CHOICES[i].equals(name)) {
                return i;
            }
        }
        return 0;
    }

    private boolean hitRect(float x, float y, float rx, float ry, float rw, float rh) {
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }
}
