package com.ketcham.engine;

import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Collections;

import com.ketcham.engine.gfx.Font;
import com.ketcham.engine.gfx.Image;
import com.ketcham.engine.gfx.ImageRequest;
import com.ketcham.engine.gfx.ImageTile;

public class Renderer
{
	private Font font = Font.STANDARD;
	private ArrayList<ImageRequest> imageRequests;

	private int maxWidth, maxHeight;
	private int[] pixels;
	private int[] zBuffer;

	private int zDepth = 0;
	private boolean processing = false;

	public Renderer(GameContainer gc)
	{
		maxWidth = gc.getWidth();
		maxHeight = gc.getHeight();
		pixels = ((DataBufferInt) gc.getWindow().getImage().getRaster().getDataBuffer()).getData();
		zBuffer = new int[pixels.length];
		imageRequests = new ArrayList<ImageRequest>();
	}

	public void clear()
	{
		for (int i = 0; i < pixels.length; i++)
		{
			pixels[i] = 0;
			zBuffer[i] = 0;
		}
	}

	public void process()
	{
		processing = true;

		Collections.sort(imageRequests, (o1, o2) ->
		{
			if (o1.getzDepth() < o2.getzDepth())
				return -1;
			if (o1.getzDepth() > o2.getzDepth())
				return 1;
			return 0;
		});

		for (ImageRequest request : imageRequests)
		{
			setZDepth(request.getzDepth());
			drawImage(request.getImage(), request.getOffX(), request.getOffY());
		}

		imageRequests.clear();
		processing = false;
	}

	private void setPixel(int x, int y, int value)
	{
		int alpha = ((value >> 24) & 0xff);

		// Check if the pixel is off screen or fully transparent
		if ((x < 0 || x >= maxWidth || y < 0 || y >= maxHeight) || alpha == 0)
			return;

		int index = x + y * maxWidth;

		if (zBuffer[index] > zDepth)
			return;

		if (alpha == 255)
		{
			pixels[index] = value;
		}
		else
		{
			int pixelColor = pixels[index];

			int newRed = ((pixelColor >> 16) & 0xff) - (int) ((((pixelColor >> 16) & 0xff) - ((value >> 16) & 0xff)) * (alpha / 255f));
			int newGreen = ((pixelColor >> 8) & 0xff) - (int) ((((pixelColor >> 8) & 0xff) - ((value >> 8) & 0xff)) * (alpha / 255f));
			int newBlue = (pixelColor & 0xff) - (int) (((pixelColor & 0xff) - (value & 0xff)) * (alpha / 255f));

			pixels[index] = (255 << 24 | newRed << 16 | newGreen << 8 | newBlue);
		}
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
				for (int x = 0; x < font.getWidths()[unicode]; x++)
				{
					if (font.getFontImage().getPixels()[(x + font.getOffsets()[unicode]) + y * font.getFontImage().getWidth()] == 0xffffffff)
					{
						setPixel(x + offX + offset, y + offY, color);
					}
				}
			}
			offset += font.getWidths()[unicode];
		}
	}

	public void drawImage(Image image, int offX, int offY)
	{
		if (image.isAlpha() && !processing)
		{
			imageRequests.add(new ImageRequest(image, zDepth, offX, offY));
			return;
		}
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
		if (image.isAlpha() && !processing)
		{
			imageRequests.add(new ImageRequest(image.getTileImage(tileX, tileY), zDepth, offX, offY));
			return;
		}
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
		for (int y = newY; y <= newHeight; y++)
		{
			for (int x = newX; x < newWidth; x++)
			{
				setPixel(x + offX, y + offY, image.getPixels()[(x + tileX * image.getTileWidth()) + (y + tileY * image.getTileHeight()) * image.getWidth()]);
			}
		}
	}

	public void drawRect(int offX, int offY, int width, int height, int color)
	{
		// Subtract 1 from both since this is 0-based
		width -= 1;
		height -= 1;

		// Loop through top/bottom edges
		for (int y = 0; y <= height; y++)
		{
			setPixel(offX, y + offY, color);
			setPixel(offX + width, y + offY, color);
		}

		// Loop through left/right edges
		for (int x = 0; x <= width; x++)
		{
			setPixel(x + offX, offY, color);
			setPixel(x + offX, offY + height, color);
		}
	}


	public void fillRect(int offX, int offY, int width, int height, int color)
	{
		// Completely off-screen (don't render at all)
		if (offX < -width)
			return;
		if (offY < -height)
			return;
		if (offX >= maxWidth)
			return;
		if (offY >= maxHeight)
			return;

		int newX = 0;
		int newY = 0;
		int newWidth = width;
		int newHeight = height;

		// Partially off-screen (render a portion)
		if (offX < 0)
			newX -= offX;
		if (offY < 0)
			newY -= offY;
		if (newWidth + offX >= maxWidth)
			newWidth -= newWidth + offX - maxWidth;
		if (newHeight + offY >= maxHeight)
			newHeight -= newHeight + offY - maxHeight;

		for (int y = newY; y < newHeight; y++)
		{
			for (int x = newX; x < newWidth; x++)
			{
				setPixel(x + offX, y + offY, color);
			}
		}
	}

	public int getZDepth()
	{
		return zDepth;
	}

	public void setZDepth(int zDepth)
	{
		this.zDepth = zDepth;
	}
}