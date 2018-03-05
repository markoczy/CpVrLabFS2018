package mkz.cpvrlab.image.debayer.processor;

import java.util.function.BiFunction;

public class DebayerProcessor {
	
	private static final int INVALID_POS = -1;
	private static final int TRUNC_NONBYTE = 0x000000FF;
	
	public BiFunction<BayeredImageInfo, Integer, Integer> calcRed = ARITHMETIC_MEAN_VERTICAL;
	public BiFunction<BayeredImageInfo, Integer, Integer> calcGreen = ARITHMETIC_MEAN_DIAGONAL;
	public BiFunction<BayeredImageInfo, Integer, Integer> calcBlue = ARITHMETIC_MEAN_HORIZONTAL;
	
	public DebayerProcessor() {}

	public DebayerProcessor(BiFunction<BayeredImageInfo, Integer, Integer> aCalcRed,
			BiFunction<BayeredImageInfo, Integer, Integer> aCalcGreen, 
			BiFunction<BayeredImageInfo, Integer, Integer> aCalcBlue) {
		calcRed = aCalcRed;
		calcGreen = aCalcGreen;
		calcBlue = aCalcBlue;
	}
	
	public int[] process(int[] data, int width, boolean offsetX, boolean offsetY)  {
		if (data.length % width != 0) throw new RuntimeException("Data size invalid: Row oversized by "+ data.length % width);
		BayeredImageInfo info = new BayeredImageInfo(data, width);
		int[] ret = new int[data.length/4];
		int pos = (offsetX ? 1 : 0) + (offsetY ? width : 0);

		int iRet = 0;
		while (pos < data.length) {
			ret[iRet++] = ((calcRed.apply(info, pos) & TRUNC_NONBYTE) << 16) | 
				((calcGreen.apply(info, pos) & TRUNC_NONBYTE) << 8) | 
				(calcBlue.apply(info, pos) & TRUNC_NONBYTE) |
				0xFF000000;
			
			if (pos%info.width == info.width-1 /* is rightmost */) {
				pos += width + 2;
			}
			else pos += 2;
		}
		System.out.println("Filled positions: "+iRet);
		
		return ret;
	}
	
	public static class BayeredImageInfo {
		public BayeredImageInfo(int[] aData, int aWidth) {
			data = aData;
			width = aWidth;
		}
		private int width;
		private int[] data;
	}

	public static BiFunction<BayeredImageInfo, Integer, Integer> meanOf(
			BiFunction<BayeredImageInfo, Integer, Integer> f1,
			BiFunction<BayeredImageInfo, Integer, Integer> f2) {
		
		return (info, pos) -> {
			int x1 = f1.apply(info, pos);
			int x2 = f2.apply(info, pos);
			if (x1 < 0) return x2;
			if (x2 < 0) return x1;
			return (int)((x1 + x2)/2.0);
		};
	}
	
	public static BiFunction<BayeredImageInfo, Integer, Integer> TOP = (info, pos) -> {
		return (pos + info.width >= info.data.length /* is highest */) ? 
				INVALID_POS : info.data[pos + info.width];
	};
	
	public static BiFunction<BayeredImageInfo, Integer, Integer> BOTTOM = (info, pos) -> {
		return (pos - info.width < 0 /* is lowest */) ? 
				INVALID_POS : info.data[pos - info.width];
	};
	
	public static BiFunction<BayeredImageInfo, Integer, Integer> LEFT = (info, pos) -> {
		return (pos%info.width == 0 /* is leftmost */) ? 
				INVALID_POS : info.data[pos - 1];
	};
	
	public static BiFunction<BayeredImageInfo, Integer, Integer> RIGHT = (info, pos) -> {
		return (pos%info.width == info.width - 1 /* is rightmost */) ? 
				INVALID_POS : info.data[pos + 1];
	};
	
	public static BiFunction<BayeredImageInfo, Integer, Integer> TOP_RIGHT = (info, pos) -> {
		return (pos+info.width >= info.data.length /* is highest */ || 
				pos%info.width >= info.width-1 /* is rightmost */) ? 
						INVALID_POS : info.data[pos + info.width + 1];
	};
	
	public static BiFunction<BayeredImageInfo, Integer, Integer> TOP_LEFT = (info, pos) -> {
		return (pos+info.width >= info.data.length /* is highest */ || 
				pos%info.width==0 /* is leftmost */) ? 
						INVALID_POS : info.data[pos + info.width - 1];
	};
	
	public static BiFunction<BayeredImageInfo, Integer, Integer> BOTTOM_RIGHT = (info, pos) -> {
		return (pos - info.width < 0 /* is lowest */ || 
				pos%info.width >= info.width-1 /* is rightmost */) ? 
						INVALID_POS : info.data[pos - info.width + 1];
	};
	
	public static BiFunction<BayeredImageInfo, Integer, Integer> BOTTOM_LEFT = (info, pos) -> {
		return (pos - info.width < 0 /* is lowest */ || 
				pos%info.width==0 /* is leftmost */) ? 
						INVALID_POS : info.data[pos - info.width - 1];
	};
	
	public static BiFunction<BayeredImageInfo, Integer, Integer> ARITHMETIC_MEAN_HORIZONTAL = 
			meanOf(LEFT, RIGHT);
	public static BiFunction<BayeredImageInfo, Integer, Integer> ARITHMETIC_MEAN_VERTICAL = 
			meanOf(TOP, BOTTOM);
	public static BiFunction<BayeredImageInfo, Integer, Integer> ARITHMETIC_MEAN_STRAIGHT = 
			meanOf(meanOf(TOP, BOTTOM), meanOf(LEFT, RIGHT));
	public static BiFunction<BayeredImageInfo, Integer, Integer> ARITHMETIC_MEAN_DIAG_BOTTOM_TOP = 
			meanOf(BOTTOM_LEFT, TOP_RIGHT);
	public static BiFunction<BayeredImageInfo, Integer, Integer> ARITHMETIC_MEAN_DIAG_TOP_BOTTOM = 
			meanOf(TOP_LEFT, BOTTOM_RIGHT);
	public static BiFunction<BayeredImageInfo, Integer, Integer> ARITHMETIC_MEAN_DIAGONAL = 
			meanOf(meanOf(BOTTOM_LEFT, TOP_RIGHT), meanOf(TOP_LEFT, BOTTOM_RIGHT));
	
	////////////////////////////////////////////////////////////////////////////////
	// Simple unit tests..
	////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args)
    {
    	int[] data = {	1, 2, 3, 
    					4, 5, 6, 
    					7, 8, 9 };
    	
    	BayeredImageInfo info = new BayeredImageInfo(data, 3);
    	
    	System.out.println("Test Positions:");
    	System.out.println(TOP_LEFT.apply(info, 4));
    	System.out.println(TOP.apply(info, 4));
    	System.out.println(TOP_RIGHT.apply(info, 4));
    	System.out.println(LEFT.apply(info, 4));
    	System.out.println(RIGHT.apply(info, 4));
    	System.out.println(BOTTOM_LEFT.apply(info, 4));
    	System.out.println(BOTTOM.apply(info, 4));
    	System.out.println(BOTTOM_RIGHT.apply(info, 4));
    	
    	// All should return 5:
    	//
    	System.out.println("Test Horizontal:");
    	// 0 + 5 = 5
    	System.out.println(ARITHMETIC_MEAN_HORIZONTAL.apply(info, 3));
    	// (4 + 6) / 2 = 5
    	System.out.println(ARITHMETIC_MEAN_HORIZONTAL.apply(info, 4));
    	// 5 + 0 = 5
    	System.out.println(ARITHMETIC_MEAN_HORIZONTAL.apply(info, 5));
    	testFcn(ARITHMETIC_MEAN_HORIZONTAL, info);
    	//
    	System.out.println("Test Vertical:");
    	// 0 + 5 = 5
    	System.out.println(ARITHMETIC_MEAN_VERTICAL.apply(info, 1));
    	// (2 + 8) / 2 = 5
    	System.out.println(ARITHMETIC_MEAN_VERTICAL.apply(info, 4));
    	// 5 + 0 = 5
    	System.out.println(ARITHMETIC_MEAN_VERTICAL.apply(info, 7));
    	testFcn(ARITHMETIC_MEAN_VERTICAL, info);
    	//
    	System.out.println("Test Diag TB:");
    	// 0 + 5 = 5
    	System.out.println(ARITHMETIC_MEAN_DIAG_TOP_BOTTOM.apply(info, 6));
    	// (7 + 3) / 2 = 5
    	System.out.println(ARITHMETIC_MEAN_DIAG_TOP_BOTTOM.apply(info, 4));
    	// 5 + 0 = 5
    	System.out.println(ARITHMETIC_MEAN_DIAG_TOP_BOTTOM.apply(info, 2));
    	testFcn(ARITHMETIC_MEAN_DIAG_TOP_BOTTOM, info);
    	//
    	System.out.println("Test Diag BT:");
    	// 0 + 5 = 5
    	System.out.println(ARITHMETIC_MEAN_DIAG_BOTTOM_TOP.apply(info, 0));
    	// (1 + 9) / 2 = 5
    	System.out.println(ARITHMETIC_MEAN_DIAG_BOTTOM_TOP.apply(info, 4));
    	// 5 + 0 = 5
    	System.out.println(ARITHMETIC_MEAN_DIAG_BOTTOM_TOP.apply(info, 8));
    	testFcn(ARITHMETIC_MEAN_DIAG_BOTTOM_TOP, info);
    }
    
    private static void testFcn(BiFunction<BayeredImageInfo, Integer, Integer> fcn, BayeredImageInfo info) {
    	for (int i = 0; i < info.data.length; i++) {
    		try {
    			fcn.apply(info, i);
			} catch (Exception e) {
				System.err.println("Failed at pos: "+i+", value: "+info.data[i]);
				e.printStackTrace();
			}
    	}
    }
}
