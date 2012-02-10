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

package ch.systemsx.cisd.openbis.generic.server;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * @author pkupczyk
 */
@Test(groups =
    { "db", "dataset" })
public class ETLServiceDatabaseTest extends AbstractDAOTest
{
    @Autowired
    private IETLLIMSService service;

    private String sessionToken;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        sessionToken = service.tryToAuthenticate("test", "password").getSessionToken();
    }

    @Test
    public void testListExperimentsWithBasicFetchOptions()
    {
        List<ExperimentIdentifier> identifiers =
                Collections.singletonList(new ExperimentIdentifier("CISD", "CISD", "NEMO", "EXP1"));

        List<Experiment> result =
                service.listExperiments(sessionToken, identifiers, new ExperimentFetchOptions());

        assertEquals(1, result.size());
        assertTrue(result.get(0).getFetchOptions().containsOnlyOption(ExperimentFetchOption.BASIC));
    }

    @Test
    public void testListExperimentsWithAllFetchOptions()
    {
        List<ExperimentIdentifier> identifiers =
                Collections.singletonList(new ExperimentIdentifier("CISD", "CISD", "NEMO", "EXP1"));

        List<Experiment> result =
                service.listExperiments(sessionToken, identifiers, new ExperimentFetchOptions(
                        ExperimentFetchOption.values()));

        assertEquals(1, result.size());

        for (ExperimentFetchOption option : ExperimentFetchOption.values())
        {
            assertTrue(result.get(0).getFetchOptions().containsOption(option));
        }
    }

    @Test
    public void testListExperimentsForProjectsWithBasicFetchOptions()
    {
        List<ProjectIdentifier> identifiers =
                Collections.singletonList(new ProjectIdentifier("CISD", "CISD", "NOE"));

        List<Experiment> result =
                service.listExperimentsForProjects(sessionToken, identifiers,
                        new ExperimentFetchOptions());

        assertEquals(1, result.size());
        assertTrue(result.get(0).getFetchOptions().containsOnlyOption(ExperimentFetchOption.BASIC));
    }

    @Test
    public void testListExperimentsForProjectsWithAllFetchOptions()
    {
        List<ProjectIdentifier> identifiers =
                Collections.singletonList(new ProjectIdentifier("CISD", "CISD", "NOE"));

        List<Experiment> result =
                service.listExperimentsForProjects(sessionToken, identifiers,
                        new ExperimentFetchOptions(ExperimentFetchOption.values()));

        assertEquals(1, result.size());

        for (ExperimentFetchOption option : ExperimentFetchOption.values())
        {
            assertTrue(result.get(0).getFetchOptions().containsOption(option));
        }
    }

}
