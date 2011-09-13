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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import net.lemnik.eodsql.ResultColumn;

import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelColor;

/**
 * @author Tomasz Pylak
 */
public class ImgChannelDTO extends AbstractImgIdentifiable
{
    @ResultColumn("CODE")
    private String code;

    @ResultColumn("LABEL")
    private String label;

    @ResultColumn("DESCRIPTION")
    private String descriptionOrNull;

    @ResultColumn("WAVELENGTH")
    private Integer wavelengthOrNull;

    // can be null if experimentId is not null
    @ResultColumn("DS_ID")
    private Long datasetIdOrNull;

    // can be null if datasetId is not null
    @ResultColumn("EXP_ID")
    private Long experimentIdOrNull;

    /** Value for the {@link ImageChannelColor} enum. Not null. */
    @ResultColumn("COLOR")
    private String channelColor;

    // EODSQL only
    @SuppressWarnings("unused")
    private ImgChannelDTO()
    {
        // All Data-Object classes must have a default constructor.
    }

    public ImgChannelDTO(String code, String descriptionOrNull, Integer wavelengthOrNull,
            Long datasetIdOrNull, Long experimentIdOrNull, String label,
            ImageChannelColor channelColor)
    {
        assert (datasetIdOrNull == null && experimentIdOrNull != null)
                || (datasetIdOrNull != null && experimentIdOrNull == null);
        this.code = CodeNormalizer.normalize(code);
        this.label = label;
        this.descriptionOrNull = descriptionOrNull;
        this.wavelengthOrNull = wavelengthOrNull;
        this.datasetIdOrNull = datasetIdOrNull;
        this.experimentIdOrNull = experimentIdOrNull;
        this.channelColor = channelColor.name();
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getDescription()
    {
        return descriptionOrNull;
    }

    public void setDescription(String description)
    {
        this.descriptionOrNull = description;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    /** can be null */
    public Integer getWavelength()
    {
        return wavelengthOrNull;
    }

    public void setWavelength(Integer wavelength)
    {
        this.wavelengthOrNull = wavelength;
    }

    public Long getDatasetId()
    {
        return datasetIdOrNull;
    }

    public void setDatasetId(Long datasetId)
    {
        this.datasetIdOrNull = datasetId;
    }

    public Long getExperimentId()
    {
        return experimentIdOrNull;
    }

    public void setExperimentId(Long experimentId)
    {
        this.experimentIdOrNull = experimentId;
    }

    /** Can be converted to {@link ImageChannelColor}. */
    public String getDbChannelColor()
    {
        return channelColor;
    }

    public void setDbChannelColor(String channelColor)
    {
        this.channelColor = channelColor;
    }
}
