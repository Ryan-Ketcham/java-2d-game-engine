package com.ketcham.engine.gfx;

public class ImageRequest
{
	private Image image;
	private int zDepth;
	private int offX, offY;

	public ImageRequest(Image image, int zDepth, int offX, int offY)
	{
		this.image = image;
		this.zDepth = zDepth;
		this.offX = offX;
		this.offY = offY;
	}

	public Image getImage()
	{
		return image;
	}

	public int getzDepth()
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
