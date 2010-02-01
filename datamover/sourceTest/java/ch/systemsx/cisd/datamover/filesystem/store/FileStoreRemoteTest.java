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

package ch.systemsx.cisd.datamover.filesystem.store;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.StoreItem;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandBuilder;
import ch.systemsx.cisd.common.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;

/**
 * Test cases for {@link FileStoreRemote}.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses = FileStoreRemote.class)
public final class FileStoreRemoteTest
{
    private static final File TEST_FOLDER = new File("targets/unit-test/FileStoreRemoteTest");

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, FileStoreRemote.class);

    private Mockery context;

    private IFileSysOperationsFactory fileSysOpertationFactory;

    private ISimpleLogger loggerOrNull;

    @BeforeMethod
    public void setUp()
    {
        LogInitializer.init();
        this.context = new Mockery();
        this.fileSysOpertationFactory = context.mock(IFileSysOperationsFactory.class);
        this.loggerOrNull = context.mock(ISimpleLogger.class);

        FileUtilities.deleteRecursively(TEST_FOLDER);
        TEST_FOLDER.mkdirs();
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    private static void createFileModifiedAt(File file, long absoluteModificationTime)
            throws IOException
    {
        boolean ok = file.createNewFile();
        assert ok : "cannot create file " + file.getPath();
        file.deleteOnExit();
        ok = file.setLastModified(absoluteModificationTime);
        assert ok : "cannot change modification time of the file " + file.getPath();
    }

    @Test
    public void testLastModified() throws IOException
    {
        IFileStore remoteStore = createRemoteStore(TEST_FOLDER);
        String fileName = "example.txt";
        createFileModifiedAt(new File(TEST_FOLDER.getAbsoluteFile(), fileName), 0);

        String curDir = new File(".").getAbsolutePath();
        System.out.println(curDir);
        operationLog.info(curDir);
        StoreItem[] items = remoteStore.tryListSortByLastModified(loggerOrNull);
        assertEquals(1, items.length);
        assertEquals(fileName, items[0].getName());
    }

    private IFileStore createRemoteStore(File dir)
    {
        ISshCommandBuilder fakeSshBuilder = createFakeSshComandBuilder();
        return new FileStoreRemote(new HostAwareFileWithHighwaterMark("fake-host", dir, null),
                "remote-dir-desc", fakeSshBuilder, fileSysOpertationFactory, false, null, null);
    }

    static ISshCommandBuilder createFakeSshComandBuilder()
    {
        return new ISshCommandBuilder()
            {
                private static final long serialVersionUID = 1L;

                public List<String> createSshCommand(String command, String host)
                {
                    return Arrays.asList("bash", "-c", command);
                }
            };
    }
}
