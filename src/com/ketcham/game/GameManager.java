package com.ketcham.game;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.ketcham.engine.AbstractGame;
import com.ketcham.engine.GameContainer;
import com.ketcham.engine.Renderer;
import com.ketcham.engine.gfx.Image;
import com.ketcham.engine.gfx.Light;
import com.ketcham.engine.sfx.SoundClip;

public class GameManager extends AbstractGame
{
	private Image terrain;
	private Image log;
	private Light mouseLight;
    private Light cornerLight;
    private SoundClip clip;

	public static void main(String[] args)
	{
		GameContainer gc = new GameContainer(new GameManager(),
                "My Game Engine",
                480,
                360,
                3f,
                true,
                100.0);
		gc.start();
	}

	public GameManager()
	{
		mouseLight = new Light(300, 0xffffffff);
        cornerLight = new Light(200, 0xffffffff);
		terrain = new Image("/terrain.png", false);
		log = new Image("/log.png", true);
		log.setLightBlock(Light.BLOCK_FULL);
		clip = new SoundClip("/audio/test_sound.wav");
		clip.setVolume(-20);
	}

	@Override
	public void update(GameContainer gc)
	{
		if (gc.getInput().isButtonDown(MouseEvent.BUTTON1))
		{
			clip.play();
		}
		if (gc.getInput().isKey(KeyEvent.VK_ESCAPE))
		{
			gc.stop();
		}
	}

	@Override
	public void render(GameContainer gc, Renderer r)
	{
		r.setZDepth(0);
		r.drawImage(terrain, 0, 0);
        r.setZDepth(1);
        r.drawLight(cornerLight, 15, 15);
        r.setZDepth(2);
        r.drawImage(log, 64, 64);
        r.setZDepth(3);
        r.drawLight(mouseLight, gc.getInput().getMouseX(), gc.getInput().getMouseY());
	}
	// TODO: Allow updating of information such as size, framerate, unlocked framerate, title, etc. during runtime
}