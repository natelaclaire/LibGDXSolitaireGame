package com.natelaclaire.solitaire.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.natelaclaire.solitaire.game.Card;

public class Assets {
    private Texture whiteTex;
    private Texture backTexture;
    private TextureRegion backRegion;
    private ObjectMap<String, TextureRegion> cardRegions;
    private Array<Texture> cardTextures;

    public Assets() {
        whiteTex = createSolidTexture(Color.WHITE);
    }

    public Texture getWhiteTex() {
        return whiteTex;
    }

    public TextureRegion getBackRegion() {
        return backRegion;
    }

    public TextureRegion getCardRegion(Card card, String frontPrefix) {
        if (cardRegions == null) {
            return null;
        }
        return cardRegions.get(card.assetKey(frontPrefix));
    }

    public void reloadCardArt(String frontPrefix, String backName) {
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

    public void dispose() {
        if (whiteTex != null) {
            whiteTex.dispose();
            whiteTex = null;
        }
        disposeCardArt();
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
        backRegion = null;
    }

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
