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

package ch.systemsx.cisd.openbis.dss.etl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageFileExtractionResult.Channel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgDatasetDTO;

/**
 * Helper class for retrieving and/or creating entities associated with the screening container data
 * set info in the DB.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ScreeningContainerDatasetInfoHelper
{
    private final IImagingQueryDAO dao;

    public ScreeningContainerDatasetInfoHelper(IImagingQueryDAO dao)
    {
        this.dao = dao;
    }

    public ExperimentAndContainerIds getOrCreateExperimentAndContainer(
            ScreeningContainerDatasetInfo info)
    {
        return getOrCreateExperimentAndContainer(dao, info);
    }

    public long createFeatureVectorDataset(long contId, ScreeningContainerDatasetInfo info)
    {
        boolean isMultidimensional = false;
        ImgDatasetDTO dataset =
                new ImgDatasetDTO(info.getDatasetPermId(), 0, 0, contId, isMultidimensional);
        return dao.addDataset(dataset);
    }

    // Package-visible static methods

    public static long createImageDataset(IImagingQueryDAO dao, ImageDatasetInfo info, long contId)
    {
        ImgDatasetDTO dataset =
                new ImgDatasetDTO(info.getDatasetPermId(), info.getTileRows(), info
                        .getTileColumns(), contId, info.hasImageSeries());
        return dao.addDataset(dataset);
    }

    /**
     * NOTE: Code responsible for trying to get sample and experiment from the DB and creating them
     * if they don't exist is in synchronized block and uses currently opened transaction. Then the
     * transaction is closed and data set is added to the DB in second transaction. If second
     * transaction will be rolled back sample and experiment created in first transaction will stay
     * in the DB.
     */
    public static ExperimentAndContainerIds getOrCreateExperimentAndContainer(IImagingQueryDAO dao,
            ScreeningContainerDatasetInfo info)
    {
        synchronized (IImagingQueryDAO.class)
        {
            CreatedOrFetchedEntity exp = getOrCreateExperiment(dao, info);
            CreatedOrFetchedEntity cont = getOrCreateContainer(dao, info, exp.getId());
            if (exp.hasAlreadyExisted() == false || cont.hasAlreadyExisted() == false)
            {
                // without this commit other threads will not see the new experiment/sample when the
                // synchronized block ends
                dao.commit();
            }
            return new ExperimentAndContainerIds(exp.getId(), cont.getId());
        }
    }

    /**
     * NOTE: Code responsible for trying to get sample and experiment from the DB and creating them
     * if they don't exist is in synchronized block and uses currently opened transaction. Then the
     * transaction is closed and data set is added to the DB in second transaction. If second
     * transaction will be rolled back sample and experiment created in first transaction will stay
     * in the DB.
     */
    public static ExperimentWithChannelsAndContainer getOrCreateExperimentWithChannelsAndContainer(
            IImagingQueryDAO dao, ScreeningContainerDatasetInfo info,
            Set<HCSImageFileExtractionResult.Channel> channels)
    {
        ScreeningContainerDatasetInfoHelper helper = new ScreeningContainerDatasetInfoHelper(dao);
        synchronized (IImagingQueryDAO.class)
        {
            CreatedOrFetchedEntity exp = getOrCreateExperiment(dao, info);
            long expId = exp.getId();
            CreatedOrFetchedEntity cont = getOrCreateContainer(dao, info, expId);
            Map<String, Long/* (tech id */> channelsMap =
                    helper.getOrCreateChannels(expId, channels);
            if (exp.hasAlreadyExisted() == false || cont.hasAlreadyExisted() == false)
            {
                // without this commit other threads will not see the new experiment/sample when the
                // synchronized block ends
                dao.commit();
            }
            return new ExperimentWithChannelsAndContainer(expId, cont.getId(), channelsMap);
        }
    }

    private static CreatedOrFetchedEntity getOrCreateContainer(IImagingQueryDAO dao,
            ScreeningContainerDatasetInfo info, long expId)
    {
        String containerPermId = info.getContainerPermId();
        Long containerId = dao.tryGetContainerIdPermId(containerPermId);
        if (containerId != null)
        {
            return new CreatedOrFetchedEntity(true, containerId);
        } else
        {
            ImgContainerDTO container =
                    new ImgContainerDTO(containerPermId, info.getContainerRows(), info
                            .getContainerColumns(), expId);
            containerId = dao.addContainer(container);
            return new CreatedOrFetchedEntity(false, containerId);
        }
    }

    private static CreatedOrFetchedEntity getOrCreateExperiment(IImagingQueryDAO dao,
            ScreeningContainerDatasetInfo info)
    {
        String experimentPermId = info.getExperimentPermId();
        Long expId = dao.tryGetExperimentIdByPermId(experimentPermId);
        if (expId != null)
        {
            return new CreatedOrFetchedEntity(true, expId);
        } else
        {
            expId = dao.addExperiment(experimentPermId);
            return new CreatedOrFetchedEntity(false, expId);
        }
    }

    private static class CreatedOrFetchedEntity
    {
        private final boolean alreadyExisted;

        private final long id;

        public CreatedOrFetchedEntity(boolean alreadyExisted, long id)
        {
            this.alreadyExisted = alreadyExisted;
            this.id = id;
        }

        public boolean hasAlreadyExisted()
        {
            return alreadyExisted;
        }

        public long getId()
        {
            return id;
        }
    }

    public static class ExperimentAndContainerIds
    {
        private final long experimentId;

        private final long containerId;

        public ExperimentAndContainerIds(long experimentId, long containerId)
        {
            this.experimentId = experimentId;
            this.containerId = containerId;
        }

        public long getExperimentId()
        {
            return experimentId;
        }

        public long getContainerId()
        {
            return containerId;
        }
    }

    public static class ExperimentWithChannelsAndContainer extends ExperimentAndContainerIds
    {
        private final Map<String, Long/* (tech id */> channelsMap;

        public ExperimentWithChannelsAndContainer(long experimentId, long containerId,
                Map<String, Long> channelsMap)
        {
            super(experimentId, containerId);
            this.channelsMap = channelsMap;
        }

        public Map<String, Long> getChannelsMap()
        {
            return channelsMap;
        }
    }

    // ------ channels creation ------------------------------

    private Map<String, Long> getOrCreateChannels(long expId,
            Set<HCSImageFileExtractionResult.Channel> channels)
    {
        List<ImgChannelDTO> allChannels = dao.getChannelsByExperimentId(expId);
        if (allChannels.size() == 0)
        {
            return createChannels(expId, channels);
        } else
        {
            return updateChannels(expId, channels, allChannels);
        }
    }

    private Map<String, Long> updateChannels(long expId, Set<Channel> channels,
            List<ImgChannelDTO> allChannels)
    {
        Map<String/* name */, ImgChannelDTO> existingChannels = asNameMap(allChannels);
        Map<String, Long> map = new HashMap<String, Long>();
        for (HCSImageFileExtractionResult.Channel channel : channels)
        {
            ImgChannelDTO channelDTO = updateChannel(channel, expId, existingChannels);
            addChannel(map, channelDTO);
        }
        return map;
    }

    private Map<String, Long> createChannels(long expId, Set<Channel> channels)
    {
        Map<String, Long> map = new HashMap<String, Long>();
        for (HCSImageFileExtractionResult.Channel channel : channels)
        {
            ImgChannelDTO channelDTO = createChannel(expId, channel);
            addChannel(map, channelDTO);
        }
        return map;
    }

    private static void addChannel(Map<String, Long> map, ImgChannelDTO channelDTO)
    {
        map.put(channelDTO.getName(), channelDTO.getId());
    }

    private static Map<String, ImgChannelDTO> asNameMap(List<ImgChannelDTO> channels)
    {
        Map<String, ImgChannelDTO> nameMap = new HashMap<String, ImgChannelDTO>();
        for (ImgChannelDTO channel : channels)
        {
            nameMap.put(channel.getName(), channel);
        }
        return nameMap;
    }

    private ImgChannelDTO updateChannel(HCSImageFileExtractionResult.Channel channel, long expId,
            Map<String, ImgChannelDTO> existingChannels)
    {
        ImgChannelDTO channelDTO = makeChannelDTO(channel, expId);
        String channelName = channelDTO.getName();
        ImgChannelDTO existingChannel = existingChannels.get(channelName);
        if (existingChannel == null)
        {
            throw createInvalidNewChannelException(expId, existingChannels, channelName);
        }
        // a channel with a specified name already exists for an experiment, its description
        // will be updated. Wavelength will be updated only if it was null before.
        if (channelDTO.getWavelength() == null)
        {
            channelDTO.setWavelength(existingChannel.getWavelength());
        }
        if (existingChannel.getWavelength() != null
                && existingChannel.getWavelength().equals(channelDTO.getWavelength()) == false)
        {
            throw UserFailureException.fromTemplate(
                    "There are already datasets registered for the experiment "
                            + "which use the same channel name, but with a different wavelength! "
                            + "Channel %s, old wavelength %d, new wavelength %d.", channelName,
                    existingChannel.getWavelength(), channelDTO.getWavelength());
        }
        channelDTO.setId(existingChannel.getId());
        dao.updateChannel(channelDTO);
        return channelDTO;
    }

    private static UserFailureException createInvalidNewChannelException(long expId,
            Map<String, ImgChannelDTO> existingChannels, String channelName)
    {
        return UserFailureException.fromTemplate(
                "Experiment with id '%d' has already some channels registered "
                        + "and does not have a channel with a name '%s'. "
                        + "Register a new experiment to use new channels. "
                        + "Available channel names in this experiment: %s.", expId, channelName,
                existingChannels.keySet());
    }

    private ImgChannelDTO createChannel(long expId, HCSImageFileExtractionResult.Channel channel)
    {
        ImgChannelDTO channelDTO = makeChannelDTO(channel, expId);
        long channelId = dao.addChannel(channelDTO);
        channelDTO.setId(channelId);
        return channelDTO;
    }

    private static ImgChannelDTO makeChannelDTO(HCSImageFileExtractionResult.Channel channel,
            long expId)
    {
        return ImgChannelDTO.createExperimentChannel(channel.getName(),
                channel.tryGetDescription(), channel.tryGetWavelength(), expId);
    }
}
