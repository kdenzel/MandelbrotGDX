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
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

/**
 *
 * @author Kai
 */
public class TransitionScreen implements Screen {

    private final Game game;
    private Screen current;
    private Screen next;

    private FrameBuffer currentBuffer;
    private FrameBuffer nextBuffer;

    private SpriteBatch spriteBatch;

    private Sprite currentScreenSprite;
    private Sprite nextScreenSprite;

    private float alpha = 1;
    private boolean fadeDirection = true;

    public TransitionScreen(Game game) {
        this.game = game;
    }

    public void setScreens(Screen current, Screen next) {
        this.current = current;
        this.next = next;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
        spriteBatch.begin();
        spriteBatch.setColor(1, 1, 1, 1f - alpha);
        spriteBatch.draw(nextScreenSprite, 0, 0);
        spriteBatch.setColor(1, 1, 1, alpha);
        spriteBatch.draw(currentScreenSprite, 0, 0);

        spriteBatch.setColor(0, 0, 0, 1);
        spriteBatch.end();
        //Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        if (alpha >= 1) {
            fadeDirection = false;
        } else if (alpha <= 0 && fadeDirection == false) {
            Gdx.app.debug(TransitionScreen.class.getSimpleName(), "end");
            game.setScreen(next);
        }
        alpha += fadeDirection == true ? 0.01 : -0.01;

    }

    @Override
    public void show() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        spriteBatch = new SpriteBatch();

        nextBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, (int) screenWidth, (int) screenHeight, false);

        nextBuffer.begin();
        next.render(Gdx.graphics.getDeltaTime());
        nextBuffer.end();

        nextScreenSprite = new Sprite(nextBuffer.getColorBufferTexture());
        nextScreenSprite.setPosition(screenWidth, 0);
        nextScreenSprite.flip(false, true);

        currentBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, (int) screenWidth, (int) screenHeight, false);
        currentBuffer.begin();
        current.render(Gdx.graphics.getDeltaTime());
        currentBuffer.end();

        currentScreenSprite = new Sprite(currentBuffer.getColorBufferTexture());
        currentScreenSprite.setPosition(0, 0);
        currentScreenSprite.flip(false, true);
        Gdx.app.debug(TransitionScreen.class.getSimpleName(), "showing...");
    }

    @Override
    public void resume() {

    }

    @Override
    public void resize(int i, int i1) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        try {
            currentBuffer.dispose();
            nextBuffer.dispose();
            spriteBatch.dispose();
        } catch(Exception ex) {
            Gdx.app.error(TransitionScreen.class.getSimpleName(), "Warn", ex);
        }
    }
}
