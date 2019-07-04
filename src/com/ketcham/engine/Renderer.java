package com.ketcham.engine;

import java.awt.image.DataBufferInt;

import com.ketcham.engine.gfx.Font;
import com.ketcham.engine.gfx.Image;
import com.ketcham.engine.gfx.ImageTile;

public class Renderer
{
	private int maxWidth, maxHeight;
	private int[] p;

	private Font font = Font.STANDARD;

	public Renderer(GameContainer gc)
	{
		maxWidth = gc.getWidth();
		maxHeight = gc.getHeight();
		p = ((DataBufferInt) gc.getWindow().getImage().getRaster().getDataBuffer()).getData();
	}

	public void clear()
	{
		for (int i = 0; i < p.length; i++)
		{
			p[i] = 0;
		}
	}

	public void setPixel(int x, int y, int value)
	{
		if ((x < 0 || x >= maxWidth || y < 0 || y >= maxHeight) || value == 0xffff00ff)
		{
			return;
		}

		p[x + y * maxWidth] = value;
	}

	public void drawText(String text, int offX, int offY, int color)
	{
        text = text.toUpperCase();
	    Image fontImage = font.getFontImage();
		int offset = 0;

		for (int i = 0; i < text.length(); i++)
        {
            int unicode = text.codePointAt(i) - 32; // 32 makes "Space" == 0

            for (int y = 0; y < fontImage.getHeight(); y++)
            {
                for (int x = 0; x < font.getWidths()[unicode]; x++){
                    if (font.getFontImage().getPixels()[(x + font.getOffsets()[unicode]) + y * font.getFontImage().getWidth()] == 0xffffffff)
                    {
                        setPixel(x+offX+offset,y+offY,color);
                    }
                }
            }
            offset += font.getWidths()[unicode];
		}
	}

	public void drawImage(Image image, int offX, int offY)
	{
		// Completely off-screen (don't render at all)
		if (offX < -image.getWidth())
			return;
		if (offY < -image.getHeight())
			return;
		if (offX >= maxWidth)
			return;
		if (offY >= maxHeight)
			return;

		int newX = 0;
		int newY = 0;
		int newWidth = image.getWidth();
		int newHeight = image.getHeight();

		// Partially off-screen (render a portion)
		if (offX < 0)
			newX -= offX;
		if (offY < 0)
			newY -= offY;
		if (newWidth + offX >= maxWidth)
			newWidth -= newWidth + offX - maxWidth;
		if (newHeight + offY >= maxHeight)
			newHeight -= newHeight + offY - maxHeight;

		// Loop through each pixel and set it to the corresponding pixel in the image
		for (int y = newY; y < newHeight; y++)
		{
			for (int x = newX; x < newWidth; x++)
			{
				setPixel(x + offX, y + offY, image.getPixels()[x + y * image.getWidth()]);
			}
		}
	}

	public void drawImageTile(ImageTile image, int offX, int offY, int tileX, int tileY)
	{
		// Completely off-screen (don't render at all)
		if (offX < -image.getTileWidth())
			return;
		if (offY < -image.getTileHeight())
			return;
		if (offX >= maxWidth)
			return;
		if (offY >= maxHeight)
			return;

		int newX = 0;
		int newY = 0;
		int newWidth = image.getTileWidth();
		int newHeight = image.getTileHeight();

		// Partially off-screen (render a portion)
		if (offX < 0)
			newX -= offX;
		if (offY < 0)
			newY -= offY;
		if (newWidth + offX >= maxWidth)
			newWidth -= newWidth + offX - maxWidth;
		if (newHeight + offY >= maxHeight)
			newHeight -= newHeight + offY - maxHeight;

		// Loop through each pixel and set it to the corresponding pixel in the image
		for (int y = newY; y < newHeight; y++)
		{
			for (int x = newX; x < newWidth; x++)
			{
				setPixel(x + offX, y + offY, image.getPixels()[(x + tileX * image.getTileWidth()) + (y + tileY * image.getTileHeight()) * image.getWidth()]);
			}
		}
	}
}