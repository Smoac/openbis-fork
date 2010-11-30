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

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.common.annotation.BeanProperty;

/**
 * @author Izabela Adamczyk
 */
public class Features
{

    private static final String POSITION = "position";

    private static final String SIDE = "side";

    private static final String QUALITY = "quality";

    private static final String QUALITY_DESC = "quality_desc";

    private static final String INTENSITY_CHANGE = "intensity_change";

    private static final String LOCALIZATION_CHANGE = "localization_change";

    private static final String END_LOCALIZATION = "end_localization";

    private static final String INITIAL_LOCALIZATION = "initial_localization";

    private String position;

    private String side;

    private String quality;

    private String qualityDesc;

    private String intensityChange;

    private String localizationChange;

    private String initialLocalization;

    private String endLocalization;

    public Features()
    {
    }

    public String getPosition()
    {
        return position;
    }

    @BeanProperty(label = POSITION)
    public void setPosition(String position)
    {
        this.position = position;
    }

    public String getSide()
    {
        return side;
    }

    @BeanProperty(label = SIDE)
    public void setSide(String side)
    {
        this.side = side;
    }

    public String getQuality()
    {
        return quality;
    }

    @BeanProperty(label = QUALITY)
    public void setQuality(String quality)
    {
        this.quality = quality;
    }

    public String getQualityDesc()
    {
        return qualityDesc;
    }

    @BeanProperty(label = QUALITY_DESC)
    public void setQualityDesc(String qualityDesc)
    {
        this.qualityDesc = qualityDesc;
    }

    public String getIntensityChange()
    {
        return intensityChange;
    }

    @BeanProperty(label = INTENSITY_CHANGE)
    public void setIntensityChange(String intensityChange)
    {
        this.intensityChange = intensityChange;
    }

    public String getLocalizationChange()
    {
        return localizationChange;
    }

    @BeanProperty(label = LOCALIZATION_CHANGE)
    public void setLocalizationChange(String localizationChange)
    {
        this.localizationChange = localizationChange;
    }

    public String getInitialLocalization()
    {
        return initialLocalization;
    }

    @BeanProperty(label = INITIAL_LOCALIZATION)
    public void setInitialLocalization(String initialLocalization)
    {
        this.initialLocalization = initialLocalization;
    }

    public String getEndLocalization()
    {
        return endLocalization;
    }

    @BeanProperty(label = END_LOCALIZATION)
    public void setEndLocalization(String endLocalization)
    {
        this.endLocalization = endLocalization;
    }

    /** NOTE: Order strictly connected with {@link #getColumns()} */
    public static List<String> getHeaderColumns()
    {
        return Arrays.asList(END_LOCALIZATION, INITIAL_LOCALIZATION, INTENSITY_CHANGE,
                LOCALIZATION_CHANGE, POSITION, QUALITY, QUALITY_DESC, SIDE);
    }

    /** NOTE: Order strictly connected with {@link #getHeaderColumns()} */
    public List<String> getColumns()
    {
        return Arrays.asList(getEndLocalization(), getInitialLocalization(), getIntensityChange(),
                getLocalizationChange(), getPosition(), getQuality(), getQualityDesc(), getSide());
    }
}