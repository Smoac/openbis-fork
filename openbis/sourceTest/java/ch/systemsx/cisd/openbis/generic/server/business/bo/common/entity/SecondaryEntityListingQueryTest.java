/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import java.sql.SQLException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { ExperimentProjectGroupCodeRecord.class, ISecondaryEntityListingQuery.class,
            SecondaryEntityDAO.class })
@Test(groups =
    { "db", "misc" })
public class SecondaryEntityListingQueryTest extends AbstractDAOTest
{

    private ExperimentPE firstExperiment;

    private PersonPE firstPerson;

    private SecondaryEntityDAO dao;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        firstExperiment = daoFactory.getExperimentDAO().listExperiments().get(0);
        firstPerson = daoFactory.getPersonDAO().getPerson(1);
        dao = createSecondaryEntityDAO(daoFactory);
    }

    public static SecondaryEntityDAO createSecondaryEntityDAO(IDAOFactory daoFactory)
    {
        ISecondaryEntityListingQuery query =
                EntityListingTestUtils.createQuery(daoFactory, ISecondaryEntityListingQuery.class);
        return SecondaryEntityDAO.create(daoFactory, query);
    }

    @Test
    public void testGetExperiment()
    {
        Experiment expFull = dao.tryGetExperiment(firstExperiment.getId());
        assertEquals(firstExperiment.getCode(), expFull.getCode());
        ProjectPE project = firstExperiment.getProject();
        assertEquals(project.getCode(), expFull.getProject().getCode());
        assertEquals(project.getGroup().getCode(), expFull.getProject().getSpace().getCode());
        assertEquals(firstExperiment.getEntityType().getCode(), expFull.getEntityType().getCode());
    }

    @Test
    public void testPerson()
    {
        Person person = dao.getPerson(firstPerson.getId());
        assertEquals(firstPerson.getFirstName(), person.getFirstName());
    }

    @Test
    public void testSamples()
    {
        Long2ObjectMap<Sample> samples = dao.getSamples(EntityListingTestUtils.createSet(1, 2));
        assertTrue(samples.size() > 0);
    }
}
