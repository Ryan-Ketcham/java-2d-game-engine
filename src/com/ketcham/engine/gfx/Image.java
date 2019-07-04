package com.ketcham.engine.gfx;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Image
{
    private int width, height;
    private int[] p;

    public Image(String path)
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
        p = image.getRGB(0, 0, width, height, null, 0, width);

        image.flush();
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
        return p;
    }
}