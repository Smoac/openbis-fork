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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import static org.apache.commons.io.FileUtils.ONE_MB;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SpeedOptimizedShareFinderTest extends AssertJUnit
{
    private static final String DATA_SET_CODE = "ds-1";
    
    private SpeedOptimizedShareFinder finder;
    
    private Mockery context;
    private IFreeSpaceProvider freeSpaceProvider;
    private SimpleDataSetInformationDTO dataSet;
    
    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        freeSpaceProvider = context.mock(IFreeSpaceProvider.class);
        dataSet = new SimpleDataSetInformationDTO();
        dataSet.setDataSetCode(DATA_SET_CODE);
        dataSet.setDataSetSize(FileUtils.ONE_MB);
        finder = new SpeedOptimizedShareFinder(new Properties());
    }
    
    @AfterMethod
    public void tearDown(Method method)
    {
        try
        {
            context.assertIsSatisfied();
        } catch (Throwable t)
        {
            // assert expectations were met, including the name of the failed method
            throw new Error(method.getName() + "() : ", t);
        }
    }
    
    @Test
    public void testFindMatchingExtensionShare()
    {
        dataSet.setSpeedHint(-50);
        Share s1 = share(false, 50, 10 * ONE_MB);
        Share s2 = share(true, 50, 0);
        Share s3 = share(false, 40, 0);
        Share s4 = share(false, 50, 11 * ONE_MB);
        
        Share foundShare = finder.tryToFindShare(dataSet, Arrays.asList(s1, s2, s3, s4));
        assertSame(s4.getShare(), foundShare.getShare());
    }
    
    @Test
    public void testFindExtensionShareRespectingSpeedHint()
    {
        dataSet.setSpeedHint(-50);
        Share s1 = share(false, 49, 10 * ONE_MB);
        Share s2 = share(true, 50, 0);
        Share s3 = share(false, 40, 11 * ONE_MB);
        
        Share foundShare = finder.tryToFindShare(dataSet, Arrays.asList(s1, s2, s3));
        assertSame(s3.getShare(), foundShare.getShare());
    }
    
    @Test
    public void testFindShareIgnoringSpeedHint()
    {
        dataSet.setSpeedHint(-50);
        dataSet.setDataSetShareId("20");
        Share s1 = share(false, 51, 10 * ONE_MB);
        Share s2 = share(true, 50, 20 * ONE_MB);
        Share s3 = share(false, 60, 11 * ONE_MB);
        Share s4 = share(true, 50, 15 * ONE_MB);
        
        Share foundShare = finder.tryToFindShare(dataSet, Arrays.asList(s1, s2, s3, s4));
        assertSame(s3.getShare(), foundShare.getShare());
    }
    
    private Share share(boolean incoming, int speed, final long freeSpace)
    {
        final File file = new File(Integer.toString(speed) + "/" + incoming + "/" + freeSpace);
        if (freeSpace > 0)
        {
            context.checking(new Expectations()
                {
                    {
                        try
                        {
                            one(freeSpaceProvider).freeSpaceKb(new HostAwareFile(file));
                            will(returnValue(freeSpace / FileUtils.ONE_KB));
                        } catch (IOException ex)
                        {
                            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                        }
                    }
                });
        }
        Share share = new Share(file, speed, freeSpaceProvider);
        share.setIncoming(incoming);
        return share;
    }
}
