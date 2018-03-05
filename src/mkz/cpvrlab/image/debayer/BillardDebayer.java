package mkz.cpvrlab.image.debayer;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.plugin.PNG_Writer;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import mkz.cpvrlab.image.common.RgbUtils;
import mkz.cpvrlab.image.debayer.processor.DebayerProcessor;

@SuppressWarnings("unused")
public class BillardDebayer implements PlugInFilter {

	@Override
    public int setup(String arg, ImagePlus imp)
    {   return DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip1)
    {
    	int w1 = ip1.getWidth();
        int h1 = ip1.getHeight();
        byte[] pix1 = (byte[]) ip1.getPixels();
        System.out.println("Bytes length: "+pix1.length);

        System.out.println("l/h: "+pix1.length/ip1.getHeight());
        
        ImagePlus imgGray = NewImage.createByteImage("GrayDeBayered", w1/2, h1/2, 1, NewImage.FILL_RANDOM);
        ImageProcessor ipGray = imgGray.getProcessor();
        byte[] pixGray = (byte[]) ipGray.getPixels();
        int w2 = ipGray.getWidth();
        int h2 = ipGray.getHeight();
        
        ImagePlus imgRGB = NewImage.createRGBImage("RGBDeBayered", w1/2, h1/2, 1, NewImage.FILL_RANDOM);
        ImageProcessor ipRGB = imgRGB.getProcessor();
        System.out.println(String.format("GetPixel returns: 0x%08X", ipRGB.getPixel(0,0)));
        System.out.println(String.format("GetPixel returns: 0x%08X", ipRGB.getPixel(0,1)));
        System.out.println(String.format("GetPixel returns: 0x%08X", ipRGB.getPixel(0,2)));
        int[] pixRGB = (int[]) ipRGB.getPixels();
        
        long msStart = System.currentTimeMillis();
        
        ImagePlus imgHue = NewImage.createByteImage("Hue", w1/2, h1/2, 1, NewImage.FILL_BLACK);
        ImageProcessor ipHue = imgHue.getProcessor();
        byte[] pixHue = (byte[]) ipHue.getPixels();
        
        int[] pix = getInts(pix1);
        
        // Works:
        DebayerProcessor proc = new DebayerProcessor(
        		/* r = */ DebayerProcessor.ARITHMETIC_MEAN_HORIZONTAL, 
        		/* g = */ DebayerProcessor.ARITHMETIC_MEAN_DIAGONAL, 
        		/* b = */ DebayerProcessor.ARITHMETIC_MEAN_VERTICAL);
        int[] outRgb = proc.process(pix, ip1.getWidth(), true, true);
        ipRGB.setPixels(outRgb);
        
        byte[] outGray = new byte[outRgb.length];
        for (int i = 0; i < outGray.length; i++) {
        	//outGray[i] = RgbUtils.getGrayscaleAverage(outRgb[i]);
        	outGray[i] = RgbUtils.getGrayscaleLuminosity(outRgb[i]);
        }
        ipGray.setPixels(outGray);
        
        byte[] outHue = new byte[outRgb.length];
        for (int i = 0; i < outGray.length; i++) {
        	outHue[i] = RgbUtils.getHue(outRgb[i]);
        }
        ipHue.setPixels(outHue);

        long ms = System.currentTimeMillis() - msStart;
        System.out.println(ms);
        ImageStatistics stats = ipGray.getStatistics();
        System.out.println("Mean:" + stats.mean);
        
        PNG_Writer png = new PNG_Writer();
        try
        {   png.writeImage(imgRGB , "out/Billard1024x544x3.png",  0);
            png.writeImage(imgHue,  "out/Billard1024x544x1H.png", 0);
            png.writeImage(imgGray, "out/Billard1024x544x1B.png", 0);
            
        } catch (Exception e)
        {   e.printStackTrace();
        }
        
        imgGray.show();
        imgGray.updateAndDraw();
        imgRGB.show();
        imgRGB.updateAndDraw();
        imgHue.show();
        imgHue.updateAndDraw();
    }
    
    private static int[] getInts(byte[] b) {
    	int[] ret = new int[b.length];
    	for (int i = 0; i < b.length; i++) {
    		ret[i] = b[i] & 0xFF;
    	}
    	return ret;
    }
    
    public static void main(String[] args)
    {
    	BillardDebayer plugin = new BillardDebayer();

        ImagePlus im = new ImagePlus("res/Billard2048x1088x1.png");
        im.show();
        plugin.setup("", im);
        plugin.run(im.getProcessor());
    }
    
    
}
