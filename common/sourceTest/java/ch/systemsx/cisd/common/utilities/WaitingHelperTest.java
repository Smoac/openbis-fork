/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.common.utilities;

import org.apache.commons.lang.time.DateUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.MockLogger;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class WaitingHelperTest extends AssertJUnit
{
    private static final class MockTimeAndWaitingProvider implements ITimeAndWaitingProvider
    {
        private long time;

        MockTimeAndWaitingProvider(long initialTime)
        {
            time = initialTime;
        }

        @Override
        public long getTimeInMilliseconds()
        {
            return time;
        }

        @Override
        public void sleep(long milliseconds)
        {
            time += milliseconds;
        }
    }
    
    private static final class MockWaitingCondition implements IWaitingCondition
    {
        private int numberOfFalse;
        private int numberOfChecks;

        MockWaitingCondition(int numberOfFalse)
        {
            this.numberOfFalse = numberOfFalse;
        }

        @Override
        public boolean conditionFulfilled()
        {
            numberOfChecks++;
            return --numberOfFalse < 0;
        }

        public int getNumberOfChecks()
        {
            return numberOfChecks;
        }

        @Override
        public String toString()
        {
            return "Mock Condition";
        }
        
    }
    
    @Test
    public void testNoTimeOut()
    {
        MockLogger logger = new MockLogger();
        WaitingHelper waitingHelper = new WaitingHelper(5 * DateUtils.MILLIS_PER_HOUR, 
                3 * DateUtils.MILLIS_PER_SECOND, new MockTimeAndWaitingProvider(30000), logger);
        MockWaitingCondition condition = new MockWaitingCondition(1000);
        
        boolean success = waitingHelper.waitOn(condition);
        
        assertEquals("INFO: Waiting 60sec: Mock Condition\n"
                + "INFO: Waiting 3min: Mock Condition\n"
                + "INFO: Waiting 5min: Mock Condition\n"
                + "INFO: Waiting 10min: Mock Condition\n"
                + "INFO: Waiting 16min: Mock Condition\n"
                + "INFO: Waiting 28min: Mock Condition\n"
                + "INFO: Waiting 46min: Mock Condition\n"
                + "INFO: Fulfilled after 50min: Mock Condition\n", logger.toString());
        assertEquals(true, success);
        assertEquals(1001, condition.getNumberOfChecks());
    }
    
    @Test
    public void testTimeOut()
    {
        MockLogger logger = new MockLogger();
        WaitingHelper waitingHelper = new WaitingHelper(5 * DateUtils.MILLIS_PER_HOUR, 
                3 * DateUtils.MILLIS_PER_SECOND, new MockTimeAndWaitingProvider(30000), logger);
        MockWaitingCondition condition = new MockWaitingCondition(10000);
        
        boolean success = waitingHelper.waitOn(condition);
        
        assertEquals("INFO: Waiting 60sec: Mock Condition\n"
                + "INFO: Waiting 3min: Mock Condition\n"
                + "INFO: Waiting 5min: Mock Condition\n"
                + "INFO: Waiting 10min: Mock Condition\n"
                + "INFO: Waiting 16min: Mock Condition\n"
                + "INFO: Waiting 28min: Mock Condition\n"
                + "INFO: Waiting 46min: Mock Condition\n"
                + "INFO: Waiting 1h 15min: Mock Condition\n"
                + "INFO: Waiting 2h 2min: Mock Condition\n"
                + "INFO: Waiting 3h 2min: Mock Condition\n"
                + "INFO: Waiting 4h 2min: Mock Condition\n", logger.toString());
        assertEquals(false, success);
        assertEquals(6000, condition.getNumberOfChecks());
    }

}
