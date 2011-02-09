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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SegmentedStoreUtilsTest extends AbstractFileSystemTestCase
{
    private Mockery context;
    private IEncapsulatedOpenBISService service;
    
    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void test()
    {
        File share1 = new File(workingDirectory, "store/1");
        File share1uuid01 = new File(share1, "uuid/01");
        File ds2 = new File(share1uuid01, "0b/0c/ds-2/original");
        ds2.mkdirs();
        FileUtilities.writeToFile(new File(ds2, "read.me"), "do nothing");
        File dataSetDirInStore = new File(share1uuid01, "02/03/ds-1");
        File original = new File(dataSetDirInStore, "original");
        original.mkdirs();
        FileUtilities.writeToFile(new File(original, "hello.txt"), "hello world");
        File share2 = new File(workingDirectory, "store/2");
        share2.mkdirs();
        File share2uuid01 = new File(share2, "uuid/01");
        File file = new File(share2uuid01, "22/33/orig");
        file.mkdirs();
        FileUtilities.writeToFile(new File(file, "hi.txt"), "hi");
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet("ds-1");
                    will(returnValue(new ExternalData()));
                    
                    one(service).updateShareIdAndSize("ds-1", "2", 11L);
                }
            });
        assertEquals(true, dataSetDirInStore.exists());
        assertFileNames(share2uuid01, "22");
        
        SegmentedStoreUtils.moveDataSetToAnotherShare(dataSetDirInStore, share2, service);

        assertEquals(false, dataSetDirInStore.exists());
        assertFileNames(share2uuid01, "02", "22");
        assertEquals("hello world\n",
                FileUtilities.loadToString(new File(share2uuid01, "02/03/ds-1/original/hello.txt")));
        context.assertIsSatisfied();
    }
    
    private void assertFileNames(File file, String... names)
    {
        File[] files = file.listFiles();
        Arrays.sort(files);
        List<String> actualNames = new ArrayList<String>();
        for (File child : files)
        {
            actualNames.add(child.getName());
        }
        assertEquals(Arrays.asList(names).toString(), actualNames.toString());
    }
}
