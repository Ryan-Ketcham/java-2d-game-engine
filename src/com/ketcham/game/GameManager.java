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
    private Image mouseImage;
    private Image image2;
    private ImageTile imageTile;
    private SoundClip clip;

    public static void main(String[] args)
    {
        GameContainer gc = new GameContainer(new GameManager());
        gc.start();
    }

    public GameManager()
    {
        mouseImage = new Image("/test.png", true);
        image2 = new Image("/test_2.png", true);
        imageTile = new ImageTile("/test_tile.png", true, 16, 16);
        //imageTile.setAlpha(true);
        clip = new SoundClip("/audio/test_sound.wav");
    }

    @Override
    public void update(GameContainer gc)
    {
        if (gc.getInput().isButtonDown(MouseEvent.BUTTON1))
        {
            //clip.play();
        }
        if (gc.getInput().isKey(KeyEvent.VK_ESCAPE))
        {
            //gc.stop();
        }
    }

    @Override
    public void render(GameContainer gc, Renderer r)
    {
        r.setZDepth(0);
        r.drawImage(image2, 10, 10);
        r.setZDepth(Integer.MAX_VALUE);
        r.drawImage(mouseImage, gc.getInput().getMouseX()-8, gc.getInput().getMouseY()-64);
        r.setZDepth(Integer.MAX_VALUE-1);
        r.drawImageTile(imageTile, 10, 10, 1, 0);
        //r.fillRect(gc.getInput().getMouseX()-50, gc.getInput().getMouseY()-100, 100, 100, 0xffffccff);
    }
}