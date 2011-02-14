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

package ch.systemsx.cisd.etlserver.plugins;

import static ch.systemsx.cisd.common.logging.LogLevel.INFO;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Simple shuffling which moves data sets from full shares to the share with initial most free space
 * until it is full.
 * 
 * @author Franz-Josef Elmer
 */
public class SimpleShuffling implements ISegmentedStoreShuffling
{
    @Private static final String MINIMUM_FREE_SPACE_KEY = "minimum-free-space-in-MB";

    private static final class ShareState
    {
        private final Share share;
        private long freeSpace;

        ShareState(Share share)
        {
            this.share = share;
            recalculateFreeSpace();
        }
        
        private void recalculateFreeSpace()
        {
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

        void removeDataSet(int dataSetIndex)
        {
            share.getDataSetsOrderedBySize().remove(dataSetIndex);
            recalculateFreeSpace();
        }

        void addDataSet(SimpleDataSetInformationDTO dataSet)
        {
            List<SimpleDataSetInformationDTO> dataSets = share.getDataSetsOrderedBySize();
            int index = Collections.binarySearch(dataSets, dataSet, Share.DATA_SET_SIZE_COMPARATOR);
            if (index < 0)
            {
                index = -index - 1;
            }
            dataSets.add(index, dataSet);
            recalculateFreeSpace();
        }
    }
    
    private final long minimumFreeSpace;
    private final ITimeProvider timeProvider;

    public SimpleShuffling(Properties properties)
    {
        this(properties, SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }
    
    SimpleShuffling(Properties properties, ITimeProvider timeProvider)
    {
        this.timeProvider = timeProvider;
        minimumFreeSpace =
            FileUtils.ONE_MB
            * PropertyUtils.getLong(properties, MINIMUM_FREE_SPACE_KEY, 1024);
        
    }
    
    public void shuffleDataSets(List<Share> sourceShares, List<Share> targetShares,
            IEncapsulatedOpenBISService service, IDataSetMover dataSetMover, ISimpleLogger logger)
    {
        List<ShareState> shareStates = getSortedShares(targetShares);
        ShareState shareWithMostFree = shareStates.get(shareStates.size() - 1);
        List<ShareState> fullShares = getFullShares(sourceShares);
        for (ShareState fullShare : fullShares)
        {
            List<SimpleDataSetInformationDTO> dataSets =
                    fullShare.getShare().getDataSetsOrderedBySize();
            long initalFreeSpaceAboveMinimum = fullShare.getFreeSpace() - minimumFreeSpace;
            int numberOfDataSetsToMove =
                    getNumberOfDataSetsToMove(dataSets, initalFreeSpaceAboveMinimum);
            if (numberOfDataSetsToMove < 0)
            {
                throw new IllegalStateException("Share " + fullShare.getShare().getShareId()
                        + " has not enough free space even if it is empty.");
            }
            for (int i = 0; i < numberOfDataSetsToMove; i++)
            {
                long dataSetSize = dataSets.get(i).getDataSetSize();
                if (shareWithMostFree.getFreeSpace() - dataSetSize > minimumFreeSpace)
                {
                    copy(fullShare, 0, shareWithMostFree, dataSetMover, logger);
                }
            }
        }
    }

    private int getNumberOfDataSetsToMove(List<SimpleDataSetInformationDTO> dataSets,
            long initalFreeSpaceAboveMinimum)
    {
        long freeSpaceAboveMinimum = initalFreeSpaceAboveMinimum;
        for (int i = 0; i < dataSets.size(); i++)
        {
            if (freeSpaceAboveMinimum > 0)
            {
                return i;
            }
            freeSpaceAboveMinimum += dataSets.get(i).getDataSetSize();
        }
        return freeSpaceAboveMinimum > 0 ? dataSets.size() : -1;
    }

    private void copy(ShareState from, int dataSetIndex, ShareState to,
            IDataSetMover mover, ISimpleLogger logger)
    {
        Share fromShare = from.getShare();
        Share toShare = to.getShare();
        SimpleDataSetInformationDTO dataSet =
                fromShare.getDataSetsOrderedBySize().get(dataSetIndex);
        File dataSetDirInStore = new File(fromShare.getShare(), dataSet.getDataSetLocation());
        String commonMessage =
                "Moving data set " + dataSet.getDataSetCode() + " from share " + fromShare.getShareId()
                        + " to share " + toShare.getShareId();
        logger.log(INFO, commonMessage + " ...");
        long t0 = timeProvider.getTimeInMilliseconds();
        mover.moveDataSetToAnotherShare(dataSetDirInStore, toShare.getShare());
        from.removeDataSet(dataSetIndex);
        to.addDataSet(dataSet);
        logger.log(INFO, commonMessage + " took "
                + ((timeProvider.getTimeInMilliseconds() - t0 + 500) / 1000) + " seconds.");
    }

    private List<ShareState> getFullShares(List<Share> sourceShares)
    {
        List<ShareState> fullShares = new ArrayList<ShareState>();
        for (ShareState shareState : getSortedShares(sourceShares))
        {
            if (shareState.getFreeSpace() < minimumFreeSpace)
            {
                fullShares.add(shareState);
            }
        }
        return fullShares;
    }

    private List<ShareState> getSortedShares(List<Share> shares)
    {
        List<ShareState> shareStates = new ArrayList<ShareState>();
        for (Share share : shares)
        {
            shareStates.add(new ShareState(share));
        }
        Collections.sort(shareStates, new Comparator<ShareState>()
            {
                public int compare(ShareState o1, ShareState o2)
                {
                    long s1 = o1.getFreeSpace();
                    long s2 = o2.getFreeSpace();
                    return s1 < s2 ? -1 : (s1 > s2 ? 1 : 0);
                }
            });
        return shareStates;
    }

}
