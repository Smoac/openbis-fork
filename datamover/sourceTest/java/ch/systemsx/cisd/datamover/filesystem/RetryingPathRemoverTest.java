/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover.filesystem;

import java.io.File;
import java.util.concurrent.Callable;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.datamover.filesystem.RetryingPathRemover.DeleteActivityDetector;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { RetryingPathRemover.class, DeleteActivityDetector.class })
public class RetryingPathRemoverTest
{
    @BeforeClass
    public void init()
    {
        LogInitializer.init();
    }

    @Test
    public void testDeleteAndMonitorHappyCase()
    {
        testDeleteAndMonitor(false);
    }

    @Test
    public void testDeleteAndMonitorBlockedFail()
    {
        testDeleteAndMonitor(true);
    }

    public void testDeleteAndMonitor(final boolean shouldNotBlock)
    {
        RetryingPathRemover remover = new RetryingPathRemover(0, 0);
        final long timeout = 20;
        final DeleteActivityDetector sensor = new DeleteActivityDetector(new File("."));
        Callable<Boolean> callable = new Callable<Boolean>()
            {
                public Boolean call() throws Exception
                {
                    for (int i = 0; i < 20; i++)
                    {
                        Thread.sleep(timeout / 2); // simulates delete
                        sensor.update();
                    }
                    if (shouldNotBlock == false)
                    {
                        Thread.sleep(timeout * 4); // simulates blocked delete
                        AssertJUnit.fail("operation should be killed till this point");
                    }
                    return true;
                }
            };
        Boolean result = remover.executeAndMonitor(sensor, callable, timeout);
        AssertJUnit.assertEquals(shouldNotBlock, result.booleanValue());
    }
}