import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.text.TextWindow;

import java.util.Set;
import java.util.TreeSet;

public class Green_Component_Measurements implements PlugInFilter {
    private Set<Integer> uniqueColors = new TreeSet<>();
    private int[] count = new int[256];
    private int[] min = new int[256];
    private int[] max = new int[256];
    private long[] totalRbMean = new long[256];
    private long[] totalRbMeanSquared = new long[256];

    private int totalSlices;
    private int currentSlice;

    public int setup(String args, ImagePlus imp) {
        for (int i = 0; i < 256; i++) {
            min[i] = Integer.MAX_VALUE;
            max[i] = Integer.MIN_VALUE;
        }
        totalSlices = imp.getStackSize();
        currentSlice = 0;
        return DOES_RGB | DOES_STACKS;
    }

    public void addColor(int color) {
        if ((color & 0xFFFFFF) != 0) {
            uniqueColors.add(color);
        }
    }

    public void run(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] pixel = ip.getPixel(x, y, null);
                int r = pixel[0];
                int g = pixel[1];
                int b = pixel[2];
                int color = (r << 16) | (g << 8) | b;
                addColor(color);
            }
        }

        for (int color : uniqueColors) {
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;

            int rbMean = (r + b) / 2;
            int rbMeanSquared = rbMean * rbMean;

            count[g]++;
            min[g] = Math.min(min[g], rbMean);
            max[g] = Math.max(max[g], rbMean);
            totalRbMean[g] += rbMean;
            totalRbMeanSquared[g] += rbMeanSquared;
        }

        uniqueColors.clear();
        currentSlice++;
        if (currentSlice == totalSlices) {
            showResults();
        }
    }

    private void showResults() {
        int[] mean = new int[256];
        int[] mean2 = new int[256];
        for (int i = 0; i < 256; i++) {
            if (count[i] > 0) {
                mean[i] = (int) (totalRbMean[i] / count[i]);
                mean2[i] = (int) (totalRbMeanSquared[i] / count[i]);
            } else {
                min[i] = 0;
                max[i] = 0;
                mean[i] = 0;
                mean2[i] = 0;
            }
        }

        StringBuilder output = new StringBuilder();
        output.append("G\tCount\tMin\tMax\tMean\tMean2\n");
        for (int i = 0; i < 256; i++) {
            output.append(String.format("%d\t%d\t%d\t%d\t%d\t%d\n",
                    i, count[i], (min[i] == Integer.MAX_VALUE ? 0 : min[i]),
                    (max[i] == Integer.MIN_VALUE ? 0 : max[i]), mean[i], mean2[i]));
        }

        new TextWindow("Green Component Analysis", output.toString(), 800, 600);
    }
}

