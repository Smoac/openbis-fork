/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.images;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageRepresentationFormat;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageRepresentationFormat.ImageRepresentationTransformation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageZoomLevelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageZoomLevelTransformationEnrichedDTO;

/**
 * @author Jakub Straszewski
 */
public class RepresentationUtil
{

    public static List<ImageRepresentationFormat> getImageRepresentationFormats(
            ImgImageDatasetDTO imageDataSet, IImagingReadonlyQueryDAO dao)
    {
        List<ImgImageZoomLevelDTO> zoomLevels = dao.listImageZoomLevels(imageDataSet.getId());
        List<ImgImageZoomLevelTransformationEnrichedDTO> zoomLevelsTransformations =
                dao.listImageZoomLevelTransformations(imageDataSet.getId());
        return convertZoomLevelsToRepresentationFormats(imageDataSet.getPermId(), zoomLevels,
                zoomLevelsTransformations);
    }

    private static Map<String, List<ImageRepresentationTransformation>> mapTransformationsPerPhysicallDataSets(
            List<ImgImageZoomLevelTransformationEnrichedDTO> enrichedTransformations)
    {
        Map<String, List<ImageRepresentationTransformation>> transformations =
                new HashMap<String, List<ImageRepresentationTransformation>>();

        if (enrichedTransformations != null)
        {
            for (ImgImageZoomLevelTransformationEnrichedDTO transformation : enrichedTransformations)
            {
                List<ImageRepresentationTransformation> transformationsPerDataSet =
                        transformations.get(transformation.getPhysicalDatasetPermId());
                if (transformationsPerDataSet == null)
                {
                    transformationsPerDataSet = new ArrayList<ImageRepresentationTransformation>();
                    transformations.put(transformation.getPhysicalDatasetPermId(),
                            transformationsPerDataSet);
                }
                transformationsPerDataSet.add(new ImageRepresentationTransformation(transformation
                        .getImageTransformationId(), transformation.getTransformationCode(),
                        transformation.getChannelCode()));
            }
        }

        return transformations;
    }

    private static List<ImageRepresentationFormat> convertZoomLevelsToRepresentationFormats(
            String dataSetCode, List<ImgImageZoomLevelDTO> zoomLevels,
            List<ImgImageZoomLevelTransformationEnrichedDTO> transformations)
    {
        ArrayList<ImageRepresentationFormat> results = new ArrayList<ImageRepresentationFormat>();
        Map<String, List<ImageRepresentationTransformation>> transformationsPerDataSet =
                mapTransformationsPerPhysicallDataSets(transformations);

        for (ImgImageZoomLevelDTO zoomLevel : zoomLevels)
        {
            ImageRepresentationFormat result =
                    new ImageRepresentationFormat(dataSetCode, zoomLevel.getId(),
                            zoomLevel.getIsOriginal(), zoomLevel.getWidth(), zoomLevel.getHeight(),
                            zoomLevel.getColorDepth(), zoomLevel.getFileType(),
                            transformationsPerDataSet.get(zoomLevel.getPhysicalDatasetPermId()));
            results.add(result);
        }

        return results;
    }

}
