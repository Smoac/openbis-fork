/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import java.util.Arrays;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ArchivingStatus;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.archive.DataSetArchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author pkupczyk
 */
public class ArchiveDataSetTest extends AbstractArchiveUnarchiveDataSetTest
{

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Object with DataSetPermId = \\[IDONTEXIST\\] has not been found.*")
    public void testArchiveWithNonexistentDataSet() throws Exception
    {
        DataSetPermId dataSetId = new DataSetPermId("IDONTEXIST");
        DataSetArchiveOptions options = new DataSetArchiveOptions();

        String sessionToken = v3.login(TEST_USER, PASSWORD);
        v3.archiveDataSets(sessionToken, Arrays.asList(dataSetId), options);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*test_space does not have enough privileges.*")
    public void testArchiveWithUnauthorizedDataSet() throws Exception
    {
        registerDataSet();

        DataSetPermId dataSetId = new DataSetPermId(dataSetCode);
        DataSetArchiveOptions options = new DataSetArchiveOptions();

        String sessionToken = v3.login(TEST_SPACE_USER, PASSWORD);
        v3.archiveDataSets(sessionToken, Arrays.asList(dataSetId), options);
    }

    @Test
    public void testArchiveWithRemoveFromStoreTrue() throws Exception
    {
        String sessionToken = v3.login(TEST_USER, PASSWORD);

        registerDataSet();

        DataSetPermId dataSetId = new DataSetPermId(dataSetCode);
        DataSetArchiveOptions options = new DataSetArchiveOptions();

        waitUntilDataSetStatus(dataSetCode, ArchivingStatus.AVAILABLE);
        v3.archiveDataSets(sessionToken, Arrays.asList(dataSetId), options);
        waitUntilDataSetStatus(dataSetCode, ArchivingStatus.ARCHIVE_PENDING);
        waitUntilDataSetStatus(dataSetCode, ArchivingStatus.ARCHIVED);
    }

    @Test
    public void testArchiveWithRemoveFromStoreFalse() throws Exception
    {
        String sessionToken = v3.login(TEST_USER, PASSWORD);

        registerDataSet();

        DataSetPermId dataSetId = new DataSetPermId(dataSetCode);
        DataSetArchiveOptions options = new DataSetArchiveOptions();
        options.setRemoveFromDataStore(false);

        waitUntilDataSetStatus(dataSetCode, ArchivingStatus.AVAILABLE);
        v3.archiveDataSets(sessionToken, Arrays.asList(dataSetId), options);
        waitUntilDataSetStatus(dataSetCode, ArchivingStatus.BACKUP_PENDING);
        waitUntilDataSetStatus(dataSetCode, ArchivingStatus.AVAILABLE);
    }

}
