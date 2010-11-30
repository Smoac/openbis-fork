/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.dynamix.tools.feature_converter;

import org.apache.commons.lang.StringUtils;

/**
 * @author Izabela Adamczyk
 */
class CategoryOracle
{
    private static final String DISCARD = "Discard";

    private static final String UP = "up";

    private static final String YES = "yes";

    private static final String NO = "no";

    private static final String CONSTANT = "Constant";

    private static final String GOOD = "Good";

    private enum Category
    {
        OTHER, DISCARDED_CHAMBER, GOOD_CHAMBER_INTENSITY_AND_LOCALIZATION_CHANGE,
        GOOD_CHAMBER_LOCALIZATION_CHANGE, GOOD_CHAMBER_INTENSITY_UP, GOOD_CHAMBER_NO_CHANGE;
    }

    public static String calculateCategory(InputRow in)
    {
        String quality = in.getQuality();
        String intensityChange = in.getIntensityChange();
        String localizationChange = in.getLocalizationChange();
        return calculateCategory(quality, intensityChange, localizationChange).toString();
    }

    private static Category calculateCategory(String quality, String intensityChange,
            String localizationChange)
    {
        if (eq(quality, GOOD) && eq(intensityChange, CONSTANT) && eq(localizationChange, NO))
        {
            return Category.GOOD_CHAMBER_NO_CHANGE;
        } else if (eq(quality, GOOD) && eq(intensityChange, UP) && eq(localizationChange, NO))
        {
            return Category.GOOD_CHAMBER_INTENSITY_UP;
        } else if (eq(quality, GOOD) && eq(intensityChange, NO) && eq(localizationChange, YES))
        {
            return Category.GOOD_CHAMBER_LOCALIZATION_CHANGE;
        } else if (eq(quality, GOOD) && eq(intensityChange, CONSTANT)
                && eq(localizationChange, YES))
        {
            return Category.GOOD_CHAMBER_INTENSITY_AND_LOCALIZATION_CHANGE;
        } else if (eq(quality, DISCARD))
        {
            return Category.DISCARDED_CHAMBER;
        } else
        {
            return Category.OTHER;
        }
    }

    private static boolean eq(String property, String value)
    {
        return StringUtils.equalsIgnoreCase(property, value);
    }
}
