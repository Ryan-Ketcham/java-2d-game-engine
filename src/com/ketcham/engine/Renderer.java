package com.ketcham.engine;

import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Collections;

import com.ketcham.engine.gfx.*;

public class Renderer
{
	private Font font = Font.STANDARD;
	private ArrayList<ImageRequest> imageRequests = new ArrayList<ImageRequest>();
	private ArrayList<LightRequest> lightRequests = new ArrayList<LightRequest>();

	private int maxWidth, maxHeight;
	private int[] pixels;
	private int[] zBuffer;
	private int[] lightMap;
	private int[] lightBlocks;

	private int ambientColor = 0xff242424;
	private int zDepth = 0;
	private boolean processing = false;

	public Renderer(GameContainer gc)
	{
		maxWidth = gc.getWidth();
		maxHeight = gc.getHeight();
		pixels = ((DataBufferInt) gc.getWindow().getImage().getRaster().getDataBuffer()).getData();
		zBuffer = new int[pixels.length];
		lightMap = new int[pixels.length];
		lightBlocks = new int[pixels.length];
	}

	public void clear()
	{
		for (int i = 0; i < pixels.length; i++)
		{
			pixels[i] = 0;
			zBuffer[i] = 0;
			lightMap[i] = ambientColor;
			lightBlocks[i] = 0;
		}
	}

	public void process()
	{
		processing = true;

		Collections.sort(imageRequests, (o1, o2) ->
		{
			if (o1.getZDepth() < o2.getZDepth())
				return -1;
			if (o1.getZDepth() > o2.getZDepth())
				return 1;
			return 0;
		});

		// Draw Alpha images
		for (ImageRequest request : imageRequests)
		{
			setZDepth(request.getZDepth());
			drawImage(request.getImage(), request.getOffX(), request.getOffY());
		}

		// Draw lighting
		for (LightRequest request : lightRequests)
		{
			setZDepth(request.getZDepth());
			drawLight(request.getLight(), request.getOffX(), request.getOffY());
		}

		for (int i = 0; i < pixels.length; i++)
		{
			float r = ((lightMap[i] >> 16) & 0xff) / 255f;
			float g = ((lightMap[i] >> 8) & 0xff) / 255f;
			float b = (lightMap[i] & 0xff) / 255f;

			pixels[i] = ((int) (((pixels[i] >> 16) & 0xff) * r)) << 16 | ((int) (((pixels[i] >> 8) & 0xff) * g)) << 8 | (int) ((pixels[i] & 0xff) * b);
		}

		imageRequests.clear();
		lightRequests.clear();
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

		zBuffer[index] = zDepth;

		if (alpha == 255)
		{
			pixels[index] = value;
		}
		else
		{
			int baseColor = pixels[index];

			int newRed = ((baseColor >> 16) & 0xff) - (int) ((((baseColor >> 16) & 0xff) - ((value >> 16) & 0xff)) * (alpha / 255f));
			int newGreen = ((baseColor >> 8) & 0xff) - (int) ((((baseColor >> 8) & 0xff) - ((value >> 8) & 0xff)) * (alpha / 255f));
			int newBlue = (baseColor & 0xff) - (int) (((baseColor & 0xff) - (value & 0xff)) * (alpha / 255f));

			pixels[index] = newRed << 16 | newGreen << 8 | newBlue;
		}
	}

	public void setLightMap(int x, int y, int value)
	{
		// Check if the pixel is off screen
		if (x < 0 || x >= maxWidth || y < 0 || y >= maxHeight)
			return;

		int baseColor = lightMap[x + y * maxWidth];

		int maxRed = Math.max((baseColor >> 16) & 0xff, (value >> 16) & 0xff);
		int maxGreen = Math.max((baseColor >> 8) & 0xff, (value >> 8) & 0xff);
		int maxBlue = Math.max(baseColor & 0xff, value & 0xff);

		lightMap[x + y * maxWidth] = maxRed << 16 | maxGreen << 8 | maxBlue;
	}

	public void setLightBlock(int x, int y, int value)
	{
		// Check if the pixel is off screen
		if (x < 0 || x >= maxWidth || y < 0 || y >= maxHeight)
			return;

		if (zBuffer[x + y * maxWidth] > zDepth)
			return;

		lightBlocks[x + y * maxWidth] = value;
	}

	public void drawText(String text, int offX, int offY, int color)
	{
		if (Font.IS_UPPER_CASE)
		{
			text = text.toUpperCase();
		}
		Image fontImage = font.getFontImage();
		int offset = 0;

		for (int i = 0; i < text.length(); i++)
		{
			int unicode = text.codePointAt(i) - Font.STARTING_CHARACTER;

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
				setLightBlock(x + offX, y + offY, image.getLightBlock());
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
				setLightBlock(x + offX, y + offY, image.getLightBlock());
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

	public void drawLight(Light light, int offX, int offY)
	{
		if (!processing)
		{
			lightRequests.add(new LightRequest(light, zDepth, offX, offY));
			return;
		}
		for (int i = 0; i <= light.getDiameter(); i++)
		{
			drawLightLine(light, light.getRadius(), light.getRadius(), i, 0, offX, offY);
			drawLightLine(light, light.getRadius(), light.getRadius(), i, light.getDiameter(), offX, offY);
			drawLightLine(light, light.getRadius(), light.getRadius(), 0, i, offX, offY);
			drawLightLine(light, light.getRadius(), light.getRadius(), light.getDiameter(), i, offX, offY);
		}
	}

	private void drawLightLine(Light light, int x0, int y0, int x1, int y1, int offX, int offY)
	{
		// Bresenhams line algorithm?
		int dx = Math.abs(x1 - x0);
		int dy = Math.abs(y1 - y0);

		int sx = x0 < x1 ? 1 : -1;
		int sy = y0 < y1 ? 1 : -1;

		int err = dx - dy;
		int err2;

		while (true)
		{
			int screenX = x0 - light.getRadius() + offX;
			int screenY = y0 - light.getRadius() + offY;

			// Check if light line is off the screen
			if (screenX < 0 || screenX >= maxWidth || screenY < 0 || screenY >= maxHeight)
				return;

			// Don't bother rendering if there is no light
			int lightColor = light.getLightValue(x0, y0);
			if (lightColor == 0)
				return;

			// Don't render if there is a light block in a lower Z-Level
			if (zBuffer[screenX + screenY * maxWidth] >= zDepth && lightBlocks[screenX + screenY * maxWidth] == Light.BLOCK_FULL)
				return;

			setLightMap(screenX, screenY, lightColor);

			if (x0 == x1 && y0 == y1)
				break;

			err2 = 2 * err;
			if (err2 > -1 * dy)
			{
				err -= dy;
				x0 += sx;
			}
			if (err2 < dx)
			{
				err += dx;
				y0 += sy;
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