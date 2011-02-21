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

import java.util.Arrays;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ShareIdManagerTest extends AssertJUnit
{
    private static final String DS1 = "ds1";
    private static final String DS2 = "ds2";
    
    private Mockery context;
    private IEncapsulatedOpenBISService service;
    private ShareIdManager manager;
    
    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        context.checking(new Expectations()
            {
                {
                    one(service).listDataSets();
                    SimpleDataSetInformationDTO ds1 = new SimpleDataSetInformationDTO();
                    ds1.setDataSetCode(DS1);
                    SimpleDataSetInformationDTO ds2 = new SimpleDataSetInformationDTO();
                    ds2.setDataSetCode(DS2);
                    ds2.setDataSetShareId("2");
                    will(returnValue(Arrays.asList(ds1, ds2)));
                }
            });
        manager = new ShareIdManager(service, 1);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testUnlockedGetShareId()
    {
        assertEquals(null, manager.getShareId(DS1));
        assertEquals("2", manager.getShareId(DS2));
    }
    
    @Test
    public void testGetShareIdOfUnknownDataSet()
    {
        try
        {
            manager.getShareId("ds?");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Unknown data set: ds?", ex.getMessage());
        }
    }
    
    @Test
    public void testLockOfUnknownDataSet()
    {
        try
        {
            manager.lock("ds?");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Unknown data set: ds?", ex.getMessage());
        }
    }
    
    @Test
    public void testReleaseLockOfUnknownDataSet()
    {
        try
        {
            manager.releaseLock("ds?");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Unknown data set: ds?", ex.getMessage());
        }
    }
    
    @Test
    public void testUnlockedSetShareId()
    {
        manager.setShareId(DS1, "1");
        assertEquals("1", manager.getShareId(DS1));
        assertEquals("2", manager.getShareId(DS2));
    }
    
    @Test
    public void testSetShareIdForNewDataSet()
    {
        manager.setShareId("new data set", "42");
        assertEquals("42", manager.getShareId("new data set"));
    }

    @Test
    public void testLockingTimeOut()
    {
        manager.lock(DS1);
        try
        {
            manager.setShareId(DS1, "1");
            fail("EnvironmentFailureException expected.");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Lock for data set ds1 hasn't been released after time out of 1 seconds.", ex.getMessage());
        }
    }
    
    @Test
    public void testLocking()
    {
        final MessageChannel ch1 = new MessageChannel();
        new Thread(new Runnable()
            {
                public void run()
                {
                    manager.lock(DS1);
                    ch1.send("locked");
                    try
                    {
                        Thread.sleep(200);
                    } catch (InterruptedException ex)
                    {
                        // ignored
                    }
                    manager.releaseLock(DS1);
                    ch1.send("unlocked");
                }
            }).start();
        ch1.assertNextMessage("locked"); // wait until data set is really locked.
        
        manager.setShareId(DS1, "1");
        
        assertEquals("1", manager.getShareId(DS1));
        ch1.assertNextMessage("unlocked"); // wait until thread is finished
    }
}
