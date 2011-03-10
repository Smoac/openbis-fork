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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.Constants;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetShareId;

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
            this.shareId = shareId == null ? Constants.DEFAULT_SHARE_ID : shareId;
            this.lockingTimeOut = lockingTimeOut;
        }

        String getShareId()
        {
            return shareId;
        }

        void setShareId(String shareId)
        {
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

        void await()
        {
            if (countDownLatch != null)
            {
                try
                {
                    boolean successful = countDownLatch.await(lockingTimeOut, TimeUnit.SECONDS);
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

    private final Map<String, Set<Thread>> lockedDataSets = new HashMap<String, Set<Thread>>();

    private final Object dataSetCodeToShareIdMapMonitor = new Object();

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

    public boolean isKnown(String dataSetCode)
    {
        return getDataSetCodeToShareIdMap().containsKey(dataSetCode);
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
            operationLog.info("Register data set " + dataSetCode + " for share " + shareId);
        }
    }

    public void lock(String dataSetCode)
    {
        synchronized (lockedDataSets)
        {
            Set<Thread> set = lockedDataSets.get(dataSetCode);
            if (set == null)
            {
                set = new LinkedHashSet<Thread>();
                GuardedShareID guardedShareId = getGuardedShareId(dataSetCode);
                lockedDataSets.put(dataSetCode, set);
                guardedShareId.lock();
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug("Data set " + dataSetCode + " has been locked.");
                }
            }
            set.add(Thread.currentThread());
            log(dataSetCode, set);
        }
    }

    public void await(String dataSetCode)
    {
        Map<String, GuardedShareID> map = getDataSetCodeToShareIdMap();
        GuardedShareID guardedShareId = map.get(dataSetCode);
        if (guardedShareId != null)
        {
            guardedShareId.await();
        }
    }

    public void releaseLock(String dataSetCode)
    {
        synchronized (lockedDataSets)
        {
            Set<Thread> set = lockedDataSets.get(dataSetCode);
            if (set == null)
            {
                return;
            }
            set.remove(Thread.currentThread());
            if (set.isEmpty())
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug("Unlock data set " + dataSetCode);
                }
                lockedDataSets.remove(dataSetCode);
                getGuardedShareId(dataSetCode).unlock();
            }
            log(dataSetCode, set);
        }
    }

    public void releaseLocks()
    {
        synchronized (lockedDataSets)
        {
            List<String> dataSets = new ArrayList<String>(lockedDataSets.keySet());
            for (String dataSet : dataSets)
            {
                releaseLock(dataSet);
            }
        }
    }

    private void log(String dataSetCode, Set<Thread> set)
    {
        if (operationLog.isDebugEnabled() && set.isEmpty() == false)
        {
            StringBuilder builder = new StringBuilder();
            for (Thread thread : set)
            {
                if (builder.length() > 0)
                {
                    builder.append(", ");
                }
                builder.append(thread.getName());
            }
            operationLog.debug("Data set " + dataSetCode + " is locked by the following threads: "
                    + builder);
        }
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
        synchronized (dataSetCodeToShareIdMapMonitor)
        {
            if (dataSetCodeToShareIdMap == null)
            {
                List<DataSetShareId> dataSets = service.listDataSetShareIds();
                dataSetCodeToShareIdMap = new HashMap<String, GuardedShareID>();
                for (DataSetShareId dataSet : dataSets)
                {
                    String dataSetCode = dataSet.getDataSetCode();
                    String shareId = dataSet.getShareId();
                    addShareId(dataSetCodeToShareIdMap, dataSetCode, shareId);
                }
                operationLog.info("Share id manager initialized with " + dataSets.size()
                        + " data sets.");
            }
            return dataSetCodeToShareIdMap;
        }
    }

}
