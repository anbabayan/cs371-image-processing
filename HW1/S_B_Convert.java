import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;

public class S_B_Convert implements PlugInFilter {
    public int setup(String args, ImagePlus im) {
        return DOES_RGB + DOES_STACKS;
    }

    public void run(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        float[] hsv = new float[3];

        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                int[] color = ip.getPixel(col, row, null);
                Color.RGBtoHSB(color[0], color[1], color[2], hsv);
                float sbValue = hsv[1] * hsv[2];  // Saturation times Brightness
                int grayValue = (int) (sbValue * 255);
                ip.putPixel(col, row, new int[]{grayValue, grayValue, grayValue});
            }
        }
    }
}
