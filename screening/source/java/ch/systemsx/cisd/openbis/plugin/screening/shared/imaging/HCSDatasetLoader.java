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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgDatasetDTO;

/**
 * Helper class for easy handling of HCS image dataset standard structure with no code for handling
 * images.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public class HCSDatasetLoader implements IHCSDatasetLoader
{
    protected final IImagingQueryDAO query;

    protected final ImgDatasetDTO dataset;

    protected ImgContainerDTO container;

    protected Integer channelCount;

    protected List<String> channelNames;

    public HCSDatasetLoader(IImagingQueryDAO query, String datasetPermId)
    {
        this.query = query;
        this.dataset = query.tryGetDatasetByPermId(datasetPermId);
        if (dataset == null)
        {
            throw new IllegalStateException(String.format("Dataset '%s' not found", datasetPermId));
        }
    }

    /** has to be called at the end */
    public void close()
    {
        query.close();
    }

    protected final ImgContainerDTO getContainer()
    {
        if (container == null)
        {
            container = query.getContainerById(dataset.getContainerId());
        }
        return container;
    }

    public Geometry getPlateGeometry()
    {
        return new Geometry(getContainer().getNumberOfRows(), getContainer().getNumberOfColumns());
    }

    protected final ImgDatasetDTO getDataset()
    {
        return dataset;
    }

    public String getDatasetPermId()
    {
        return dataset.getPermId();
    }

    public Geometry getWellGeometry()
    {
        return new Geometry(getDataset().getFieldNumberOfRows(), getDataset()
                .getFieldNumberOfColumns());
    }

    public int getChannelCount()
    {
        if (channelCount == null)
        {
            channelCount = getChannelsNames().size();
        }
        return channelCount;
    }

    public List<String> getChannelsNames()
    {
        if (channelNames == null)
        {
            String[] namesAsArray =
                    query.getChannelNamesByDatasetIdOrExperimentId(getDataset().getId(),
                            getContainer().getExperimentId());
            channelNames = new ArrayList<String>(Arrays.asList(namesAsArray));
        }
        return channelNames;
    }

    public boolean isMultidimensional()
    {
        return dataset.getIsMultidimensional();
    }
}