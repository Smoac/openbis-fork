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

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.common.utilities.MD5ChecksumCalculator;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellImageChannelStack;
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
public class HCSDatasetLoader implements IHCSDatasetLoader
{
    protected final IImagingReadonlyQueryDAO query;

    protected final ImgDatasetDTO dataset;

    private final String mergedChannelTransformerFactorySignature;
    
    protected ImgContainerDTO container;

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
        long experimentId = getContainer().getExperimentId();
        ImgExperimentDTO experiment = query.tryGetExperimentById(experimentId);
        mergedChannelTransformerFactorySignature = getSignature(experiment.getSerializedImageTransformerFactory());
        this.channels =
                query.getChannelsByDatasetIdOrExperimentId(getDataset().getId(), experimentId);
    }

    protected final ImgContainerDTO getContainer()
    {
        if (container == null)
        {
            container = query.getContainerById(dataset.getContainerId());
        }
        return container;
    }

    protected final ImgDatasetDTO getDataset()
    {
        return dataset;
    }

    public int getChannelCount()
    {
        return channels.size();
    }

    public List<WellImageChannelStack> listImageChannelStacks(WellLocation wellLocation)
    {
        int spotYRow = wellLocation.getRow();
        int spotXColumn = wellLocation.getColumn();
        List<ImgChannelStackDTO> stacks =
                query.listChannelStacks(dataset.getId(), spotXColumn, spotYRow);
        return convert(stacks);
    }

    private static List<WellImageChannelStack> convert(List<ImgChannelStackDTO> stacks)
    {
        List<WellImageChannelStack> result = new ArrayList<WellImageChannelStack>();
        for (ImgChannelStackDTO stack : stacks)
        {
            result.add(convert(stack));
        }
        return result;
    }

    private static WellImageChannelStack convert(ImgChannelStackDTO stack)
    {
        return new WellImageChannelStack(stack.getId(), stack.getRow(), stack.getColumn(),
                stack.getT(), stack.getZ());
    }

    public PlateImageParameters getImageParameters()
    {
        PlateImageParameters params = new PlateImageParameters();
        params.setDatasetCode(dataset.getPermId());
        params.setRowsNum(getContainer().getNumberOfRows());
        params.setColsNum(getContainer().getNumberOfColumns());
        params.setTileRowsNum(getDataset().getFieldNumberOfRows());
        params.setTileColsNum(getDataset().getFieldNumberOfColumns());
        params.setIsMultidimensional(dataset.getIsMultidimensional());
        params.addTransformerFactorySignatureFor(ScreeningConstants.MERGED_CHANNELS,
                mergedChannelTransformerFactorySignature);
        List<String> channelsCodes = new ArrayList<String>();
        List<String> channelsLabels = new ArrayList<String>();
        for (ImgChannelDTO channel : channels)
        {
            // TODO 2010-11-19, IA: is this escaping needed?
            String channelCode = StringEscapeUtils.escapeCsv(channel.getCode());
            channelsCodes.add(channelCode);
            channelsLabels.add(StringEscapeUtils.escapeCsv(channel.getLabel()));
            params.addTransformerFactorySignatureFor(channelCode, getSignature(channel
                    .getSerializedImageTransformerFactory()));
        }
        params.setChannelsCodes(channelsCodes);
        params.setChannelsLabels(channelsLabels);
        return params;
    }
    
    private String getSignature(byte[] bytesOrNull)
    {
        if (bytesOrNull == null)
        {
            return null;
        }
        return MD5ChecksumCalculator.calculate(bytesOrNull).substring(0, 10);
    }
}