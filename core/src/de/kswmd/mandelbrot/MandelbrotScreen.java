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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author Kai
 */
public final class MandelbrotScreen implements Screen {

    public static final double X_START = -2f;
    public static final double X_END = 1f;

    public static final double Y_START = -1f;
    public static final double Y_END = 1f;

    public static final boolean DEBUG = true;

    final SpriteBatch batch = new SpriteBatch();
    Texture img;
    Pixmap mandelbrot;
    int maxiterations = 1000;
    int colorRGBA = 0xaaaaaaFF;
    long zoom = 1;

    Vector2 size;
    Vector2 viewPort = new Vector2();

    double xOffset;
    double yOffset;
    long zoomFactor = 1;

    float fpsCounter;
    float timer;

    InputMultiplexer inputMultiplexer = new InputMultiplexer();

    /**
     * Stage dependend References
     */
    Stage stage;
    Label fpsLabel;
    Label coordinatesLabel;
    Label zoomLabel;
    Label zoomFactorLabel;
    Label errorLabelInDialog;
    TextField hexRGBATextField;
    TextField iterationsTextField;
    Dialog changeColorDialog;

    public MandelbrotScreen() {
        initMandelbrot();
    }

    @Override
    public void resume() {
        initMandelbrot();
    }

    private void initStage() {
        stage = new Stage();
        stage.setDebugAll(false);
        fpsLabel = new Label("FPS: ...", AssetManager.INSTANCE.defaultSkin, "default-font", Color.GREEN);
        coordinatesLabel = new Label("(x,y)", AssetManager.INSTANCE.defaultSkin, "default-font", Color.GREEN);
        zoomLabel = new Label("Zoom: " + zoom, AssetManager.INSTANCE.defaultSkin, "default-font", Color.GREEN);
        zoomFactorLabel = new Label("Zoom factor: " + zoomFactor, AssetManager.INSTANCE.defaultSkin, "default-font", Color.GREEN);
        Table table = new Table();
        table.setFillParent(true);
        table.row().expand().align(Align.topLeft);
        Table labelContainer = new Table();
        labelContainer.add(fpsLabel).align(Align.left);
        labelContainer.row();
        labelContainer.add(coordinatesLabel).align(Align.left);
        labelContainer.row();
        labelContainer.add(zoomLabel).align(Align.left);
        labelContainer.row();
        labelContainer.add(zoomFactorLabel).align(Align.left);
        table.add(labelContainer);
        stage.addActor(table);
        changeColorDialog = new Dialog("Change color", AssetManager.INSTANCE.defaultSkin);
        changeColorDialog.setVisible(false);
        Button cancelButton = new TextButton("Cancel", AssetManager.INSTANCE.defaultSkin);
        cancelButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                super.touchDown(event, x, y, pointer, button);
                boolean leftButton = event.getButton() == Input.Buttons.LEFT;
                if (leftButton) {
                    changeColorDialog.hide();
                    changeColorDialog.setVisible(false);
                }
                return false;
            }
        });
        Button submitButton = new TextButton("Submit", AssetManager.INSTANCE.defaultSkin);
        submitButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                super.touchDown(event, x, y, pointer, button);
                boolean leftButton = event.getButton() == Input.Buttons.LEFT;
                if (leftButton) {
                    try {
                        String hex = hexRGBATextField.getText().toUpperCase();
                        colorRGBA = (int) Long.parseLong(hex, 16);
                        maxiterations = Integer.parseInt(iterationsTextField.getText());
                        changeColorDialog.hide();
                        changeColorDialog.setVisible(false);
                        errorLabelInDialog.setText("");
                        destroyMandelbrotPicture();
                        initMandelbrot();
                    } catch (Exception ex) {
                        Gdx.app.error(MandelbrotScreen.class.getSimpleName(), "Warn", ex);
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        ex.printStackTrace(pw);
                        errorLabelInDialog.setText(sw.toString());
                        changeColorDialog.pack();
                    }
                }
                return false;
            }
        });
        hexRGBATextField = new TextField(Integer.toHexString(colorRGBA), AssetManager.INSTANCE.defaultSkin);
        iterationsTextField = new TextField(String.valueOf(maxiterations), AssetManager.INSTANCE.defaultSkin);
        changeColorDialog.getContentTable().add(new Label("Max iterations:", AssetManager.INSTANCE.defaultSkin));
        changeColorDialog.getContentTable().add(iterationsTextField);
        changeColorDialog.getContentTable().row();
        changeColorDialog.getContentTable().add(new Label("HEX String color RGBA:", AssetManager.INSTANCE.defaultSkin));
        changeColorDialog.getContentTable().add(hexRGBATextField);
        changeColorDialog.getContentTable().row();
        errorLabelInDialog = new Label("", AssetManager.INSTANCE.defaultSkin,"default-font",Color.RED);
        changeColorDialog.getContentTable().add(errorLabelInDialog);
        changeColorDialog.getButtonTable().add(cancelButton);
        changeColorDialog.getButtonTable().add(submitButton);
    }

    private void initMandelbrot() {
        size = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mandelbrot = new Pixmap((int) size.x, (int) size.y, Pixmap.Format.RGBA8888);
        setMandelbrotPixel();
        img = new Texture(mandelbrot);
    }

    public void setMandelbrotPixel() {
        int WIDTH = (int) size.x;
        int HEIGHT = (int) size.y;
        double xLength = (Math.abs(X_END) + Math.abs(X_START));
        double yLength = (Math.abs(Y_END) + Math.abs(Y_START));
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                //realanteil
                double a = ((((double) x / WIDTH) * xLength)) + X_START;
                //Imaginaeranteil
                double b = ((((double) y / HEIGHT) * yLength)) + Y_START;
                a = a / zoom;
                b = b / zoom;
                if ((isZero(a) || isZero(b)) && DEBUG == true) {
                    mandelbrot.drawPixel(x, y, 0x00FF00FF);
                } else {
                    int iters = iters(a + xOffset, b + yOffset);
                    if (iters >= maxiterations) {
                        mandelbrot.drawPixel(x, y, 0);
                    } else {
                        mandelbrot.drawPixel(x, y, colorRGBA >> iters);
                    }
                }
            }
        }
    }

    private int iters(double real, double imag) {
        int i;
        double x = 0;
        double y = 0;
        double x2;
        double z;
        for (i = 0; i < maxiterations; i++) {
            z = Math.sqrt(x * x + y * y);
            if (z > 2) {
                break;
            }
            x2 = (x * x - y * y) + real;
            y = (2 * x * y) + imag;
            x = x2;
        }
        return i;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(img, 0, 0);
        batch.end();
        if (stage != null) {
            stage.act(delta);
            stage.draw();
        }
        timer += delta;
        fpsCounter++;
        if (timer >= 1) {
            fpsLabel.setText("FPS: " + fpsCounter);
            timer = 0;
            fpsCounter = 0;
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputMultiplexer);
        initStage();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(new InputAdapter() {

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (changeColorDialog.isVisible()) {
                    return true;
                }
                destroyMandelbrotPicture();
                int WIDTH = Math.round(size.x);
                int HEIGHT = Math.round(size.y);
                double xLength = Math.abs(X_END) + Math.abs(X_START);
                double yLength = Math.abs(Y_END) + Math.abs(Y_START);
                xOffset = xOffset + ((((double) (screenX - (int) viewPort.x * 2) / WIDTH) * xLength / zoom) + X_START / zoom);
                yOffset = yOffset + ((((double) (screenY - (int) viewPort.y * 2) / HEIGHT) * yLength / zoom) + Y_START / zoom);
                initMandelbrot();
                return true;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                double xLength = Math.abs(X_END) + Math.abs(X_START);
                double yLength = Math.abs(Y_END) + Math.abs(Y_START);
                double xO = xOffset;
                double yO = yOffset;
                int WIDTH = Math.round(size.x);
                int HEIGHT = Math.round(size.y);
                xO = xO + ((((double) (screenX - (int) viewPort.x * 2) / WIDTH) * xLength / zoom) + X_START / zoom);
                yO = yO + ((((double) (screenY - (int) viewPort.y * 2) / HEIGHT) * yLength / zoom) + Y_START / zoom);
                Gdx.app.debug(MandelbrotScreen.class.getSimpleName(), "(x,y) " + xO + "," + yO);
                coordinatesLabel.setText("(x,y) " + xO + "," + yO);
                return true;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                if (changeColorDialog.isVisible()) {
                    return true;
                }
                destroyMandelbrotPicture();
                zoom = (long) Math.max(1l, (zoom + (zoomFactor * amountY * -1)));
                Gdx.app.debug(MandelbrotScreen.class.getSimpleName(), "scrolled X: " + amountX + ", Y: " + amountY + " zoom: " + zoom);
                zoomLabel.setText("Zoom: " + zoom);
                initMandelbrot();
                return true;
            }

            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.SPACE) {
                    destroyMandelbrotPicture();
                    zoom = 1;
                    zoomFactor = 1;
                    xOffset = 0;
                    yOffset = 0;
                    maxiterations = 1000;
                    zoomLabel.setText("Zoom: " + zoom);
                    zoomFactorLabel.setText("Zoom factor: " + zoomFactor);
                    iterationsTextField.setText(String.valueOf(maxiterations));
                    hexRGBATextField.setText(Integer.toHexString(colorRGBA));
                    initMandelbrot();
                } else if (keycode == Input.Keys.PAGE_UP || keycode == Input.Keys.PAGE_DOWN) {
                    zoomFactor = keycode == Input.Keys.PAGE_UP ? zoomFactor * 2 : Math.max(1, zoomFactor / 2);
                    Gdx.app.debug(MandelbrotScreen.class.getSimpleName(), "scrolled zoom factor: " + zoomFactor);
                    zoomFactorLabel.setText("Zoom factor: " + zoomFactor);
                } else if (keycode == Input.Keys.ENTER && !changeColorDialog.isVisible()) {
                    changeColorDialog.show(stage);
                    changeColorDialog.setVisible(true);
                } else if (keycode == Input.Keys.ESCAPE) {
                    Gdx.app.exit();
                }
                return false;
            }

        });
        initMandelbrot();
    }

    private boolean isZero(double value) {
        return value == 0;
    }

    @Override
    public void resize(int width, int height) {
        size = Scaling.stretch.apply(size.x, size.y, width, height);
        viewPort.set((width - size.x) / 2, (height - size.y) / 2);
        int sizeX = Math.round(size.x);
        int sizeY = Math.round(size.y);
        Gdx.gl.glViewport(Math.round(viewPort.x), Math.round(viewPort.y), sizeX, sizeY);
        stage.getViewport().update(sizeX, sizeY, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void hide() {
        destroyMandelbrotPicture();
    }

    @Override
    public void dispose() {
        destroyMandelbrotPicture();
        batch.dispose();
        stage.dispose();
    }

    public void destroyMandelbrotPicture() {
        mandelbrot.dispose();
        img.dispose();
    }
}
