/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

/**
 * Helper class to cache objects retrieved from remote services. Used by
 * {@link FtpPathResolverContext}.
 * 
 * @author Franz-Josef Elmer
 */
public class Cache
{
    private static final long LIVE_TIME = 1000;
    
    private static final class TimeStampedObject<T>
    {
        private final long timestamp;
        private final T object;
        public TimeStampedObject(T object, long timestamp)
        {
            this.object = object;
            this.timestamp = timestamp;
        }
    }
    
    private final Map<String, TimeStampedObject<DataSet>> dataSets = new HashMap<String, Cache.TimeStampedObject<DataSet>>();
    private final Map<String, TimeStampedObject<ExternalData>> externalData = new HashMap<String, Cache.TimeStampedObject<ExternalData>>();
    private final Map<String, TimeStampedObject<Experiment>> experiments = new HashMap<String, Cache.TimeStampedObject<Experiment>>();
    
    private final ITimeProvider timeProvider;
    
    public Cache(ITimeProvider timeProvider)
    {
        this.timeProvider = timeProvider;
    }
    
    void putDataSet(DataSet dataSet)
    {
        dataSets.put(dataSet.getCode(), timestamp(dataSet));
    }
    
    DataSet getDataSet(String dataSetCode)
    {
        return getObject(dataSets, dataSetCode);
    }

    ExternalData getExternalData(String code)
    {
        return getObject(externalData, code);
    }

    void putDataSet(ExternalData dataSet)
    {
        externalData.put(dataSet.getCode(), timestamp(dataSet));
    }

    Experiment getExperiment(String experimentId)
    {
        return getObject(experiments, experimentId);
    }

    void putDataSet(Experiment experiment)
    {
        experiments.put(experiment.getIdentifier(), timestamp(experiment));
    }

    private <T> TimeStampedObject<T> timestamp(T object)
    {
        return new TimeStampedObject<T>(object, timeProvider.getTimeInMilliseconds());
    }

    private <T> T getObject(Map<String, TimeStampedObject<T>> map, String key)
    {
        TimeStampedObject<T> timeStampedObject = map.get(key);
        return timeStampedObject == null
                || timeProvider.getTimeInMilliseconds() - timeStampedObject.timestamp > LIVE_TIME ? null
                : timeStampedObject.object;
    }
    
}