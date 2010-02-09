/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * Stores information about images and metadata of one well.
 * 
 * @author Tomasz Pylak
 */
class WellData
{
    private WellMetadata metadataOrNull;

    private WellImages imagesOrNull;

    private ExperimentIdentifier experimentIdentifier;

    public static WellData create(PlateContent plateContent, WellLocation location)
    {
        ExperimentIdentifier experimentIdentifier = getExperimentIdentifier(plateContent);
        WellImages wellImages = tryCreateWellImages(plateContent, location);
        return new WellData(wellImages, experimentIdentifier);
    }

    private static ExperimentIdentifier getExperimentIdentifier(PlateContent plateContent)
    {
        return ExperimentIdentifier.createIdentifier(plateContent.getPlate().getExperiment());
    }

    private static WellImages tryCreateWellImages(PlateContent plateContent, WellLocation location)
    {
        DatasetImagesReference images = plateContent.tryGetImages();
        if (images != null)
        {
            return new WellImages(images.getImageParameters(), images.getDownloadUrl(), location);
        } else
        {
            return null;
        }
    }

    private WellData(WellImages imagesOrNull, ExperimentIdentifier experimentIdentifier)
    {
        this.imagesOrNull = imagesOrNull;
        this.experimentIdentifier = experimentIdentifier;
    }

    public void setMetadata(WellMetadata well)
    {
        this.metadataOrNull = well;
    }

    public WellMetadata tryGetMetadata()
    {
        return metadataOrNull;
    }

    public WellImages tryGetImages()
    {
        return imagesOrNull;
    }

    public ExperimentIdentifier getExperiment()
    {
        return experimentIdentifier;
    }

    public String getWellContentDescription()
    {
        if (metadataOrNull != null)
        {
            return metadataOrNull.getWellSample().getSubCode();
        } else
        {
            return "?";
        }
    }
}