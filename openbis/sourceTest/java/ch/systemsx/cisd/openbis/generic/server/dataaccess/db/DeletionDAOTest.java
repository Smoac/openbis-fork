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

import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;

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
        assertEquals(4, allDeletions.size());

        assertTrashedEntities(0, 5, 1, allDeletions.get(0));
        assertTrashedEntities(0, 3, 0, allDeletions.get(1));
        assertTrashedEntities(0, 323, 0, allDeletions.get(2));
        assertTrashedEntities(2, 3, 0, allDeletions.get(3));

        assertTrashedEntities(2, 334, 1, allDeletions);
    }

    private void assertTrashedEntities(int expectedExperiments, int expectedSamples,
            int expectedDataSets, DeletionPE deletion)
    {
        assertTrashedEntities(expectedExperiments, expectedSamples, expectedDataSets,
                Collections.singletonList(deletion));
    }

    private void assertTrashedEntities(int expectedExperiments, int expectedSamples,
            int expectedDataSets, List<DeletionPE> deletions)
    {
        List<TechId> deletionIds = TechId.createList(deletions);
        IDeletionDAO deletionDAO = daoFactory.getDeletionDAO();
        assertEquals(deletionIds.toString(), expectedExperiments, deletionDAO
                .findTrashedExperimentIds(deletionIds).size());
        assertEquals(deletionIds.toString(), expectedSamples,
                deletionDAO.findTrashedSampleIds(deletionIds).size());
        assertEquals(deletionIds.toString(), expectedDataSets,
                deletionDAO.findTrashedDataSetCodes(deletionIds).size());
    }
}
