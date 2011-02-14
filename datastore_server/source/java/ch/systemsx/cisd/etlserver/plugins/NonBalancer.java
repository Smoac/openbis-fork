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

import java.util.List;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Implementation of {@link ISegmentedStoreBalancer} which just logs information about each share.
 *
 * @author Franz-Josef Elmer
 */
public class NonBalancer implements ISegmentedStoreBalancer
{

    private static final int N = 3;

    public void balanceStore(List<Share> shares, IEncapsulatedOpenBISService service,
            IDataSetMover dataSetMover, ISimpleLogger logger)
    {
        logger.log(INFO, "Data Store Shares:");
        for (Share share : shares)
        {
            List<SimpleDataSetInformationDTO> dataSets = share.getDataSetsOrderedBySize();
            logger.log(
                    INFO,
                    "   Share " + share.getShareId() + " (free space: "
                            + FileUtils.byteCountToDisplaySize(share.calculateFreeSpace())
                            + ") has " + dataSets.size() + " data sets occupying "
                            + FileUtilities.byteCountToDisplaySize(share.getTotalSizeOfDataSets())
                            + ".");
            for (int i = 0, n = Math.min(N, dataSets.size()); i < n; i++)
            {
                SimpleDataSetInformationDTO dataSet = dataSets.get(i);
                logger.log(
                        INFO,
                        "      " + dataSet.getDataSetCode() + " "
                                + FileUtilities.byteCountToDisplaySize(dataSet.getDataSetSize()));
            }
            if (dataSets.size() > N)
            {
                logger.log(INFO, "      ...");
            }
        }
    }

}
