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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

/**
 * Configuration parameters for the Generation Detection algorithm.
 * 
 * @author Piotr Buczek
 */
final class ConfigParameters
{
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
     * @throws ConfFailureException if properties file does not exist, a property is missed or has
     *             an invalid value.
     */
    public ConfigParameters(String propertiesFilePath)
    {
        this(ParameterUtils.loadProperties(propertiesFilePath));
    }

    /**
     * Creates an instance based on the specified properties.
     * 
     * @throws ConfFailureException if a property is missed or has an invalid value.
     */
    private ConfigParameters(final Properties properties)
    {
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
        sb.append("\n");
        return sb.toString();
    }

    //
    // helpers for dealing with Properties
    //

    private final static double getMandatoryDoubleProperty(final Properties properties,
            final String key)
    {
        final String property = ParameterUtils.getMandatoryProperty(properties, key);
        try
        {
            return Double.parseDouble(property);
        } catch (final NumberFormatException ex)
        {
            throw new ConfFailureException("Configuration parameter '" + key
                    + "' is not a double number: " + property);
        }
    }

    private final static int getMandatoryIntegerProperty(final Properties properties,
            final String key)
    {
        final String property = ParameterUtils.getMandatoryProperty(properties, key);
        try
        {
            return Integer.parseInt(property);
        } catch (final NumberFormatException ex)
        {
            throw new ConfFailureException("Configuration parameter '" + key
                    + "' is not an integer number: " + property);
        }
    }

    /**
     * Helper class based on PropertyUtils from common project used to extract values from
     * {@link Properties}.
     */
    // NOTE: this class doesn't use PropertyUtils from common project because that is the only class
    // needed from that project and all classes from that project would need to be added to
    // distribution jar.
    private static class ParameterUtils
    {

        static final String EMPTY_STRING_FORMAT = "Property '%s' is an empty string.";

        static final String NOT_FOUND_PROPERTY_FORMAT =
                "Given key '%s' not found in properties: '%s'";

        private ParameterUtils()
        {
            // This class can not be instantiated.
        }

        private static void assertParameters(final Properties properties, final String propertyKey)
        {
            assert properties != null : "Given properties can not be null.";
            assert propertyKey != null : "Given property key can not be null.";
        }

        /**
         * Searches for the property with the specified key in this property list.
         * 
         * @return <code>null</code> or the value trimmed if found.
         */
        public final static String getProperty(final Properties properties, final String propertyKey)
        {
            assertParameters(properties, propertyKey);
            final String property = properties.getProperty(propertyKey);
            return property == null ? null : property.trim();
        }

        /**
         * Looks up given mandatory <var>propertyKey</var> in given <var>properties</var>.
         * 
         * @throws ConfFailureException if given <var>propertyKey</var> could not be found or if it
         *             is empty.
         */
        public final static String getMandatoryProperty(final Properties properties,
                final String propertyKey) throws ConfFailureException
        {
            assertParameters(properties, propertyKey);
            String property = getProperty(properties, propertyKey);
            if (property == null)
            {
                throw ConfFailureException.fromTemplate(NOT_FOUND_PROPERTY_FORMAT, propertyKey,
                        StringUtils.join(properties.keySet(), ", "));
            }

            if (property.length() == 0)
            {
                throw ConfFailureException.fromTemplate(EMPTY_STRING_FORMAT, propertyKey);
            }
            return property;
        }

        /**
         * Trims each value of given <var>properties</var> using {@link StringUtils#trim(String)}.
         */
        @SuppressWarnings("unchecked")
        public final static void trimProperties(final Properties properties)
        {
            assert properties != null : "Unspecified properties";
            for (final Enumeration<String> enumeration =
                    (Enumeration<String>) properties.propertyNames(); enumeration.hasMoreElements(); /**/)
            {
                final String key = enumeration.nextElement();
                properties.setProperty(key, StringUtils.trim(properties.getProperty(key)));
            }
        }

        /**
         * Loads and returns {@link Properties} found in given <var>propertiesFilePath</var>.
         * 
         * @throws ConfFailureException If an exception occurs when loading the properties.
         * @return never <code>null</code> but could return empty properties.
         */
        public final static Properties loadProperties(final String propertiesFilePath)
        {
            try
            {
                return loadProperties(new FileInputStream(propertiesFilePath), propertiesFilePath);
            } catch (FileNotFoundException ex)
            {
                final String msg =
                        String.format("Could not load the properties from given resource '%s'.",
                                propertiesFilePath);
                throw new ConfFailureException(msg, ex);
            }
        }

        /**
         * Loads and returns {@link Properties} found in given <var>propertiesFilePath</var>.
         * 
         * @throws ConfFailureException If an exception occurs when loading the properties.
         * @return never <code>null</code> but could return empty properties.
         */
        public final static Properties loadProperties(final InputStream is,
                final String resourceName)
        {
            assert is != null : "No input stream specified";
            final Properties properties = new Properties();
            try
            {
                properties.load(is);
                trimProperties(properties);
                return properties;
            } catch (final Exception ex)
            {
                final String msg =
                        String.format("Could not load the properties from given resource '%s'.",
                                resourceName);
                throw new ConfFailureException(msg, ex);
            } finally
            {
                // close quietly
                closeQuietly(is);
            }
        }

        // IOUtils
        /**
         * Unconditionally close an <code>InputStream</code>.
         * <p>
         * Equivalent to {@link InputStream#close()}, except any exceptions will be ignored. This is
         * typically used in finally blocks.
         * 
         * @param input the InputStream to close, may be null or already closed
         */
        public static void closeQuietly(InputStream input)
        {
            try
            {
                if (input != null)
                {
                    input.close();
                }
            } catch (IOException ioe)
            {
                // ignore
            }
        }

    }

    // copy of ConfigurationFailureException from commons
    private static class ConfFailureException extends RuntimeException
    {

        private static final long serialVersionUID = 1L;

        public ConfFailureException(String message)
        {
            super(message);
        }

        public ConfFailureException(String message, Throwable cause)
        {
            super(message, cause);
        }

        /**
         * Creates an {@link ConfFailureException} using a {@link java.util.Formatter}.
         */
        public static ConfFailureException fromTemplate(String messageTemplate, Object... args)
        {
            return new ConfFailureException(String.format(messageTemplate, args));
        }

        /**
         * Creates an {@link ConfFailureException} using a {@link java.util.Formatter}.
         */
        public static ConfFailureException fromTemplate(Throwable cause, String messageTemplate,
                Object... args)
        {
            return new ConfFailureException(String.format(messageTemplate, args), cause);
        }

    }

}
