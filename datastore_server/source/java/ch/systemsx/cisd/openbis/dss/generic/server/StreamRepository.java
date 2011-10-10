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

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.common.utilities.TokenGenerator;

/**
 * Repository of {@link InputStream} instances.
 *
 * @author Franz-Josef Elmer
 */
public class StreamRepository implements IStreamRepository
{
    private static final class InputStreamWithTimeStamp
    {
        final Date timestamp;
        final InputStreamWithPath inputStreamWithPath;
        InputStreamWithTimeStamp(InputStreamWithPath inputStreamWithPath, Date timestamp)
        {
            this.inputStreamWithPath = inputStreamWithPath;
            this.timestamp = timestamp;
        }
    }
    
    static interface IUniqueIdGenerator
    {
        public String createUniqueID();
    }
    
    private static final class IdGenerator implements IUniqueIdGenerator
    {
        private final TokenGenerator tokenGenerator = new TokenGenerator();

        public String createUniqueID()
        {
            return tokenGenerator.getNewToken(System.currentTimeMillis());
        }
    }
    
    private final Map<String, InputStreamWithTimeStamp> streams = new HashMap<String, InputStreamWithTimeStamp>();
    private final IUniqueIdGenerator inputStreamIDGenerator;
    private final ITimeProvider timeProvider;
    private final long minimumTime;
    
    public StreamRepository(int minimumTimeInSecondsToKeepStreams)
    {
        this(minimumTimeInSecondsToKeepStreams, new IdGenerator(), SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }
    
    StreamRepository(int minimumTimeInSecondsToKeepStreams,
            IUniqueIdGenerator inputStreamIDGenerator, ITimeProvider timeProvider)
    {
        if (minimumTimeInSecondsToKeepStreams <= 0)
        {
            throw new IllegalArgumentException(
                    "Minimum time to keep streams is not a positive number: "
                            + minimumTimeInSecondsToKeepStreams);
        }
        minimumTime = minimumTimeInSecondsToKeepStreams * 1000L;
        this.inputStreamIDGenerator = inputStreamIDGenerator;
        this.timeProvider = timeProvider;
    }

    public synchronized String addStream(InputStream inputStream, String path)
    {
        removeStaleInputStreams();
        String id = inputStreamIDGenerator.createUniqueID();
        Date timestamp = new Date(timeProvider.getTimeInMilliseconds());
        streams.put(id, new InputStreamWithTimeStamp(new InputStreamWithPath(inputStream, path), timestamp));
        return id;
    }

    public synchronized InputStreamWithPath getStream(String inputStreamID)
    {
        removeStaleInputStreams();
        InputStreamWithTimeStamp inputStreamWithTimeStamp = streams.remove(inputStreamID);
        if (inputStreamWithTimeStamp == null)
        {
            throw new IllegalArgumentException("Stream " + inputStreamID + " is no longer available.");
        }
        return inputStreamWithTimeStamp.inputStreamWithPath;
    }
    
    private void removeStaleInputStreams()
    {
        long currentTime = timeProvider.getTimeInMilliseconds();
        Set<Entry<String, InputStreamWithTimeStamp>> entrySet = streams.entrySet();
        for (Iterator<Entry<String, InputStreamWithTimeStamp>> iterator = entrySet.iterator(); iterator
                .hasNext();)
        {
            if (iterator.next().getValue().timestamp.getTime() < currentTime - minimumTime)
            {
                iterator.remove();
            }
        }
    }
    
    
}
