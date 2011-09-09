/*
 * Copyright 2011 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.transformations;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageTransformation;

/**
 * Utility class to construct various kinds of image transformations.
 * 
 * @author Tomasz Pylak
 */
public class ImageTransformationBuffer
{
    private static final String PREDEFINED_TRANSFORMATIONS_CODE_PREFIX = "PREDEFINED_";

    private final List<ImageTransformation> imageTransformations;

    private final Set<String> usedTransformationCodes;

    public ImageTransformationBuffer()
    {
        this.imageTransformations = new ArrayList<ImageTransformation>();
        this.usedTransformationCodes = new HashSet<String>();
    }

    /**
     * Appends any specified transformation. Note that code of each added transformation should be
     * unique.
     */
    public void append(ImageTransformation transformation)
    {
        String newCode = transformation.getCode();
        if (usedTransformationCodes.contains(newCode))
        {
            throw new IllegalArgumentException("Two transformations have the same code: " + newCode);
        }
        usedTransformationCodes.add(newCode);
        imageTransformations.add(transformation);
    }

    /** @returns all appended transformations */
    public ImageTransformation[] getTransformations()
    {
        return imageTransformations.toArray(new ImageTransformation[imageTransformations.size()]);
    }

    // -------------- bit shifting transformations ---------------------

    /**
     * Appends transformations which extracts a range of grayscale image colors by choosing 8
     * consecutive bits. All shifts which make sense for 12 bit images will be appended (from 0 to
     * 4).
     */
    public void appendAllBitShiftsFor12Bit()
    {
        appendAllAvailableBitShifts(12);
    }

    /**
     * Appends transformations which extracts a range of grayscale image colors by choosing 8
     * consecutive bits. All shifts which make sense for 16 bit images will be appended (from 0 to
     * 8).
     */
    public void appendAllBitShiftsFor16Bit()
    {
        appendAllAvailableBitShifts(16);
    }

    private void appendAllAvailableBitShifts(int totalNumberOfBits)
    {
        for (int i = 0; i <= (totalNumberOfBits - 8); i++)
        {
            appendBitShiftingTransformation(i);
        }
    }

    /**
     * Appends transformation which extracts a range of grayscale image colors by choosing 8
     * consecutive bits starting from the specified one.
     */
    public void appendBitShiftingTransformation(int shiftBits)
    {
        append(createBitShifting(shiftBits));
    }

    /**
     * Creates a transformation which extracts a range of grayscale image colors by choosing 8
     * consecutive bits, staring from the specified one.
     * <p>
     * This method is useful when one wants to modify the default code, label or description
     * afterwards.
     */
    public static ImageTransformation createBitShifting(int shiftBits)
    {
        if (shiftBits < 0)
        {
            throw new IllegalArgumentException(
                    "Cannot create an image transformation which shifts by a negative number of bits");
        }
        String label = createBitShiftingTransformationLabel(shiftBits);
        String code = createBitShiftingTransformationCode(shiftBits);
        String description = createBitShiftingTransformationDescription(shiftBits);

        IImageTransformerFactory factory = new BitShiftingImageTransformerFactory(shiftBits);
        return new ImageTransformation(code, label, description, factory);
    }

    private static String createBitShiftingTransformationCode(int shiftBits)
    {
        return PREDEFINED_TRANSFORMATIONS_CODE_PREFIX + "BIT_SHIFTING_" + shiftBits;
    }

    private static String createBitShiftingTransformationDescription(int shiftBits)
    {
        return String.format(
                "Shows a range of grayscale image colors by visualising only 8 consecutive bits from %d to %d. "
                        + "Useful for extracting information from 12 and 16 bit images.",
                shiftBits, shiftBits + 7);
    }

    private static String createBitShiftingTransformationLabel(int shiftBits)
    {
        return String.format("Show bits %d-%d", shiftBits, shiftBits + 7);
    }

    // -----------------------------

    /**
     * Appends transformation which converts grayscale image pixel intensities from the range
     * [blackPointIntensity, whitePointIntensity] to 8 bit color depth. Useful to compare images of
     * higher color depth with each other when they do not use the whole range of available
     * intensities.
     */
    public void appendRescaleIntensityRangeTo8Bits(int blackPointIntensity, int whitePointIntensity)
    {
        appendRescaleIntensityRangeTo8Bits(blackPointIntensity, whitePointIntensity, null);
    }

    /**
     * See {@link #appendRescaleIntensityRangeTo8Bits(int, int)}.
     * <p>
     * Additionally sets the label of the transformation.
     */
    public void appendRescaleIntensityRangeTo8Bits(int blackPointIntensity,
            int whitePointIntensity, String userFriendlyLabelOrNull)
    {
        append(createRescaleIntensityRangeTo8Bits(blackPointIntensity, whitePointIntensity,
                userFriendlyLabelOrNull));
    }

    /**
     * Creates a transformation which converts grayscale image pixel intensities from the range
     * [blackPointIntensity, whitePointIntensity] to 8 bit color depth.
     * <p>
     * This method is useful when one wants to modify the default code, label or description
     * afterwards.
     * 
     * @param userFriendlyLabelOrNull label of the transformation. If null a default label is
     *            assigned.
     */
    public static ImageTransformation createRescaleIntensityRangeTo8Bits(int blackPointIntensity,
            int whitePointIntensity, String userFriendlyLabelOrNull)
    {
        if (blackPointIntensity > whitePointIntensity || blackPointIntensity < 0
                || whitePointIntensity < 0)
        {
            throw new IllegalArgumentException(String.format(
                    "Cannot create an image transformation because the range "
                            + "of intensities is invalid: [%d, %d]", blackPointIntensity,
                    whitePointIntensity));
        }
        String label = createIntensityRangeTransformationLabel(userFriendlyLabelOrNull);
        String code =
                createIntensityRangeTransformationCode(blackPointIntensity, whitePointIntensity);
        String description =
                createIntensityRangeTransformationDescription(blackPointIntensity,
                        whitePointIntensity);

        IImageTransformerFactory factory =
                new IntensityRangeImageTransformerFactory(blackPointIntensity, whitePointIntensity);
        return new ImageTransformation(code, label, description, factory);
    }

    private static String createIntensityRangeTransformationCode(int blackPointIntensity,
            int whitePointIntensity)
    {
        return PREDEFINED_TRANSFORMATIONS_CODE_PREFIX + "INTENSITY_LEVEL_" + blackPointIntensity
                + "_" + whitePointIntensity;
    }

    private static String createIntensityRangeTransformationDescription(int blackPointIntensity,
            int whitePointIntensity)
    {
        return String
                .format("Transforms grayscale image by converting intensities of its pixels "
                        + "which are in the range [%d, %d] to 8 bit color depth. "
                        + "The range of intensities is usually calculated by processing a series of 12 or 16 bit images, "
                        + "then the transformation becomes useful to compare these images with each other in 8 bit color depth, "
                        + "especially when they use only a small part of available intensities range.",
                        blackPointIntensity, whitePointIntensity);
    }

    private static String createIntensityRangeTransformationLabel(String labelOrNull)
    {
        return labelOrNull != null ? labelOrNull : "Comparable Autorescaling";
    }

    // --------------------------

    /**
     * Appends transformation which converts each single grayscale image to 8 bit color depth and
     * rescales pixels intensities so that the darkest pixel will become black and the brightest
     * will become white.
     */
    public void appendAutoRescaleIntensityTo8Bits()
    {
        appendAutoRescaleIntensityTo8Bits(0, null);
    }

    /**
     * See {@link #appendAutoRescaleIntensityTo8Bits()}.
     * 
     * @param threshold value form 0 to 1, it specifies the percentage of darkest and brightest
     *            pixels which will be ignored (they will all become black or white).
     */
    public void appendAutoRescaleIntensityTo8Bits(float threshold)
    {
        appendAutoRescaleIntensityTo8Bits(threshold, null);
    }

    /**
     * See {@link #appendAutoRescaleIntensityTo8Bits(float)}.
     * <p>
     * Additionally sets the label of the transformation.
     */
    public void appendAutoRescaleIntensityTo8Bits(float threshold, String userFriendlyLabelOrNull)
    {
        append(createAutoRescaleIntensityTo8Bits(threshold, userFriendlyLabelOrNull));
    }

    /**
     * Creates a transformation which converts each single grayscale image to 8 bit color depth and
     * rescales pixels intensities so that the darkest pixel will become black and the brightest
     * will become white (with some threshold margin).
     * <p>
     * This method is useful when one wants to modify the default code, label or description
     * afterwards.
     * 
     * @param threshold value form 0 to 1, it specifies the percentage of darkest and brightest
     *            pixels which will be ignored (they will all become black or white).
     * @param userFriendlyLabelOrNull label of the transformation. If null a default label is
     *            assigned.
     */
    public static ImageTransformation createAutoRescaleIntensityTo8Bits(float threshold,
            String userFriendlyLabelOrNull)
    {
        if (threshold < 0 || threshold > 1)
        {
            throw new IllegalArgumentException(
                    "Invalid value of the threshold, should be between 0 and 1, but is: "
                            + threshold);
        }
        String label = createAutoRescaleIntensityTransformationLabel(userFriendlyLabelOrNull);
        String code = createAutoRescaleIntensityTransformationCode(threshold);
        String description = createAutoRescaleIntensityTransformationDescription(threshold);

        IImageTransformerFactory factory =
                new AutoRescaleIntensityImageTransformerFactory(threshold);
        return new ImageTransformation(code, label, description, factory);
    }

    private static String createAutoRescaleIntensityTransformationCode(float threshold)
    {
        return PREDEFINED_TRANSFORMATIONS_CODE_PREFIX + "AUTO_INTENSITY_LEVEL_" + threshold;
    }

    private static String createAutoRescaleIntensityTransformationDescription(float threshold)
    {
        return String
                .format("Creates a transformation which converts each single grayscale image to 8 bit color depth and "
                        + "rescales pixels intensities so that the darkest pixel will become black and the brightest "
                        + "will become white (threshold margin: %s).",
                        new DecimalFormat("#.###").format(threshold));
    }

    private static String createAutoRescaleIntensityTransformationLabel(String labelOrNull)
    {
        return labelOrNull != null ? labelOrNull : "Autorescaling";
    }

    // --------------------------

    /**
     * Allows to transform the images with ImageMagic convert tool (which has to be installed and
     * accessible). Convert will be called with the specified parameters.
     */
    public void appendImageMagicConvertTransformation(String convertCliArguments)
    {
        appendImageMagicConvertTransformation(convertCliArguments, null);
    }

    /**
     * See {@link #appendImageMagicConvertTransformation(String)}.
     * <p>
     * Additionally sets the label of the transformation.
     */
    public void appendImageMagicConvertTransformation(String convertCliArguments,
            String userFriendlyLabelOrNull)
    {
        append(createImageMagicConvert(convertCliArguments,
                generateUniqueConvertTransformationCode(), userFriendlyLabelOrNull));
    }

    /**
     * Creates a transformation which converts the images with ImageMagic convert tool.
     * <p>
     * This method is useful when one wants to modify the default code, label or description
     * afterwards.
     * 
     * @param userFriendlyLabelOrNull label of the transformation. If null a default label is
     *            assigned.
     */
    public static ImageTransformation createImageMagicConvert(String convertCliArguments,
            String transformationCode, String userFriendlyLabelOrNull)
    {
        if (StringUtils.isBlank(convertCliArguments))
        {
            throw new IllegalArgumentException(
                    "No argument has been specified for the 'convert' command");
        }
        if (StringUtils.isBlank(transformationCode))
        {
            throw new IllegalArgumentException("Transformation has not been specified");
        }
        String label =
                createImageMagicConvertTransformationLabel(convertCliArguments,
                        userFriendlyLabelOrNull);
        String description = createImageMagicConvertTransformationDescription(convertCliArguments);

        IImageTransformerFactory factory =
                new ConvertToolImageTransformerFactory(convertCliArguments);
        return new ImageTransformation(transformationCode, label, description, factory);
    }

    private static String createImageMagicConvertTransformationDescription(
            String convertCliArguments)
    {
        return "Transforms images with ImageMagic 'convert' tool with parameters: "
                + convertCliArguments;
    }

    private static String createImageMagicConvertTransformationLabel(String convertCliArguments,
            String labelOrNull)
    {
        return labelOrNull != null ? labelOrNull : String.format("Convert (%s)",
                convertCliArguments);
    }

    private String generateUniqueConvertTransformationCode()
    {
        int i = 1;
        while (usedTransformationCodes.contains(getConvertTransformationCode(i)))
        {
            i++;
        }
        return getConvertTransformationCode(i);
    }

    private static String getConvertTransformationCode(int seqNumber)
    {
        return PREDEFINED_TRANSFORMATIONS_CODE_PREFIX + "CONVERT_" + seqNumber;
    }
}
