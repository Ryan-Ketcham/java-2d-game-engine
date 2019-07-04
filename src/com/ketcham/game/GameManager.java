package com.ketcham.game;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.ketcham.engine.AbstractGame;
import com.ketcham.engine.GameContainer;
import com.ketcham.engine.Renderer;
import com.ketcham.engine.gfx.Image;
import com.ketcham.engine.gfx.ImageTile;
import com.ketcham.engine.sfx.SoundClip;

public class GameManager extends AbstractGame
{
    private ImageTile image;
    private SoundClip clip;

    public static void main(String[] args)
    {
        GameContainer gc = new GameContainer(new GameManager());
        gc.start();
    }

    public GameManager()
    {
        //image = new Image("/test.png");
        image = new ImageTile("/test_tile.png", 16, 16);
        clip = new SoundClip("/audio/test_sound.wav");
    }

    @Override
    public void update(GameContainer gc)
    {
        if (gc.getInput().isButtonDown(MouseEvent.BUTTON1))
        {
            clip.play();
        }
    }

    @Override
    public void render(GameContainer gc, Renderer r)
    {
        r.drawImageTile(image, gc.getInput().getMouseX()-8, gc.getInput().getMouseY()-64, 0, 0);
    }
}