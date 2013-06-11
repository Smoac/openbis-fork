/*
 * Copyright 2013 ETH Zuerich, CISD
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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetStatusUpdater;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.DatasetDescriptionBuilder;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=AbstractArchiverProcessingPlugin.class)
public class DistributingArchiverTest extends RsyncArchiverTest
{
    private static final String DATA_SET_CODE = "ds1";
    
    private File defaultArchive;

    @BeforeMethod
    public void prepareTestData()
    {
        File ds1InStore = new File(share1, "a/b/c/ds1");
        defaultArchive = new File(workingDirectory, "default-archive");
        defaultArchive.mkdirs();
        properties.setProperty(DistributedPackagingDataSetFileOperationsManager.DEFAULT_DESTINATION_KEY, defaultArchive.getPath());
    }

    @Test
    public void testArchive()
    {
        DistributingArchiver archiver = createArchiver();
        DatasetDescription ds1 = new DatasetDescriptionBuilder(DATA_SET_CODE).getDatasetDescription();
        prepareUpdateStatus(DataSetArchivingStatus.AVAILABLE, false);
        prepareGetShareId();
        
        ProcessingStatus processingStatus = archiver.archive(Arrays.asList(ds1), archiverTaskContext, false);
        
        List<Status> errorStatuses = processingStatus.getErrorStatuses();
//        assertEquals("", logRecorder.getLogContent());
//        assertEquals("", errorStatuses.toString());
    }
    
    private void prepareUpdateStatus(final DataSetArchivingStatus status, final boolean presentInArchive)
    {
        context.checking(new Expectations()
            {
                {
                    one(statusUpdater).update(Arrays.asList(DATA_SET_CODE), status, presentInArchive);
                }
            });
    }
    
    private void prepareGetShareId()
    {
        context.checking(new Expectations()
        {
            {
                allowing(shareIdManager).getShareId(DATA_SET_CODE);
                will(returnValue("1"));
            }
        });
    }
    

    private DistributingArchiver createArchiver()
    {
        DistributingArchiver archiver = new DistributingArchiver(properties, store);
        archiver.statusUpdater = statusUpdater;
        return archiver;
    }

}
