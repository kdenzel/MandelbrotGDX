/*
 * The MIT License
 *
 * Copyright 2024 Kai.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.kswmd.mandelbrot;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 *
 * @author Kai
 */
public class LoadingScreen extends ScreenAdapter {

    private Stage stage;

    private Texture badlogic;
    private SpriteBatch batch;
    private float timer = 0;
    private float fpsCounter = 0;
    private float fpsTimer;

    private final Screen nextScreen;
    private final Game game;

    private Label fpsLabel;
    
    private boolean skip;

    public LoadingScreen(Screen nextScreen, Game game) {
        this.nextScreen = nextScreen;
        this.game = game;
    }

    @Override
    public void show() {
        badlogic = new Texture("badlogic.jpg");
        batch = new SpriteBatch();
        stage = new Stage(new ScreenViewport());
        stage.setDebugAll(false);
        Table table = new Table();
        table.setFillParent(true);

        Image badlogicImage = new Image();
        badlogicImage.setDrawable(new TextureRegionDrawable(new TextureRegion(badlogic)));
        badlogicImage.setSize(badlogic.getWidth(), badlogic.getHeight());

        table.row().align(Align.center);
        table.add(badlogicImage);
        table.row().align(Align.center);
        fpsLabel = new Label("FPS: ...", AssetManager.INSTANCE.defaultSkin);
        table.add(fpsLabel);
        stage.addActor(table);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
        timer += delta;
        if (timer > 6 && !skip) {
            skip = true;
            game.setScreen(nextScreen);
        }
        fpsTimer += delta;
        fpsCounter++;
        if(fpsTimer > 1){
            fpsLabel.setText("FPS: " + fpsCounter);
            fpsTimer = 0;
            fpsCounter = 0;
        }

    }

    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
    }

}
