package ch.ethz.sis.hcscld;

import java.util.Arrays;
import java.util.BitSet;

/**
 * <p>
 * <em>This software has been released into the public domain.
 * <strong>Please read the notes in this source file for additional information.
 * </strong></em>
 * </p>
 * <p>
 * This class provides a configurable implementation of the Canny edge detection algorithm. This
 * classic algorithm has a number of shortcomings, but remains an effective tool in many scenarios.
 * <em>This class is designed
 * for single threaded use only.</em>
 * </p>
 * <p>
 * The original version worked on images, this version has been changed to work on binary masks
 * represented by {@link BitSet}/
 * </p>
 * <p>
 * Sample usage:
 * </p>
 * 
 * <pre>
 * <code>
 * //create the detector
 * CannyEdgeDetector detector = new CannyEdgeDetector();
 * //apply it to an image
 * detector.setSourceMask(mask);
 * detector.process();
 * BitSet edgeMask = detector.getEdgeMask(false);
 * </code>
 * </pre>
 * <p>
 * For a more complete understanding of this edge detector's parameters consult an explanation of
 * the algorithm.
 * </p>
 * 
 * @author Tom Gibara
 */

class CannyEdgeDetector
{

    // statics

    private static final int BLACK_FRAME_PIXELS = 3;

    private final static float GAUSSIAN_CUT_OFF = 0.005f;

    private final static float MAGNITUDE_SCALE = 100F;

    private final static float MAGNITUDE_LIMIT = 1000F;

    private final static int MAGNITUDE_MAX = (int) (MAGNITUDE_SCALE * MAGNITUDE_LIMIT);

    private static final float gaussianKernelRadius = 1f;

    private static final float lowThreshold = 0.5f;

    private static final float highThreshold = 1f;

    private static final int gaussianKernelWidth = 2;

    // fields

    private int height;

    private int width;

    private int picsize;

    private int wbHeight;

    private int wbWidth;

    private int wbPicsize;

    private int[] data;

    private int[] magnitude;

    private BitSet sourceMask;

    private int maskOffset;

    private BitSet edgeMask;

    private float[] xConv;

    private float[] yConv;

    private float[] xGradient;

    private float[] yGradient;

    // constructors

    // accessors

    /**
     * The mask that provides the form used by this detector to generate edges.
     */
    public BitSet getSourceMask()
    {
        return sourceMask;
    }

    /**
     * Specifies the (binary) mask that will provide the data of the form in which edges will be
     * detected.
     * 
     * @param mask The binary mask of the form.
     * @param offset The (bit) offset within the mask where the form starts.
     * @param width The width of the rectangle that contains the form.
     * @param height The height of the rectangle that contains the form.
     */
    public void setSourceMask(BitSet mask, int offset, int width, int height)
    {
        this.sourceMask = mask;
        this.maskOffset = offset;
        this.width = width;
        this.height = height;
        this.picsize = width * height;
        this.wbWidth = width + 2 * BLACK_FRAME_PIXELS;
        this.wbHeight = height + 2 * BLACK_FRAME_PIXELS;
        this.wbPicsize = wbWidth * wbHeight;
    }

    /**
     * Return the (binary) mask of the detected edges.
     * 
     * @param thicken If <code>true</code>, the edges will be made thicker so that they are easier
     *            to see when visualized.
     */
    public BitSet getEdgeMask(boolean thicken)
    {
        thresholdEdgesToBinaryMask(thicken);
        return edgeMask;
    }

    // worker methods

    public void process()
    {
        initArrays();
        fillWorkingBuffer();
        computeGradients(gaussianKernelRadius, gaussianKernelWidth);
        int low = Math.round(lowThreshold * MAGNITUDE_SCALE);
        int high = Math.round(highThreshold * MAGNITUDE_SCALE);
        performHysteresis(low, high);
    }

    // private utility methods

    private void initArrays()
    {
        if (data == null || wbPicsize != data.length)
        {
            data = new int[wbPicsize];
            magnitude = new int[wbPicsize];

            xConv = new float[wbPicsize];
            yConv = new float[wbPicsize];
            xGradient = new float[wbPicsize];
            yGradient = new float[wbPicsize];
        }
    }

    // NOTE: The elements of the method below (specifically the technique for
    // non-maximal suppression and the technique for gradient computation)
    // are derived from an implementation posted in the following forum (with the
    // clear intent of others using the code):
    // http://forum.java.sun.com/thread.jspa?threadID=546211&start=45&tstart=0
    // My code effectively mimics the algorithm exhibited above.
    // Since I don't know the providence of the code that was posted it is a
    // possibility (though I think a very remote one) that this code violates
    // someone's intellectual property rights. If this concerns you feel free to
    // contact me for an alternative, though less efficient, implementation.

    private void computeGradients(float kernelRadius, int kernelWidth)
    {

        // generate the gaussian convolution masks
        float kernel[] = new float[kernelWidth];
        float diffKernel[] = new float[kernelWidth];
        int kwidth;
        for (kwidth = 0; kwidth < kernelWidth; kwidth++)
        {
            float g1 = gaussian(kwidth, kernelRadius);
            if (g1 <= GAUSSIAN_CUT_OFF && kwidth >= 2)
                break;
            float g2 = gaussian(kwidth - 0.5f, kernelRadius);
            float g3 = gaussian(kwidth + 0.5f, kernelRadius);
            kernel[kwidth] =
                    (g1 + g2 + g3) / 3f / (2f * (float) Math.PI * kernelRadius * kernelRadius);
            diffKernel[kwidth] = g3 - g2;
        }

        int initX = kwidth - 1;
        int maxX = wbWidth - (kwidth - 1);
        int initY = wbWidth * (kwidth - 1);
        int maxY = wbWidth * (wbHeight - (kwidth - 1));

        // perform convolution in x and y directions
        for (int x = initX; x < maxX; x++)
        {
            for (int y = initY; y < maxY; y += wbWidth)
            {
                int index = x + y;
                float sumX = data[index] * kernel[0];
                float sumY = sumX;
                int xOffset = 1;
                int yOffset = wbWidth;
                for (; xOffset < kwidth;)
                {
                    sumY += kernel[xOffset] * (data[index - yOffset] + data[index + yOffset]);
                    sumX += kernel[xOffset] * (data[index - xOffset] + data[index + xOffset]);
                    yOffset += wbWidth;
                    xOffset++;
                }

                yConv[index] = sumY;
                xConv[index] = sumX;
            }

        }

        for (int x = initX; x < maxX; x++)
        {
            for (int y = initY; y < maxY; y += wbWidth)
            {
                float sum = 0f;
                int index = x + y;
                for (int i = 1; i < kwidth; i++)
                    sum += diffKernel[i] * (yConv[index - i] - yConv[index + i]);

                xGradient[index] = sum;
            }

        }

        for (int x = kwidth; x < wbWidth - kwidth; x++)
        {
            for (int y = initY; y < maxY; y += wbWidth)
            {
                float sum = 0.0f;
                int index = x + y;
                int yOffset = wbWidth;
                for (int i = 1; i < kwidth; i++)
                {
                    sum += diffKernel[i] * (xConv[index - yOffset] - xConv[index + yOffset]);
                    yOffset += wbWidth;
                }

                yGradient[index] = sum;
            }

        }

        initX = kwidth;
        maxX = wbWidth - kwidth;
        initY = wbWidth * kwidth;
        maxY = wbWidth * (wbHeight - kwidth);
        for (int x = initX; x < maxX; x++)
        {
            for (int y = initY; y < maxY; y += wbWidth)
            {
                int index = x + y;
                int indexN = index - wbWidth;
                int indexS = index + wbWidth;
                int indexW = index - 1;
                int indexE = index + 1;
                int indexNW = indexN - 1;
                int indexNE = indexN + 1;
                int indexSW = indexS - 1;
                int indexSE = indexS + 1;

                float xGrad = xGradient[index];
                float yGrad = yGradient[index];
                float gradMag = hypot(xGrad, yGrad);

                // perform non-maximal supression
                float nMag = hypot(xGradient[indexN], yGradient[indexN]);
                float sMag = hypot(xGradient[indexS], yGradient[indexS]);
                float wMag = hypot(xGradient[indexW], yGradient[indexW]);
                float eMag = hypot(xGradient[indexE], yGradient[indexE]);
                float neMag = hypot(xGradient[indexNE], yGradient[indexNE]);
                float seMag = hypot(xGradient[indexSE], yGradient[indexSE]);
                float swMag = hypot(xGradient[indexSW], yGradient[indexSW]);
                float nwMag = hypot(xGradient[indexNW], yGradient[indexNW]);
                float tmp;
                /*
                 * An explanation of what's happening here, for those who want to understand the
                 * source: This performs the "non-maximal supression" phase of the Canny edge
                 * detection in which we need to compare the gradient magnitude to that in the
                 * direction of the gradient; only if the value is a local maximum do we consider
                 * the point as an edge candidate. We need to break the comparison into a number of
                 * different cases depending on the gradient direction so that the appropriate
                 * values can be used. To avoid computing the gradient direction, we use two simple
                 * comparisons: first we check that the partial derivatives have the same sign (1)
                 * and then we check which is larger (2). As a consequence, we have reduced the
                 * problem to one of four identical cases that each test the central gradient
                 * magnitude against the values at two points with 'identical support'; what this
                 * means is that the geometry required to accurately interpolate the magnitude of
                 * gradient function at those points has an identical geometry (upto
                 * right-angled-rotation/reflection). When comparing the central gradient to the two
                 * interpolated values, we avoid performing any divisions by multiplying both sides
                 * of each inequality by the greater of the two partial derivatives. The common
                 * comparand is stored in a temporary variable (3) and reused in the mirror case
                 * (4).
                 */
                if (xGrad * yGrad <= 0 /* (1) */
                ? Math.abs(xGrad) >= Math.abs(yGrad) /* (2) */
                ? (tmp = Math.abs(xGrad * gradMag)) >= Math.abs(yGrad * neMag - (xGrad + yGrad)
                        * eMag) /* (3) */
                        && tmp > Math.abs(yGrad * swMag - (xGrad + yGrad) * wMag) /* (4) */
                : (tmp = Math.abs(yGrad * gradMag)) >= Math.abs(xGrad * neMag - (yGrad + xGrad)
                        * nMag) /* (3) */
                        && tmp > Math.abs(xGrad * swMag - (yGrad + xGrad) * sMag) /* (4) */
                : Math.abs(xGrad) >= Math.abs(yGrad) /* (2) */
                ? (tmp = Math.abs(xGrad * gradMag)) >= Math.abs(yGrad * seMag + (xGrad - yGrad)
                        * eMag) /* (3) */
                        && tmp > Math.abs(yGrad * nwMag + (xGrad - yGrad) * wMag) /* (4) */
                : (tmp = Math.abs(yGrad * gradMag)) >= Math.abs(xGrad * seMag + (yGrad - xGrad)
                        * sMag) /* (3) */
                        && tmp > Math.abs(xGrad * nwMag + (yGrad - xGrad) * nMag) /* (4) */
                )
                {
                    magnitude[index] =
                            gradMag >= MAGNITUDE_LIMIT ? MAGNITUDE_MAX
                                    : (int) (MAGNITUDE_SCALE * gradMag);
                    // NOTE: The orientation of the edge is not employed by this
                    // implementation. It is a simple matter to compute it at
                    // this point as: Math.atan2(yGrad, xGrad);
                } else
                {
                    magnitude[index] = 0;
                }
            }
        }
    }

    // NOTE: It is quite feasible to replace the implementation of this method
    // with one which only loosely approximates the hypot function. I've tested
    // simple approximations such as Math.abs(x) + Math.abs(y) and they work fine.
    private float hypot(float x, float y)
    {
        return (float) Math.hypot(x, y);
    }

    private float gaussian(float x, float sigma)
    {
        return (float) Math.exp(-(x * x) / (2f * sigma * sigma));
    }

    private void performHysteresis(int low, int high)
    {
        // NOTE: this implementation reuses the data array to store both
        // luminance data from the image, and edge intensity from the processing.
        // This is done for memory efficiency, other implementations may wish
        // to separate these functions.
        Arrays.fill(data, 0);

        int offset = 0;
        for (int y = 0; y < wbHeight; y++)
        {
            for (int x = 0; x < wbWidth; x++)
            {
                if (data[offset] == 0 && magnitude[offset] >= high)
                {
                    follow(x, y, offset, low);
                }
                offset++;
            }
        }
        // Treat last line especially to not loose edges there
        for (int x = 0; x < width; ++x)
        {
            data[getWbIndex(x, height - 1)] += data[getWbIndex(x, height)];
        }
    }

    private void follow(int x1, int y1, int i1, int threshold)
    {
        int x0 = x1 == 0 ? x1 : x1 - 1;
        int x2 = x1 == wbWidth - 1 ? x1 : x1 + 1;
        int y0 = y1 == 0 ? y1 : y1 - 1;
        int y2 = y1 == wbHeight - 1 ? y1 : y1 + 1;

        data[i1] = magnitude[i1];
        for (int x = x0; x <= x2; x++)
        {
            for (int y = y0; y <= y2; y++)
            {
                int i2 = x + y * wbWidth;
                if ((y != y1 || x != x1) && data[i2] == 0 && magnitude[i2] >= threshold)
                {
                    follow(x, y, i2, threshold);
                    return;
                }
            }
        }
    }

    private void thresholdEdgesToBinaryMask(boolean thicken)
    {
        edgeMask = new BitSet(picsize);
        if (thicken)
        {
            for (int x = 0; x < width; ++x)
            {
                for (int y = 0; y < height; ++y)
                {
                    if (data[getWbIndex(x, y)] > 0)
                    {
                        final int dxs = (x + 1 == width) ? -1 : 0;
                        final int dys = (y + 1 == height) ? -1 : 0;
                        for (int dx = dxs; dx <= dxs + 1; ++dx)
                        {
                            for (int dy = dys; dy <= dys + 1; ++dy)
                            {
                                edgeMask.set((x + dx) * height + (y + dy));
                            }
                        }
                    }
                }
            }
        } else
        {
            for (int x = 0; x < width; ++x)
            {
                for (int y = 0; y < height; ++y)
                {
                    if (data[getWbIndex(x, y)] > 0)
                    {
                        edgeMask.set(x * height + y);
                    }
                }
            }
        }
    }

    int getWbIndex(int x, int y)
    {
        return (BLACK_FRAME_PIXELS + x) + (BLACK_FRAME_PIXELS + y) * wbWidth;
    }

    private void fillWorkingBuffer()
    {
        for (int x = 0; x < width; ++x)
        {
            for (int y = 0; y < height; ++y)
            {
                data[getWbIndex(x, y)] = sourceMask.get(maskOffset + x * height + y) ? 0xff : 0;
            }
        }
    }

}
