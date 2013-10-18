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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.io.File;
import java.lang.reflect.Method;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;

/**
 * @author pkupczyk
 */
public class AttachmentsDropboxTest extends SystemTestCase
{
    // for jython script go to
    // sourceTest/core-plugins/generic-test/1/dss/drop-boxes/attachments-test/attachments-test-handler.py

    @BeforeClass
    public void beforeClass()
    {
        Logger.getLogger("OPERATION.Resources").setLevel(Level.DEBUG);
    }

    @AfterClass
    public void afterClass()
    {
        Logger.getLogger("OPERATION.Resources").setLevel(Level.INFO);
    }

    @BeforeMethod
    public void beforeMethod(Method method)
    {
        logAppender = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
    }

    @AfterMethod
    public void afterMethod(Method method)
    {
        logAppender.reset();
    }

    @Test
    public void testAttachments() throws Exception
    {
        File dataDirectory = new File(workingDirectory, "attachments-test-data");
        dataDirectory.mkdirs();
        FileUtilities.writeToFile(new File(dataDirectory, "test.txt"), "");

        moveFileToIncoming(dataDirectory);
        waitUntilDataSetImported();

        String[] lines = logAppender.getLogContent().split("\n");
        int count = 0;

        for (String line : lines)
        {
            if (line.contains("DEBUG OPERATION.Resources - Successfully released a resource") && line.contains("ReleasableStream"))
            {
                count++;
            }
        }

        Assert.assertEquals(count, 3);
    }

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-attachments-test");
    }

}
