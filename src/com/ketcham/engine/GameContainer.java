package com.ketcham.engine;

public class GameContainer implements Runnable
{
	private Window window;
	private Renderer renderer;
	private Input input;
	private AbstractGame game;
	private Thread thread;

	private boolean running = false;

	private int width = 340, height = 280;
	private float scale = 3f;
	private String title = "My Game Engine";
	private boolean unlockFramerate = true;
	private double maxFramerate = 100.0;
	private double UPDATE_CAP;

	public GameContainer(AbstractGame game, String title, int width, int height, float scale, boolean unlockFramerate, double maxFramerate)
	{
		this.game = game;
		this.title = title;
		this.width = width;
		this.height = height;
		this.scale = scale;
		this.unlockFramerate = unlockFramerate;
		this.maxFramerate = maxFramerate;
		this.UPDATE_CAP = 1.0 / maxFramerate;
	}

	public void start()
	{
		window = new Window(this);
		renderer = new Renderer(this);
		input = new Input(this);
		thread = new Thread(this);
		thread.run();
	}

	public void stop()
	{
		dispose();
	}

	public void run()
	{
		running = true;
		double firstTime = 0;
		double lastTime = System.nanoTime() / 1000000000.0;
		double passedTime = 0;
		double unprocessedTime = 0;

		double frameTime = 0.0;
		int frames = 0;
		int fps = 0;
		while (running)
		{
			boolean render = unlockFramerate;

			firstTime = System.nanoTime() / 1000000000.0;
			passedTime = firstTime - lastTime;
			lastTime = firstTime;

			unprocessedTime += passedTime;
			frameTime += passedTime;

			while (unprocessedTime >= UPDATE_CAP)
			{
				unprocessedTime -= UPDATE_CAP;
				render = true;

				game.update(this);

				input.update();

				if (frameTime >= 1.0)
				{
					frameTime = 0;
					fps = frames;
					frames = 0;
				}
			}
			if (render)
			{
				frames++;
				renderer.clear();
				game.render(this, renderer);
				renderer.process();
				renderer.drawText("FPS: " + fps, 0, 0, 0xff00ffff);
				window.update();
			}
			else
			{
				try
				{
					Thread.sleep(1);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}

		dispose();
	}

	private void dispose()
	{
		try
		{
			window.dispose();
			thread.join();
			running = false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public int getHeight() { return height; }

	public int getWidth() { return width; }

	public float getScale()	{ return scale; }

	public String getTitle() { return title; }

	public Window getWindow() { return window; }

	public Input getInput()	{ return input;	}

	public boolean isUnlockFramerate() { return unlockFramerate; }

	public void setUnlockFramerate(boolean unlockFramerate) { this.unlockFramerate = unlockFramerate; }

	public double getMaxFramerate()	{ return maxFramerate; }

	public void setMaxFramerate(double maxFramerate)
	{
		this.maxFramerate = maxFramerate; this.UPDATE_CAP = 1.0 / maxFramerate;
	}

	public void setWidth(int width) { this.width = width; }

	public void setHeight(int height) { this.height = height; }

	public void setScale(float scale) { this.scale = scale; }

	public void setTitle(String title) { this.title = title; }
}