package mkz.cpvrlab.image.common;

import java.awt.Color;

public class RgbUtils {
	
	public static int[] toRgb(int rgb) {
		int[] ret = new int[3];
		ret[0] = (rgb >> 16) & 0x000000FF;
		ret[1] = (rgb >> 8) & 0x000000FF;
		ret[2] = rgb & 0x000000FF;
		return ret;
	}

	public static byte getGrayscaleAverage(int rgb) {
		int[] split = toRgb(rgb);
		return (byte)((split[0] + split[1] + split[2])/3.0);
	}
	
	public static byte getGrayscaleLuminosity(int rgb) {
		int[] split = toRgb(rgb);
		return (byte)(split[0]*0.21 + split[1]*0.71 + split[2]*0.07);
	}
	
	// Lazy impl using awt.color
	public static byte getHue(int rgb) {
		int[] split = toRgb(rgb);
		float[] hsb = Color.RGBtoHSB(split[0], split[1], split[2], null);
		return (byte)(hsb[0]*255);
	}
	
	public static void main(String[] args) {
		// rgb = AARRGGBB
		int rgb = 0xFFABCDEF;

		int[] split = toRgb(rgb);
        System.out.println(String.format("Red: 0x%08X", split[0]));
        System.out.println(String.format("Green: 0x%08X", split[1]));
        System.out.println(String.format("Blue: 0x%08X", split[2]));
        System.out.println(String.format("Average: %d", getGrayscaleAverage(rgb)));
        System.out.println(String.format("Luminosity: %d", getGrayscaleLuminosity(rgb)));
        float[] hsb = Color.RGBtoHSB(split[0], split[1], split[2], null);
        System.out.println(String.format("Hue: %d", getHue(rgb)));
        System.out.println("Hue from Color: " + (byte)(hsb[0]*255));
	}
}
