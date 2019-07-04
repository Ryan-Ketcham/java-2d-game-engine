package com.ketcham.engine.gfx;

public class ImageTile extends Image
{
	private int tileWidth, tileHeight;

	public ImageTile(String path, boolean alpha, int tileWidth, int tileHeight)
	{
		super(path, alpha);
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
	}

	public Image getTileImage(int tileX, int tileY)
	{
		int[] pixels = new int[tileWidth * tileHeight];

		for (int y = 0; y < tileHeight; y++)
		{
			for (int x = 0; x < tileWidth; x++)
			{
				pixels[x + y * tileWidth] = this.getPixels()[(x + tileX * tileWidth) + (y + tileY * tileHeight) * this.getWidth()];
			}
		}
		return new Image(pixels, tileWidth, tileHeight, isAlpha());
	}

	public int getTileWidth()
	{
		return tileWidth;
	}

	public int getTileHeight()
	{
		return tileHeight;
	}
}
