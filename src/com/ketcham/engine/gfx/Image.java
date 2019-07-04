package com.ketcham.engine.gfx;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Image
{
    private int width, height;
    private int[] pixels;

    private boolean alpha;

    public Image(String path, boolean alpha)
    {
        BufferedImage image = null;
        try
        {
            image = ImageIO.read(Image.class.getResourceAsStream(path));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        width = image.getWidth();
        height = image.getHeight();
        pixels = image.getRGB(0, 0, width, height, null, 0, width);
        this.alpha = alpha;

        image.flush();
    }

    public Image(int[] pixels, int width, int height, boolean alpha)
    {
        this.pixels = pixels;
        this.width = width;
        this.height = height;
        this.alpha = alpha;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public int[] getPixels()
    {
        return pixels;
    }

    public boolean isAlpha()
    {
        return alpha;
    }

    public void setAlpha(boolean alpha)
    {
        this.alpha = alpha;
    }

}