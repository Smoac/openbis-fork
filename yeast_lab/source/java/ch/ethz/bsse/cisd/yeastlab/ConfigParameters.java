/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.ethz.bsse.cisd.yeastlab;

import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * Configuration parameters for the Generation Detection algorithm.
 * 
 * @author Piotr Buczek
 */
final class ConfigParameters
{
    private static final String BEGINNING_F_WINDOW_FILTER_ENABLED =
            "beginning-f-window-filter-enabled";

    private static final String BEGINNING_F_WINDOW_LENGTH = "beginning-f-window-length";

    private static final String BEGINNING_F_WINDOW_MAX_AVG_DIFF = "beginning-f-window-max-avg-diff";

    private static final String BEGINNING_F_WINDOW_MAX_MISSING = "beginning-f-window-max-missing";

    private static final String BEGINNING_F_WINDOW_MAX_OFFSET = "beginning-f-window-max-offset";

    private static final String FIRST_PEAK_FILTER_ENABLED = "first-peak-filter-enabled";

    private static final String FIRST_PEAK_MAX_CHILD_OFFSET = "first-peak-max-child-offset";

    private static final String FIRST_PEAK_MAX_PARENT_OFFSET = "first-peak-max-parent-offset";

    private static final String FIRST_PEAK_MAX_MISSING = "first-peak-max-missing";

    private static final String FIRST_PEAK_MIN_FRAME_HEIGHT_DIFF =
            "first-peak-min-frame-height-diff";

    private static final String FIRST_PEAK_MAX_FRAME_HEIGHT_DIFF_EXCEPTIONS =
            "first-peak-max-frame-height-diff-exceptions";

    private static final String FIRST_PEAK_MIN_LENGTH = "first-peak-min-length";

    private static final String FIRST_PEAK_MIN_TOTAL_HEIGHT_DIFF =
            "first-peak-min-total-height-diff";

    private static final String FIRST_PEAK_NUMBER_OF_FIRST_FRAMES_TO_IGNORE =
            "first-peak-number-of-first-frames-to-ignore";

    private static final String MAX_F_MEAN_OF_LIVING_CELL = "max-f-mean-of-living-cell";

    private static final String MAX_NEW_BORN_CELL_PIXELS = "max-new-born-cell-pixels";

    private static final String MAX_PARENT_CANDIDATES = "max-parent-candidates";

    private static final String MIN_NEW_BORN_CELL_ECCENTRICITY = "min-new-born-cell-eccentricity";

    private static final String MIN_PARENT_PIXELS = "min-parent-pixels";

    private static final String MIN_STABLE_NUCLEUS_AREA_FRAMES = "min-stable-nucleus-area-frames";

    private static final String NUMBER_OF_FIRST_FRAMES_TO_IGNORE =
            "number-of-first-frames-to-ignore";

    private static final String NUMBER_OF_LAST_FRAMES_TO_IGNORE = "number-of-last-frames-to-ignore";

    private static final String SMOOTH_F_DEVIATION_WINDOW = "smooth-f-deviation-window";

    private final boolean beginningFWindowFilterEnabled;

    private final int beginningFWindowLength;

    private final double beginningFWindowMaxAvgDiff;

    private final int beginningFWindowMaxMissing;

    private final int beginningFWindowMaxOffset;

    private final boolean firstPeakFilterEnabled;

    private final int firstPeakMaxFrameHeightDiffExceptions;

    private final double firstPeakMinFrameHeightDiff;

    private final int firstPeakMinLength;

    private final double firstPeakMinTotalHeightDiff;

    private final int firstPeakMaxChildOffset;

    private final int firstPeakMaxParentOffset;

    private final int firstPeakMaxMissing;

    private final int firstPeakNumberOfFirstFramesToIgnore;

    private final double maxFMeanOfLivingCell;

    private final int maxNewBornCellPixels;

    private final int maxParentCandidates;

    private final double minNewBornCellEccentricity;

    private final int minParentPixels;

    private final int minStableNucleusAreaFrames;

    private final int numberOfFirstFramesToIgnore;

    private final int numberOfLastFramesToIgnore;

    private final int smoothFDeviationWindow;

    /**
     * Creates an instance based on a properties file at given path.
     * 
     * @throws ConfigurationFailureException if properties file does not exist, a property is missed
     *             or has an invalid value.
     */
    public ConfigParameters(String propertiesFilePath) throws ConfigurationFailureException
    {
        this(PropertyUtils.loadProperties(propertiesFilePath));
    }

    /**
     * Creates an instance based on the specified properties.
     * 
     * @throws ConfigurationFailureException if a property is missed or has an invalid value.
     */
    private ConfigParameters(final Properties properties)
    {
        beginningFWindowFilterEnabled =
                getMandatoryBooleanProperty(properties, BEGINNING_F_WINDOW_FILTER_ENABLED);
        beginningFWindowLength = getMandatoryIntegerProperty(properties, BEGINNING_F_WINDOW_LENGTH);
        beginningFWindowMaxAvgDiff =
                getMandatoryDoubleProperty(properties, BEGINNING_F_WINDOW_MAX_AVG_DIFF);
        beginningFWindowMaxMissing =
                getMandatoryIntegerProperty(properties, BEGINNING_F_WINDOW_MAX_MISSING);
        beginningFWindowMaxOffset =
                getMandatoryIntegerProperty(properties, BEGINNING_F_WINDOW_MAX_OFFSET);

        firstPeakFilterEnabled = getMandatoryBooleanProperty(properties, FIRST_PEAK_FILTER_ENABLED);
        firstPeakMaxChildOffset =
                getMandatoryIntegerProperty(properties, FIRST_PEAK_MAX_CHILD_OFFSET);
        firstPeakMaxParentOffset =
                getMandatoryIntegerProperty(properties, FIRST_PEAK_MAX_PARENT_OFFSET);
        firstPeakMaxMissing = getMandatoryIntegerProperty(properties, FIRST_PEAK_MAX_MISSING);
        firstPeakMaxFrameHeightDiffExceptions =
                getMandatoryIntegerProperty(properties, FIRST_PEAK_MAX_FRAME_HEIGHT_DIFF_EXCEPTIONS);
        firstPeakMinFrameHeightDiff =
                getMandatoryDoubleProperty(properties, FIRST_PEAK_MIN_FRAME_HEIGHT_DIFF);
        firstPeakMinLength = getMandatoryIntegerProperty(properties, FIRST_PEAK_MIN_LENGTH);
        firstPeakMinTotalHeightDiff =
                getMandatoryDoubleProperty(properties, FIRST_PEAK_MIN_TOTAL_HEIGHT_DIFF);
        firstPeakNumberOfFirstFramesToIgnore =
                getMandatoryIntegerProperty(properties, FIRST_PEAK_NUMBER_OF_FIRST_FRAMES_TO_IGNORE);

        maxFMeanOfLivingCell = getMandatoryDoubleProperty(properties, MAX_F_MEAN_OF_LIVING_CELL);
        maxNewBornCellPixels = getMandatoryIntegerProperty(properties, MAX_NEW_BORN_CELL_PIXELS);
        maxParentCandidates = getMandatoryIntegerProperty(properties, MAX_PARENT_CANDIDATES);
        minNewBornCellEccentricity =
                getMandatoryDoubleProperty(properties, MIN_NEW_BORN_CELL_ECCENTRICITY);
        minParentPixels = getMandatoryIntegerProperty(properties, MIN_PARENT_PIXELS);
        minStableNucleusAreaFrames =
                getMandatoryIntegerProperty(properties, MIN_STABLE_NUCLEUS_AREA_FRAMES);
        numberOfFirstFramesToIgnore =
                getMandatoryIntegerProperty(properties, NUMBER_OF_FIRST_FRAMES_TO_IGNORE);
        numberOfLastFramesToIgnore =
                getMandatoryIntegerProperty(properties, NUMBER_OF_LAST_FRAMES_TO_IGNORE);
        smoothFDeviationWindow = getMandatoryIntegerProperty(properties, SMOOTH_F_DEVIATION_WINDOW);
    }

    public double getMaxFMeanOfLivingCell()
    {
        return maxFMeanOfLivingCell;
    }

    public int getMaxNewBornCellPixels()
    {
        return maxNewBornCellPixels;
    }

    public int getMaxParentCandidates()
    {
        return maxParentCandidates;
    }

    public double getMinNewBornCellEccentricity()
    {
        return minNewBornCellEccentricity;
    }

    public int getMinParentPixels()
    {
        return minParentPixels;
    }

    public int getMinStableNucleusAreaFrames()
    {
        return minStableNucleusAreaFrames;
    }

    public int getNumberOfFirstFramesToIgnore()
    {
        return numberOfFirstFramesToIgnore;
    }

    public int getNumberOfLastFramesToIgnore()
    {
        return numberOfLastFramesToIgnore;
    }

    public int getSmoothFDeviationWindow()
    {
        return smoothFDeviationWindow;
    }

    public boolean isBeginningFWindowFilterEnabled()
    {
        return beginningFWindowFilterEnabled;
    }

    public int getBeginningFWindowLength()
    {
        return beginningFWindowLength;
    }

    public double getBeginningFWindowMaxAvgDiff()
    {
        return beginningFWindowMaxAvgDiff;
    }

    public int getBeginningFWindowMaxMissing()
    {
        return beginningFWindowMaxMissing;
    }

    public int getBeginningFWindowMaxOffset()
    {
        return beginningFWindowMaxOffset;
    }

    public boolean isFirstPeakFilterEnabled()
    {
        return firstPeakFilterEnabled;
    }

    public int getFirstPeakMaxChildOffset()
    {
        return firstPeakMaxChildOffset;
    }

    public int getFirstPeakMaxParentOffset()
    {
        return firstPeakMaxParentOffset;
    }

    public int getFirstPeakMaxMissing()
    {
        return firstPeakMaxMissing;
    }

    public double getFirstPeakMinFrameHeightDiff()
    {
        return firstPeakMinFrameHeightDiff;
    }

    public int getFirstPeakMaxFrameHeightDiffExceptions()
    {
        return firstPeakMaxFrameHeightDiffExceptions;
    }

    public int getFirstPeakMinLength()
    {
        return firstPeakMinLength;
    }

    public double getFirstPeakMinTotalHeightDiff()
    {
        return firstPeakMinTotalHeightDiff;
    }

    public int getFirstPeakNumberOfFirstFramesToIgnore()
    {
        return firstPeakNumberOfFirstFramesToIgnore;
    }

    public final String getDescription()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("Generation detection configuration parameters:\n\n");
        sb.append(String.format("Max number of pixels of a new born cell: %d\n",
                getMaxNewBornCellPixels()));
        sb.append(String
                .format("Min number of pixels of a parent cell: %d\n", getMinParentPixels()));
        sb.append(String.format("Min number of frames with stable cell nucleus area: %d\n",
                getMinStableNucleusAreaFrames()));
        sb.append(String.format("Min eccentricity of a new born cell: %1.2f\n",
                getMinNewBornCellEccentricity()));
        sb.append(String.format("Number of first frames to ignore: %d\n",
                getNumberOfFirstFramesToIgnore()));
        sb.append(String.format("Number of last frames to ignore: %d\n",
                getNumberOfLastFramesToIgnore()));
        sb.append(String.format(
                "Frame window radius for smooth fluorescence deviation evaluation: %d\n",
                getSmoothFDeviationWindow()));
        sb.append(String.format("Max fluorescence mean of a living cell: %1.2f\n",
                getMaxFMeanOfLivingCell()));
        sb.append(String.format("Max number of parent candidates per cell: %d\n",
                getMaxParentCandidates()));
        sb.append(String.format("beginning-f-window-filter-enabled: %s\n",
                isBeginningFWindowFilterEnabled()));
        sb.append(String.format("beginning-f-window-length: %d\n", getBeginningFWindowLength()));
        sb.append(String.format("beginning-f-window-max-avg-diff: %1.2f\n",
                getBeginningFWindowMaxAvgDiff()));
        sb.append(String.format("beginning-f-window-max-missing: %d\n",
                getBeginningFWindowMaxMissing()));
        sb.append(String.format("beginning-f-window-max-offset: %d\n",
                getBeginningFWindowMaxOffset()));
        sb.append(String.format("first-peak-filter-enabled: %s\n", isFirstPeakFilterEnabled()));
        sb.append(String.format("first-peak-number-of-first-frames-to-ignore: %d\n",
                getFirstPeakNumberOfFirstFramesToIgnore()));
        sb.append(String.format("first-peak-max-child-offset: %d\n", getFirstPeakMaxChildOffset()));
        sb.append(String
                .format("first-peak-max-parent-offset: %d\n", getFirstPeakMaxParentOffset()));
        sb.append(String.format("first-peak-max-missing: %d\n", getFirstPeakMaxMissing()));
        sb.append(String.format("first-peak-min-length: %d\n", getFirstPeakMinLength()));
        sb.append(String.format("first-peak-min-frame-height-diff: %1.2f\n",
                getFirstPeakMinFrameHeightDiff()));
        sb.append(String.format("first-peak-max-frame-height-diff-exceptions: %d\n",
                getFirstPeakMaxFrameHeightDiffExceptions()));
        sb.append(String.format("first-peak-min-total-height-diff: %1.2f\n",
                getFirstPeakMinTotalHeightDiff()));
        sb.append("\n");
        return sb.toString();
    }

    //
    // helper methods for dealing with mandatory typed properties
    //

    private final static double getMandatoryDoubleProperty(final Properties properties,
            final String key)
    {
        final String property = PropertyUtils.getMandatoryProperty(properties, key);
        try
        {
            return Double.parseDouble(property);
        } catch (final NumberFormatException ex)
        {
            throw new ConfigurationFailureException("Configuration parameter '" + key
                    + "' is not a double number: " + property);
        }
    }

    private final static int getMandatoryIntegerProperty(final Properties properties,
            final String key)
    {
        final String property = PropertyUtils.getMandatoryProperty(properties, key);
        try
        {
            return Integer.parseInt(property);
        } catch (final NumberFormatException ex)
        {
            throw new ConfigurationFailureException("Configuration parameter '" + key
                    + "' is not an integer number: " + property);
        }
    }

    private final static boolean getMandatoryBooleanProperty(final Properties properties,
            final String key)
    {
        final String property = PropertyUtils.getMandatoryProperty(properties, key);
        try
        {
            return Boolean.parseBoolean(property);
        } catch (final NumberFormatException ex)
        {
            throw new ConfigurationFailureException("Configuration parameter '" + key
                    + "' is not a boolean value: " + property);
        }
    }

}
