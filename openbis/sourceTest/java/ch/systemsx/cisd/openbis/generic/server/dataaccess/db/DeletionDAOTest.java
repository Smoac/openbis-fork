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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

/**
 * @author Piotr Buczek
 */
@Test(groups = "db")
public class DeletionDAOTest extends AbstractDAOTest
{

    @Test
    public void testFindTrashedEntities() throws Exception
    {
        // simple test with db specific numbers
        IDeletionDAO deletionDAO = daoFactory.getDeletionDAO();
        List<DeletionPE> allDeletions = deletionDAO.listAllEntities();
        assertEquals(countRowsInTable(TableNames.DELETIONS_TABLE), allDeletions.size());
        assertEquals(4, allDeletions.size());

        assertTrashedEntitiesFound(0, 5, 1, allDeletions.get(0));
        assertTrashedEntitiesFound(0, 3, 0, allDeletions.get(1));
        assertTrashedEntitiesFound(0, 323, 0, allDeletions.get(2));
        assertTrashedEntitiesFound(2, 3, 0, allDeletions.get(3));

        assertTrashedEntitiesFound(2, 334, 1, allDeletions);
    }

    @Test
    public void testCreateDeletion() throws InterruptedException
    {
        IDeletionDAO deletionDAO = daoFactory.getDeletionDAO();

        int rowsInDeletions = countRowsInTable(TableNames.DELETIONS_TABLE);

        DeletionPE deletion = new DeletionPE();
        deletion.setRegistrator(getTestPerson());
        deletion.setReason("example reason");
        Date dateBeforeRegistration = DateUtils.addDays(new Date(), -1);
        deletionDAO.create(deletion);
        Date dateAfterRegistration = DateUtils.addDays(new Date(), 1);

        List<DeletionPE> allDeletions = deletionDAO.listAllEntities();
        assertEquals(rowsInDeletions + 1, countRowsInTable(TableNames.DELETIONS_TABLE));
        assertEquals(rowsInDeletions + 1, allDeletions.size());
        DeletionPE lastDeletion = allDeletions.get(allDeletions.size() - 1);
        assertEquals(deletion.getReason(), lastDeletion.getReason());
        assertEquals(deletion.getRegistrator(), lastDeletion.getRegistrator());
        assertTrue(dateBeforeRegistration + " < " + lastDeletion.getRegistrationDate(),
                dateBeforeRegistration.getTime() < lastDeletion.getRegistrationDate().getTime());
        assertTrue(lastDeletion.getRegistrationDate() + " > " + dateAfterRegistration, lastDeletion
                .getRegistrationDate().getTime() < dateAfterRegistration.getTime());
    }

    // @Test(dependsOnMethods = "testFindTrashedEntities")
    @Test
    public void testRevertFirstDeletion()
    {
        IDeletionDAO deletionDAO = daoFactory.getDeletionDAO();
        List<DeletionPE> allDeletions = deletionDAO.listAllEntities();

        assertTrashedEntitiesFound(0, 5, 1, allDeletions.get(0));
        DeletionPE deletion = allDeletions.get(0);

        testRevertDeletion(deletionDAO, deletion);
    }

    private void testRevertDeletion(IDeletionDAO deletionDAO, DeletionPE deletion)
    {
        List<TechId> deletionId = Collections.singletonList(TechId.create(deletion));
        List<TechId> experimentIds = deletionDAO.findTrashedExperimentIds(deletionId);
        List<TechId> sampleIds = deletionDAO.findTrashedSampleIds(deletionId);
        List<String> dataSetCodes = deletionDAO.findTrashedDataSetCodes(deletionId);

        assertExperimentsDeleted(true, experimentIds);
        assertSamplesDeleted(true, sampleIds);
        assertDataSetsDeleted(true, dataSetCodes);

        int rowsInDeletions = countRowsInTable(TableNames.DELETIONS_TABLE);
        List<DeletionPE> allDeletions = deletionDAO.listAllEntities();

        deletionDAO.revert(deletion);

        assertEquals(rowsInDeletions - 1, countRowsInTable(TableNames.DELETIONS_TABLE));
        assertEquals(allDeletions.size() - 1, deletionDAO.listAllEntities().size());

        assertExperimentsDeleted(false, experimentIds);
        assertSamplesDeleted(false, sampleIds);
        assertDataSetsDeleted(false, dataSetCodes);
    }

    private void assertSamplesDeleted(boolean expectedDeleted, List<TechId> sampleIds)
    {
        ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        for (TechId id : sampleIds)
        {
            String errorMsg =
                    String.format("sample with id %s is expected %s be deleted;", id,
                            expectedDeleted ? "to" : "not to");
            assertEquals(errorMsg, expectedDeleted, sampleDAO.getByTechId(id).getDeletion() != null);
        }
    }

    private void assertExperimentsDeleted(boolean expectedDeleted, List<TechId> experimentIds)
    {
        IExperimentDAO experimentDAO = daoFactory.getExperimentDAO();
        for (TechId id : experimentIds)
        {
            String errorMsg =
                    String.format("experiment with id %s is expected %s be deleted;", id,
                            expectedDeleted ? "to" : "not to");
            assertEquals(errorMsg, expectedDeleted,
                    experimentDAO.getByTechId(id).getDeletion() != null);
        }
    }

    private void assertDataSetsDeleted(boolean expectedDeleted, List<String> dataSetCodes)
    {
        IDataDAO dataDAO = daoFactory.getDataDAO();
        for (String code : dataSetCodes)
        {
            String errorMsg =
                    String.format("data set '%s' is expected %s be deleted;", code,
                            expectedDeleted ? "to" : "not to");
            assertEquals(errorMsg, expectedDeleted, dataDAO.tryToFindDataSetByCode(code)
                    .getDeletion() != null);
        }
    }

    private void assertTrashedEntitiesFound(int expectedExperiments, int expectedSamples,
            int expectedDataSets, DeletionPE deletion)
    {
        assertTrashedEntitiesFound(expectedExperiments, expectedSamples, expectedDataSets,
                Collections.singletonList(deletion));
    }

    private void assertTrashedEntitiesFound(int expectedExperiments, int expectedSamples,
            int expectedDataSets, List<DeletionPE> deletions)
    {
        List<TechId> deletionIds = TechId.createList(deletions);
        IDeletionDAO deletionDAO = daoFactory.getDeletionDAO();

        List<TechId> foundExperimentIds = deletionDAO.findTrashedExperimentIds(deletionIds);
        assertEquals(deletionIds.toString(), expectedExperiments, foundExperimentIds.size());
        assertExperimentsDeleted(true, foundExperimentIds);
        List<TechId> foundSampleIds = deletionDAO.findTrashedSampleIds(deletionIds);
        assertEquals(deletionIds.toString(), expectedSamples, foundSampleIds.size());
        assertSamplesDeleted(true, foundSampleIds);
        List<String> foundDataSetCodes = deletionDAO.findTrashedDataSetCodes(deletionIds);
        assertEquals(deletionIds.toString(), expectedDataSets, foundDataSetCodes.size());
        assertDataSetsDeleted(true, foundDataSetCodes);
    }
}
