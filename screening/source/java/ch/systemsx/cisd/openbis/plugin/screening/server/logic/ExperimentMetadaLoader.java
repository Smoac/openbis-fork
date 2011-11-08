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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageChannel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageTransformationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.WidthAndHeightDTO;

/**
 * @author Kaloyan Enimanev
 */
public class ExperimentMetadaLoader implements IExperimentMetadataLoader
{
    /**
     * A query selecting some metadata for an experiment.
     */
    interface IExperimentMetadataQuery<T>
    {

        List<T> select(IImagingReadonlyQueryDAO query);

    }

    private final long experimentId;

    private final List<IImagingReadonlyQueryDAO> imagingQueries;

    public ExperimentMetadaLoader(long experimentId, List<IImagingReadonlyQueryDAO> imagingQueries)
    {
        this.experimentId = experimentId;
        this.imagingQueries = imagingQueries;
    }
    
    public Geometry tryGetPlateGeometry()
    {
        WidthAndHeightDTO plateGeometry =
                getUniqueOrNull(new IExperimentMetadataQuery<WidthAndHeightDTO>()
                    {
                        public List<WidthAndHeightDTO> select(IImagingReadonlyQueryDAO query)
                        {
                            return query.listPlateGeometriesForExperiment(experimentId);
                        }
                    });
        return asGeometry(plateGeometry);
    }

    public Geometry tryGetTileGeometry()
    {
        WidthAndHeightDTO tileGeometry = getUniqueOrNull(new IExperimentMetadataQuery<WidthAndHeightDTO>()
                    {
                        public List<WidthAndHeightDTO> select(IImagingReadonlyQueryDAO query)
                        {
                            return query.listTileGeometriesForExperiment(experimentId);
                        }
                    });
        return asGeometry(tileGeometry);
    }

    public List<ImageChannel> getImageChannels()
    {
        List<ImgChannelDTO> channels =
                getMergedResult(new IExperimentMetadataQuery<ImgChannelDTO>()
                    {

                        public List<ImgChannelDTO> select(IImagingReadonlyQueryDAO query)
                        {
                            // TODO KE: does this return all channels ?
                            return query.getChannelsByExperimentId(experimentId);
                        }
                    });
        List<ImgChannelDTO> uniqueChannels = removeDuplicates(channels);
        return asImageChannels(uniqueChannels);
    }

    private List<ImgChannelDTO> removeDuplicates(List<ImgChannelDTO> channels)
    {
        Map<String, ImgChannelDTO> channelsByCode = new TreeMap<String, ImgChannelDTO>();
        for (ImgChannelDTO channel : channels)
        {
            channelsByCode.put(channel.getCode(), channel);
        }
        return new ArrayList<ImgChannelDTO>(channelsByCode.values());
    }

    private List<ImageChannel> asImageChannels(List<ImgChannelDTO> channels)
    {
        ArrayList<ImageChannel> translated = new ArrayList<ImageChannel>();
        for (ImgChannelDTO channel : channels)
        {
            translated.add(asImageChannel(channel));
        }
        return translated;
    }

    private ImageChannel asImageChannel(ImgChannelDTO channel)
    {
        // TODO KE: fetch the image transformations additionally ?
        return new ImageChannel(channel.getCode(), channel.getLabel(), channel.getDescription(),
                channel.getWavelength(), Collections.<ImageTransformationInfo>emptyList());
    }

    private Geometry asGeometry(WidthAndHeightDTO widthAndHeight)
    {
        if (widthAndHeight == null)
        {
            return null;
        } else
        {
            return Geometry.createFromRowColDimensions(widthAndHeight.getHeight(),
                    widthAndHeight.getWidth());

        }
    }

    private <T> T getUniqueOrNull(IExperimentMetadataQuery<T> experimentMetadataQuery)
    {
        List<T> mergedResults = getMergedResult(experimentMetadataQuery);
        return getUniqueOrNull(mergedResults);
    }

    private <T> List<T> getMergedResult(IExperimentMetadataQuery<T> experimentMetadataQuery)
    {
        List<T> allResults = new ArrayList<T>();
        for (IImagingReadonlyQueryDAO imagingQueryDAO : imagingQueries)
        {
            List<T> singleResult = experimentMetadataQuery.select(imagingQueryDAO);
            if (singleResult != null)
            {
                allResults.addAll(singleResult);
            }
        }
        return allResults;
    }

    private <T> T getUniqueOrNull(List<T> list)
    {
        if (list.isEmpty())
        {
            return null;
        }
        T first = list.get(0);
        for (T element : list)
        {
            if (false == first.equals(element))
            {
                // there are two different results
                return null;
            }
        }
        return first;
    }

}
