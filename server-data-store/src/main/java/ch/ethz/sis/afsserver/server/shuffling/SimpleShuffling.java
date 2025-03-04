/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.afsserver.server.shuffling;

import static ch.systemsx.cisd.common.logging.LogLevel.INFO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import ch.ethz.sis.shared.log.LogManager;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.properties.PropertyUtils;

/**
 * Simple shuffling which moves data sets from full shares to the share with initial most free space until it is full.
 *
 * @author Franz-Josef Elmer
 */
public class SimpleShuffling implements ISegmentedStoreShuffling
{
    @Private
    static final String MINIMUM_FREE_SPACE_KEY = "minimum-free-space-in-MB";

    private static final class ShareAndFreeSpace
    {
        private final Share share;

        private long freeSpace;

        ShareAndFreeSpace(Share share)
        {
            this.share = share;
            freeSpace = share.calculateFreeSpace();
        }

        long getFreeSpace()
        {
            return freeSpace;
        }

        Share getShare()
        {
            return share;
        }
    }

    private final Properties properties;

    private final long minimumFreeSpace;

    private TaskExecutor taskExecutor;

    public SimpleShuffling(Properties properties)
    {
        this.properties = properties;
        minimumFreeSpace =
                FileUtils.ONE_MB * PropertyUtils.getLong(properties, MINIMUM_FREE_SPACE_KEY, 1024);
        taskExecutor = new TaskExecutor(properties, LogManager.getLogger(SimpleShuffling.class));
    }

    @Override
    public void init(ISimpleLogger logger)
    {
        taskExecutor.cleanup();
        logger.log(LogLevel.INFO, "Simple shuffling strategy initialized");
    }

    @Override public void shuffleDataSets(final List<Share> sourceShares, final List<Share> targetShares, final Set<String> incomingShares,
            final IEncapsulatedOpenBISService service, final IFreeSpaceProvider freeSpaceProvider, final IDataSetMover dataSetMover,
            final IConfigProvider configProvider,
            final IChecksumProvider checksumProvider, final ISimpleLogger logger)
    {
        List<ShareAndFreeSpace> fullShares = getFullShares(sourceShares);
        for (ShareAndFreeSpace fullShare : fullShares)
        {
            Share share = fullShare.getShare();

            List<SimpleDataSetInformationDTO> allDataSets = share.getDataSetsOrderedBySize();
            List<SimpleDataSetInformationDTO> dataSetsWithCalculatedSize =
                    allDataSets.stream().filter(dataSet -> dataSet.getDataSetSize() != null).collect(Collectors.toList());

            long initialFreeSpaceAboveMinimum = fullShare.getFreeSpace() - minimumFreeSpace;

            int numberOfDataSetsToMove;
            if (share.isWithdrawShare())
            {
                numberOfDataSetsToMove = dataSetsWithCalculatedSize.size();
                logger.log(INFO, "All " + numberOfDataSetsToMove
                        + " data sets with calculated size will be moved away from share " + share.getShareId());
            } else
            {
                logger.log(INFO,
                        "BEGIN Computing number of data sets to be moved from share " + share.getShareId());
                numberOfDataSetsToMove = getNumberOfDataSetsToMove(dataSetsWithCalculatedSize,
                        initialFreeSpaceAboveMinimum, logger);
                logger.log(INFO,
                        "END Computing number of data sets to move from share " + share.getShareId());
                if (numberOfDataSetsToMove < 0)
                {
                    throw new IllegalStateException("Share " + share.getShareId()
                            + " has not enough free space even if it is empty.");
                }
            }

            EagerShufflingTask shufflingTask =
                    new EagerShufflingTask(properties, incomingShares, service, freeSpaceProvider, dataSetMover, configProvider, checksumProvider);

            for (int i = 0; i < numberOfDataSetsToMove; i++)
            {
                SimpleDataSetInformationDTO dataSet = dataSetsWithCalculatedSize.get(i);
                try
                {
                    taskExecutor.execute(shufflingTask, "shuffling", dataSet.getDataSetCode(), false);
                } catch (Throwable ex)
                {
                    // ignore because it has already been logged. Try the next data set.
                }
            }
        }
    }

    private int getNumberOfDataSetsToMove(List<SimpleDataSetInformationDTO> dataSets,
            long initialFreeSpaceAboveMinimum, ISimpleLogger logger)
    {
        long freeSpaceAboveMinimum = initialFreeSpaceAboveMinimum;

        long spaceBelowMinimum = freeSpaceAboveMinimum * -1;
        float spaceBelowMinimumkB = spaceBelowMinimum / 1024.f;
        float spaceBelowMinimumMB = spaceBelowMinimumkB / 1024.f;
        String freeSpaceString =
                String.format("\tSpace needed to free: %d bytes (%.2f kB, %.2f MB)",
                        spaceBelowMinimum, spaceBelowMinimumkB, spaceBelowMinimumMB);
        logger.log(INFO, freeSpaceString);
        logger.log(INFO, "\tInspecting " + dataSets.size() + " data sets.");
        for (int i = 0; i < dataSets.size(); i++)
        {
            if (freeSpaceAboveMinimum > 0)
            {
                logger.log(INFO, "\t" + i + " data sets to move, available space : "
                        + freeSpaceAboveMinimum);
                return i;
            }
            freeSpaceAboveMinimum += dataSets.get(i).getDataSetSize();
        }
        return freeSpaceAboveMinimum > 0 ? dataSets.size() : -1;
    }

    private List<ShareAndFreeSpace> getFullShares(List<Share> sourceShares)
    {
        List<ShareAndFreeSpace> fullShares = new ArrayList<ShareAndFreeSpace>();
        for (ShareAndFreeSpace shareState : getSortedShares(sourceShares))
        {
            if (shareState.getShare().isWithdrawShare() || shareState.getFreeSpace() < minimumFreeSpace)
            {
                fullShares.add(shareState);
            }
        }
        return fullShares;
    }

    private List<ShareAndFreeSpace> getSortedShares(List<Share> shares)
    {
        List<ShareAndFreeSpace> shareStates = new ArrayList<ShareAndFreeSpace>();
        for (Share share : shares)
        {
            shareStates.add(new ShareAndFreeSpace(share));
        }
        Collections.sort(shareStates, new Comparator<ShareAndFreeSpace>()
        {
            @Override
            public int compare(ShareAndFreeSpace o1, ShareAndFreeSpace o2)
            {
                long s1 = o1.getFreeSpace();
                long s2 = o2.getFreeSpace();
                return s1 < s2 ? -1 : (s1 > s2 ? 1 : 0);
            }
        });
        return shareStates;
    }

}
