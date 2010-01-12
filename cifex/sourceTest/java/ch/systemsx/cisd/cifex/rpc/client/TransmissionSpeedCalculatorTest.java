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

package ch.systemsx.cisd.cifex.rpc.client;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.cifex.rpc.client.TransmissionSpeedCalculator;
import ch.systemsx.cisd.common.utilities.ITimeProvider;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class TransmissionSpeedCalculatorTest extends AssertJUnit
{
    private TransmissionSpeedCalculator transmissionSpeedCalculator;

    private ITimeProvider timeProvider;

    static private final class TestTimeProvider implements ITimeProvider
    {
        private long timeInMillisecs = 0;

        public long getTimeInMilliseconds()
        {
            long time = timeInMillisecs;
            timeInMillisecs += 100;
            return time;
        }

    }

    @BeforeMethod
    public void setUp()
    {
        timeProvider = new TestTimeProvider();

        transmissionSpeedCalculator = new TransmissionSpeedCalculator(timeProvider);
    }

    @Test
    public void testNormalTransmission() throws Exception
    {
        transmissionSpeedCalculator.noteTransmittedBytesSinceLastUpdate(0);
        transmissionSpeedCalculator.noteTransmittedBytesSinceLastUpdate(100);
        transmissionSpeedCalculator.noteTransmittedBytesSinceLastUpdate(120);
        transmissionSpeedCalculator.noteTransmittedBytesSinceLastUpdate(90);
        assertEquals(1.0333333f, transmissionSpeedCalculator.getEstimatedBytesPerMillisecond());
    }

    @Test
    public void testResumeTransmission() throws Exception
    {
        transmissionSpeedCalculator.noteTransmittedBytesSinceLastUpdate(1000);
        transmissionSpeedCalculator.noteTransmittedBytesSinceLastUpdate(100);
        transmissionSpeedCalculator.noteTransmittedBytesSinceLastUpdate(120);
        transmissionSpeedCalculator.noteTransmittedBytesSinceLastUpdate(90);
        assertEquals(1.0333333f, transmissionSpeedCalculator.getEstimatedBytesPerMillisecond());
    }

}
