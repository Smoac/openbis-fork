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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Immplementation of {@link IShareIdManager} based on {@link CountDownLatch}.
 *
 * @author Franz-Josef Elmer
 */
public class ShareIdManager implements IShareIdManager
{
    private static final class GuardedShareID
    {
        private final int lockingTimeOut;
        
        private final String dataSetCode;
        
        private CountDownLatch countDownLatch;
        
        private String shareId;

        GuardedShareID(String dataSetCode, String shareId, int lockingTimeOut)
        {
            this.dataSetCode = dataSetCode;
            this.shareId = shareId;
            this.lockingTimeOut = lockingTimeOut;
        }

        String getShareId()
        {
            return shareId;
        }
        
        void setShareId(String shareId)
        {
            await();
            this.shareId = shareId;
        }
        
        void lock()
        {
            countDownLatch = new CountDownLatch(1);
        }
        
        void unlock()
        {
            if (countDownLatch != null)
            {
                countDownLatch.countDown();
            }
        }
        
        private void await()
        {
            if (countDownLatch != null)
            {
                try
                {
                    boolean successful  = countDownLatch.await(lockingTimeOut, TimeUnit.SECONDS);
                    if (successful == false)
                    {
                        throw new EnvironmentFailureException("Lock for data set " + dataSetCode
                                + " hasn't been released after time out of " + lockingTimeOut
                                + " seconds.");
                    }
                } catch (InterruptedException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }
    }
    
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ShareIdManager.class);

    private final IEncapsulatedOpenBISService service;
    private final int lockingTimeOut;
    
    private Map<String, GuardedShareID> dataSetCodeToShareIdMap;

    public ShareIdManager(IEncapsulatedOpenBISService service, int lockingTimeOutInSeconds)
    {
        this.service = service;
        this.lockingTimeOut = lockingTimeOutInSeconds;
    }

    private void addShareId(Map<String, GuardedShareID> map, String dataSetCode, String shareId)
    {
        GuardedShareID guardedShareId = new GuardedShareID(dataSetCode, shareId, lockingTimeOut);
        map.put(dataSetCode, guardedShareId);
    }

    public String getShareId(String dataSetCode)
    {
        return getGuardedShareId(dataSetCode).getShareId();
    }

    public void setShareId(String dataSetCode, String shareId)
    {
        Map<String, GuardedShareID> map = getDataSetCodeToShareIdMap();
        GuardedShareID guardedShareId = map.get(dataSetCode);
        if (guardedShareId != null)
        {
            guardedShareId.setShareId(shareId);
            operationLog.info("New share of data set " + dataSetCode + " is " + shareId);
        } else
        {
            addShareId(map, dataSetCode, shareId);
            operationLog.info("Data set " + dataSetCode + " for share " + shareId);
        }
    }

    public void lock(String dataSetCode)
    {
        getGuardedShareId(dataSetCode).lock();
    }

    public void releaseLock(String dataSetCode)
    {
        getGuardedShareId(dataSetCode).unlock();
    }

    private GuardedShareID getGuardedShareId(String dataSetCode)
    {
        GuardedShareID shareId = getDataSetCodeToShareIdMap().get(dataSetCode);
        if (shareId == null)
        {
            throw new IllegalArgumentException("Unknown data set: " + dataSetCode);
        }
        return shareId;
    }
    
    private Map<String, GuardedShareID> getDataSetCodeToShareIdMap()
    {
        if (dataSetCodeToShareIdMap == null)
        {
            dataSetCodeToShareIdMap  =
                new HashMap<String, GuardedShareID>();
            List<SimpleDataSetInformationDTO> dataSets = service.listDataSets();
            for (SimpleDataSetInformationDTO dataSet : dataSets)
            {
                String dataSetCode = dataSet.getDataSetCode();
                addShareId(dataSetCodeToShareIdMap, dataSetCode, dataSet.getDataSetShareId());
            }
            operationLog.info("Share id manager initialized with " + dataSets.size() + " data sets.");
        }
        return dataSetCodeToShareIdMap;
    }
    
}
