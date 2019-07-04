package com.ketcham.engine.gfx;

public class Font
{
	public static final Font STANDARD = new Font("/fonts/font.png");
	private static final int NUM_CHARACTERS = 59;
	private final static int CHARACTER_START_COLOR = 0xff0000ff;
	private final static int CHARACTER_END_COLOR = 0xffffff00;

	private Image fontImage;

	private int[] offsets;
	private int[] widths;

	public Font(String path)
	{
		fontImage = new Image(path);

		offsets = new int[NUM_CHARACTERS];
		widths = new int[NUM_CHARACTERS];

		int unicode = 0;
		for (int i = 0; i < fontImage.getWidth(); i++)
		{
			if (fontImage.getPixels()[i] == CHARACTER_START_COLOR)
			{
				offsets[unicode] = i;
			}
			if (fontImage.getPixels()[i] == CHARACTER_END_COLOR)
			{
				widths[unicode] = i - offsets[unicode];
				unicode++;
			}
		}
	}

	public Image getFontImage()
	{
		return fontImage;
	}

	public int[] getOffsets()
	{
		return offsets;
	}

	public int[] getWidths()
	{
		return widths;
	}
}
