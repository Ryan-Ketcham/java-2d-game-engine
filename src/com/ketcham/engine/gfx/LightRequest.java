package com.ketcham.engine.gfx;

public class LightRequest
{
	private Light light;
	private int zDepth;
	private int offX, offY;

	public LightRequest(Light light, int zDepth, int offX, int offY)
	{
		this.light = light;
		this.zDepth = zDepth;
		this.offX = offX;
		this.offY = offY;
	}

	public Light getLight()
	{
		return light;
	}

	public int getZDepth()
	{
		return zDepth;
	}

	public int getOffX()
	{
		return offX;
	}

	public int getOffY()
	{
		return offY;
	}
}
