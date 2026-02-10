package com.natelaclaire.solitaire;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.natelaclaire.solitaire.game.GameEngine;
import com.natelaclaire.solitaire.ui.Assets;
import com.natelaclaire.solitaire.ui.GameLayout;
import com.natelaclaire.solitaire.ui.GameRenderer;
import com.natelaclaire.solitaire.ui.InputController;
import com.natelaclaire.solitaire.ui.UiState;

public class SolitaireGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout glyphLayout;
    private OrthographicCamera camera;
    private FitViewport viewport;

    private GameEngine engine;
    private GameLayout layout;
    private UiState ui;
    private Assets assets;
    private GameRenderer renderer;
    private InputController inputController;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        glyphLayout = new GlyphLayout();

        camera = new OrthographicCamera();
        viewport = new FitViewport(1024f, 768f, camera);
        viewport.apply(true);

        engine = new GameEngine();
        layout = new GameLayout();
        ui = new UiState();
        assets = new Assets();
        assets.reloadCardArt(ui.frontPrefix, ui.backName);
        renderer = new GameRenderer(batch, font, glyphLayout, assets, layout, ui);
        inputController = new InputController(viewport, layout, ui, engine, assets, this::updateLayout);

        updateLayout();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                return inputController.touchDown(screenX, screenY);
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return inputController.touchDragged(screenX, screenY);
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                return inputController.touchUp(screenX, screenY);
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                return inputController.scrolled(amountY);
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
        ScreenUtils.clear(renderer.getTableColor());
        if (layout.worldWidth == 0f || layout.worldHeight == 0f) {
            updateLayout();
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        renderer.render(engine);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        assets.dispose();
    }

    private void updateLayout() {
        layout.update(viewport, engine.getState(), font, glyphLayout);
        ui.updateRulesLayout(layout, font, glyphLayout);
    }
}
