package mkz.cpvrlab.image.common;

public class StdUtils {
	
	public static final int max(int... values) {
		int max = 0;
		for (int v: values) {
			if (max < v) max = v;
		}
		return max;
	}

	public static final int min(int... values) {
		int min = Integer.MAX_VALUE;
		for (int v: values) {
			if (min > v) min = v;
		}
		return min;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
