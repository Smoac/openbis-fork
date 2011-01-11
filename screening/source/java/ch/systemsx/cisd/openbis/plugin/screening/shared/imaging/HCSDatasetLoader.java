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
import java.util.List;

import ch.systemsx.cisd.common.utilities.MD5ChecksumCalculator;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelStackDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgExperimentDTO;

/**
 * Helper class for easy handling of HCS image dataset standard structure with no code for handling
 * images.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public class HCSDatasetLoader implements IImageDatasetLoader
{
    protected final IImagingReadonlyQueryDAO query;

    protected final ImgDatasetDTO dataset;

    private final String mergedChannelTransformerFactorySignatureOrNull;

    protected ImgContainerDTO containerOrNull;

    protected ImgExperimentDTO experimentOrNull;

    protected Integer channelCount;

    protected List<ImgChannelDTO> channels;

    public HCSDatasetLoader(IImagingReadonlyQueryDAO query, String datasetPermId)
    {
        this.query = query;

        this.dataset = query.tryGetDatasetByPermId(datasetPermId);
        if (dataset == null)
        {
            throw new IllegalStateException(String.format("Dataset '%s' not found", datasetPermId));
        }

        Long containerId = dataset.getContainerId();
        if (containerId != null)
        {
            this.containerOrNull = query.getContainerById(containerId);
            this.experimentOrNull = query.tryGetExperimentById(containerOrNull.getExperimentId());
        } else
        {
            this.containerOrNull = null;
            this.experimentOrNull = null;
        }

        this.mergedChannelTransformerFactorySignatureOrNull =
                tryGetImageTransformerFactorySignatureForMergedChannels();
        this.channels = loadChannels();
    }

    private List<ImgChannelDTO> loadChannels()
    {
        List<ImgChannelDTO> myChannels = query.getChannelsByDatasetId(dataset.getId());
        if (myChannels.size() == 0 && containerOrNull != null)
        {
            myChannels = query.getChannelsByExperimentId(containerOrNull.getExperimentId());
        }
        return myChannels;
    }

    private String tryGetImageTransformerFactorySignatureForMergedChannels()
    {
        byte[] imageTransformerFactory = dataset.getSerializedImageTransformerFactory();
        if (imageTransformerFactory == null && experimentOrNull != null)
        {
            imageTransformerFactory = experimentOrNull.getSerializedImageTransformerFactory();
        }
        return tryGetSignature(imageTransformerFactory);
    }

    protected final ImgContainerDTO tryGetContainer()
    {
        return containerOrNull;
    }

    protected final ImgDatasetDTO getDataset()
    {
        return dataset;
    }

    public int getChannelCount()
    {
        return channels.size();
    }

    public List<ImageChannelStack> listImageChannelStacks(WellLocation wellLocationOrNull)
    {
        List<ImgChannelStackDTO> stacks;
        if (wellLocationOrNull != null)
        {
            int spotYRow = wellLocationOrNull.getRow();
            int spotXColumn = wellLocationOrNull.getColumn();
            stacks = query.listChannelStacks(dataset.getId(), spotXColumn, spotYRow);
        } else
        {
            stacks = query.listSpotlessChannelStacks(dataset.getId());
        }
        return convert(stacks);
    }

    private static List<ImageChannelStack> convert(List<ImgChannelStackDTO> stacks)
    {
        List<ImageChannelStack> result = new ArrayList<ImageChannelStack>();
        for (ImgChannelStackDTO stack : stacks)
        {
            result.add(convert(stack));
        }
        return result;
    }

    private static ImageChannelStack convert(ImgChannelStackDTO stack)
    {
        return new ImageChannelStack(stack.getId(), stack.getRow(), stack.getColumn(),
                stack.getT(), stack.getZ(), stack.getSeriesNumber());
    }

    public ImageDatasetParameters getImageParameters()
    {
        ImageDatasetParameters params = new ImageDatasetParameters();
        params.setDatasetCode(dataset.getPermId());
        if (containerOrNull != null)
        {
            params.setRowsNum(containerOrNull.getNumberOfRows());
            params.setColsNum(containerOrNull.getNumberOfColumns());
        }
        params.setTileRowsNum(getDataset().getFieldNumberOfRows());
        params.setTileColsNum(getDataset().getFieldNumberOfColumns());
        params.setIsMultidimensional(dataset.getIsMultidimensional());
        params.addTransformerFactorySignatureFor(ScreeningConstants.MERGED_CHANNELS,
                mergedChannelTransformerFactorySignatureOrNull);
        List<String> channelsCodes = new ArrayList<String>();
        List<String> channelsLabels = new ArrayList<String>();
        for (ImgChannelDTO channel : channels)
        {
            String channelCode = channel.getCode();
            channelsCodes.add(channelCode);
            channelsLabels.add(channel.getLabel());
            params.addTransformerFactorySignatureFor(channelCode,
                    tryGetSignature(channel.getSerializedImageTransformerFactory()));
        }
        params.setChannelsCodes(channelsCodes);
        params.setChannelsLabels(channelsLabels);
        return params;
    }

    private String tryGetSignature(byte[] bytesOrNull)
    {
        if (bytesOrNull == null)
        {
            return null;
        }
        return MD5ChecksumCalculator.calculate(bytesOrNull).substring(0, 10);
    }
}