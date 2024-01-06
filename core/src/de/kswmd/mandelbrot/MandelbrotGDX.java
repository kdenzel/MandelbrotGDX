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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

/**
 *
 * @author Kai
 */
public class MandelbrotGDX extends Game{

    public static Screen MANDELBROT_SCREEN;
    public static Screen LOADING_SCREEN;
    public static TransitionScreen TRANSITION_SCREEN;
    
    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        MANDELBROT_SCREEN = new MandelbrotScreen();
        TRANSITION_SCREEN = new TransitionScreen(this);
        LOADING_SCREEN = new LoadingScreen(TRANSITION_SCREEN,this);
        TRANSITION_SCREEN.setScreens(LOADING_SCREEN, MANDELBROT_SCREEN);
        setScreen(LOADING_SCREEN);
    }

    @Override
    public void pause() {
   
    }

    @Override
    public void dispose() {
        Gdx.app.debug(MandelbrotGDX.class.getSimpleName(), "Disposing");
        MANDELBROT_SCREEN.dispose();
        LOADING_SCREEN.dispose();
        TRANSITION_SCREEN.dispose();
        System.exit(-1);
    }
    
    
    
    
    
}
