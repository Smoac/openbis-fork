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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandExecutor;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Piotr Buczek
 */
@Friend(toClasses = RsyncDataSetCopier.class)
public class RsyncDataSetCopierTest extends AbstractFileSystemTestCase
{

    private static final String LOCATION_1 = "l1";

    @SuppressWarnings("unused")
    private static final String LOCATION_2 = "l2";

    private static final String DS1_CODE = "ds1";

    private static final String DS2_CODE = "ds2";

    private static final String DS1_LOCATION = LOCATION_1 + File.separator + DS1_CODE;

    private static final String DS2_LOCATION = LOCATION_1 + File.separator + DS2_CODE;

    private static final String DS1_DATA_FILE = "data.txt";

    private static final String DATA = "hello test";

    private static final String ORIGINAL = "original";

    private static final String SHARE_ID = "42";

    private File storeRoot;

    private DatasetDescription ds1;

    private File ds1Location;

    private File ds1Data;

    private DatasetDescription ds2;

    private File ds2Location;

    private File ds2Data;

    private Mockery context;

    private IPathCopierFactory copierFactory;

    @SuppressWarnings("unused")
    private IPathCopier copier;

    private ISshCommandExecutorFactory sshExecutorFactory;

    @SuppressWarnings("unused")
    private ISshCommandExecutor sshExecutor;

    private File destination;

    private File rsyncExec;

    private File sshExec;

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
    }

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        context = new Mockery();
        copierFactory = context.mock(IPathCopierFactory.class);
        copier = context.mock(IPathCopier.class);
        sshExecutorFactory = context.mock(ISshCommandExecutorFactory.class);
        sshExecutor = context.mock(ISshCommandExecutor.class);

        storeRoot = new File(workingDirectory, "store");
        storeRoot.mkdirs();
        File share = new File(storeRoot, SHARE_ID);

        ds1 = createDataSetDescription(DS1_CODE, LOCATION_1 + File.separator + DS1_CODE, true);
        ds1Location = new File(share, DS1_LOCATION);
        File ds1Folder = new File(ds1Location, ORIGINAL);
        ds1Folder.mkdirs();
        ds1Data = new File(ds1Folder, DS1_DATA_FILE);
        FileUtilities.writeToFile(ds1Data, DATA);

        ds2 = createDataSetDescription(DS2_CODE, DS2_LOCATION, true);
        ds2Location = new File(share, DS2_LOCATION);
        File ds2Folder = new File(ds2Location, ORIGINAL);
        ds2Folder.mkdirs();
        ds2Data = new File(ds2Folder, "images");
        ds2Data.mkdirs();

        destination = new File(workingDirectory, "destination");
        destination.mkdirs();
        rsyncExec = new File(workingDirectory, "my-rsync");
        rsyncExec.createNewFile();
        sshExec = new File(workingDirectory, "my-rssh");
        sshExec.createNewFile();

        storeRoot = new File(workingDirectory, "store");
        storeRoot.mkdirs();
    }

    private DatasetDescription createDataSetDescription(String dataSetCode, String location,
            boolean withSample)
    {
        DatasetDescription description = new DatasetDescription();
        description.setDatasetCode(dataSetCode);
        description.setDatasetTypeCode("MY_DATA");
        description.setDataSetLocation(location);
        description.setDatabaseInstanceCode("i");
        description.setSpaceCode("g");
        description.setProjectCode("p");
        description.setExperimentCode("e");
        description.setExperimentIdentifier("/g/p/e");
        description.setExperimentTypeCode("MY_EXPERIMENT");
        if (withSample)
        {
            description.setSampleCode("s");
            description.setSampleIdentifier("/g/s");
            description.setSampleTypeCode("MY_SAMPLE");
        }
        return description;
    }

    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testLocalCopyToDestination()
    {
        Properties properties = createLocalDestinationProperties();
        RsyncDataSetCopier dataSetCopier =
                new RsyncDataSetCopier(properties, copierFactory, sshExecutorFactory);
        prepareForCheckingLastModifiedDate();

        File copiedDataSet = ds1ArchivedLocationFile();
        File copiedData = ds1ArchivedDataFile();

        // check that data set is not yet in archive
        assertEquals(false, copiedDataSet.exists());

        Status status = dataSetCopier.copyToDestination(ds1Location, ds1);

        assertEquals(Status.OK, status);
        // check that data set is now in archive
        assertDs1InArchive(copiedDataSet, copiedData);
        // check that data set is still in store
        assertDs1InStore();

        context.assertIsSatisfied();
    }

    @Test
    public void testLocalCopyTwoDataSetsToDestination()
    {
        Properties properties = createLocalDestinationProperties();
        RsyncDataSetCopier dataSetCopier =
                new RsyncDataSetCopier(properties, copierFactory, sshExecutorFactory);
        prepareForCheckingLastModifiedDate();

        File copiedDataSet1 = ds1ArchivedLocationFile();
        File copiedDataSet2 = ds2ArchivedLocationFile();
        File copiedData1 = ds1ArchivedDataFile();
        File copiedData2 = ds2ArchivedDataFile();

        // check that both data sets are not yet in archive
        assertEquals(false, copiedDataSet1.exists());
        assertEquals(false, copiedDataSet2.exists());

        Status status1 = dataSetCopier.copyToDestination(ds1Location, ds1);

        assertEquals(Status.OK, status1);
        assertDs1InArchive(copiedDataSet1, copiedData1);
        // check that 2nd data set is not yet in archive
        assertEquals(false, copiedDataSet2.exists());

        Status status2 = dataSetCopier.copyToDestination(ds2Location, ds2);
        assertEquals(Status.OK, status2);
        // check that 1st data set is still in archive
        assertDs1InArchive(copiedDataSet1, copiedData1);
        // check 2nd data set
        assertDs2InArchive(copiedDataSet2, copiedData2);

        assertDs1InStore();
        assertEquals(true, ds2Data.exists());

        context.assertIsSatisfied();
    }

    @Test
    public void testLocalCopyAndRetrieveFromDestination()
    {
        // copy to archive

        Properties properties = createLocalDestinationProperties();
        RsyncDataSetCopier dataSetCopier =
                new RsyncDataSetCopier(properties, copierFactory, sshExecutorFactory);
        prepareForCheckingLastModifiedDate();

        File copiedDataSet = ds1ArchivedLocationFile();
        File copiedData = ds1ArchivedDataFile();

        // check that data set is not yet in archive
        assertEquals(false, copiedDataSet.exists());

        Status status = dataSetCopier.copyToDestination(ds1Location, ds1);

        assertEquals(Status.OK, status);
        assertDs1InArchive(copiedDataSet, copiedData);

        // delete from store
        try
        {
            FileUtils.deleteDirectory(ds1Location);
        } catch (IOException e)
        {
            fail(e.getMessage());
        }
        assertEquals(false, ds1Data.exists());

        // retrieve from archive

        Status statusRetrieve = dataSetCopier.retrieveFromDestination(ds1Location, ds1);
        assertEquals(Status.OK, statusRetrieve);
        assertDs1InStore();

        assertDs1InArchive(copiedDataSet, copiedData);

        context.assertIsSatisfied();
    }

    @Test
    public void testLocalCopyAndDeleteFromDestination()
    {
        // copy to archive

        Properties properties = createLocalDestinationProperties();
        RsyncDataSetCopier dataSetCopier =
                new RsyncDataSetCopier(properties, copierFactory, sshExecutorFactory);
        prepareForCheckingLastModifiedDate();

        File copiedDataSet = ds1ArchivedLocationFile();
        File copiedData = ds1ArchivedDataFile();

        // check that data set is not yet in archive
        assertEquals(false, copiedDataSet.exists());

        Status status = dataSetCopier.copyToDestination(ds1Location, ds1);

        assertEquals(Status.OK, status);
        assertDs1InArchive(copiedDataSet, copiedData);

        // delete from archive

        Status statusDelete = dataSetCopier.deleteFromDestination(ds1);
        assertEquals(Status.OK, statusDelete);
        assertEquals(false, copiedDataSet.exists());

        // we didn't delete it from store
        assertDs1InStore();

        context.assertIsSatisfied();
    }

    private File ds1ArchivedLocationFile()
    {
        return new File(destination, ds1.getDataSetLocation());
    }

    private File ds2ArchivedLocationFile()
    {
        return new File(destination, ds2.getDataSetLocation());
    }

    private File ds1ArchivedDataFile()
    {
        return new File(ds1ArchivedLocationFile(), ORIGINAL + File.separator + ds1Data.getName());
    }

    private File ds2ArchivedDataFile()
    {
        return new File(ds2ArchivedLocationFile(), ORIGINAL + File.separator + ds2Data.getName());
    }

    private void assertDs1InStore()
    {
        assertEquals(true, ds1Data.exists());
        assertEquals(DATA, FileUtilities.loadToString(ds1Data).trim());
    }

    private void assertDs1InArchive(File copiedDataSet, File copiedData)
    {
        assertEquals(true, copiedDataSet.isDirectory());
        assertEquals(ds1Data.lastModified(), copiedDataSet.lastModified());
        assertEquals(DATA, FileUtilities.loadToString(copiedData).trim());
    }

    private void assertDs2InArchive(File copiedDataSet2, File copiedData2)
    {
        assertEquals(true, copiedDataSet2.isDirectory());
        assertEquals(ds2Data.lastModified(), copiedData2.lastModified());
    }

    private Properties createLocalDestinationProperties()
    {
        final Properties properties = new Properties();
        properties.setProperty(RsyncDataSetCopier.DESTINATION_KEY, destination.getPath());
        return properties;
    }

    @SuppressWarnings("unused")
    private Properties createRemoteDestinationProperties()
    {
        final Properties properties = new Properties();
        properties.setProperty(RsyncDataSetCopier.DESTINATION_KEY,
                "localhost:" + destination.getPath());
        properties.setProperty(RsyncDataSetCopier.RSYNC_EXEC + "-executable", rsyncExec.getPath());
        properties.setProperty(RsyncDataSetCopier.SSH_EXEC + "-executable", sshExec.getPath());
        return properties;
    }

    private void prepareForCheckingLastModifiedDate()
    {
        // Sleep long enough to test last modified date of target will be same as of source.
        try
        {
            Thread.sleep(2000);
        } catch (InterruptedException ex)
        {
            // ignored
        }
    }
}
