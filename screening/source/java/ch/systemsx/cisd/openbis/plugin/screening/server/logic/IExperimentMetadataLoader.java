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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.List;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageChannel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;

/**
 * Loader for experiment image metadata.
 * 
 * @author Kaloyan Enimanev
 * @author Franz-Josef Elmer
 */
public interface IExperimentMetadataLoader
{
    /**
     * Try to detect the plate geometry of an experiment. Return NULL if there is no plate geometry or the geometries of the contained plates differ.
     */
    Geometry tryGetPlateGeometry();

    /**
     * Try to detect the tile geometry of an experiment. Return NULL if there is no tile geometry or the geometries of the contained tiles differ.
     */
    Geometry tryGetTileGeometry();

    /**
     * Return all image channels within an experiment.
     */
    List<ImageChannel> getImageChannels();

    /**
     * Returns the original image size.
     * 
     * @return <code>null</code> if not all data sets have the same original image size.
     */
    ImageSize tryGetOriginalImageSize();

    /**
     * Returns a sorted list of image sizes where for all data sets thumbnail images of these sizes exist.
     */
    List<ImageSize> getThumbnailImageSizes();
}
