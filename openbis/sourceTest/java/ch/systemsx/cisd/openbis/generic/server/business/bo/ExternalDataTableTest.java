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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.HierarchyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Test cases for corresponding {@link ExternalDataTable} class.
 * 
 * @author Christian Ribeaud
 */
public final class ExternalDataTableTest extends AbstractBOTest
{

    private final ExternalDataTable createExternalDataTable()
    {
        return new ExternalDataTable(daoFactory, ManagerTestTool.EXAMPLE_SESSION);
    }

    @Test
    public final void testLoadBySampleIdentifierWithNullSampleIdentifier()
    {
        final ExternalDataTable externalDataTable = createExternalDataTable();
        boolean fail = true;
        try
        {
            externalDataTable.loadBySampleIdentifier(null);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        fail = true;
        try
        {
            externalDataTable.getExternalData();
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test
    public final void testLoadBySampleIdentifierFailed()
    {
        final ExternalDataTable externalDataTable = createExternalDataTable();
        final String sampleCode = "CP-01";
        final String dbCode = "DB-1";
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier(dbCode), sampleCode);
        final DatabaseInstancePE databaseInstancePE = new DatabaseInstancePE();
        databaseInstancePE.setCode(dbCode);
        context.checking(new Expectations()
            {
                {
                    one(databaseInstanceDAO).tryFindDatabaseInstanceByCode(dbCode);
                    will(returnValue(databaseInstancePE));

                    one(sampleDAO).tryFindByCodeAndDatabaseInstance(sampleCode, databaseInstancePE,
                            HierarchyType.CHILD);
                }
            });
        try
        {
            externalDataTable.loadBySampleIdentifier(sampleIdentifier);
            fail("'" + UserFailureException.class.getName() + "' expected.");
        } catch (final UserFailureException ex)
        {
            // Nothing to do here.
        }
        context.assertIsSatisfied();
    }

    @Test
    public final void testLoadBySampleIdentifier()
    {
        final ExternalDataTable externalDataTable = createExternalDataTable();
        final String sampleCode = "CP-01";
        final String dbCode = "DB-1";
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier(dbCode), sampleCode);
        final DatabaseInstancePE databaseInstancePE = new DatabaseInstancePE();
        databaseInstancePE.setCode(dbCode);
        final SamplePE sample = new SamplePE();
        sample.setCode(sampleCode);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));

                    allowing(daoFactory).getExternalDataDAO();
                    will(returnValue(externalDataDAO));

                    allowing(daoFactory).getDatabaseInstanceDAO();
                    will(returnValue(databaseInstanceDAO));

                    one(databaseInstanceDAO).tryFindDatabaseInstanceByCode(dbCode);
                    will(returnValue(databaseInstancePE));

                    one(sampleDAO).tryFindByCodeAndDatabaseInstance(sampleCode, databaseInstancePE,
                            HierarchyType.CHILD);
                    will(returnValue(sample));

                    one(externalDataDAO).listExternalData(sample, SourceType.DERIVED);
                    one(externalDataDAO).listExternalData(sample, SourceType.MEASUREMENT);
                }
            });
        externalDataTable.loadBySampleIdentifier(sampleIdentifier);
        context.assertIsSatisfied();
    }
    
    @Test
    public void testLoadByExperimentIdentifier()
    {
        final ExperimentIdentifier identifier = new ExperimentIdentifier(
                new ProjectIdentifier("db", "group", "project"), "exp");
        final ExternalDataPE externalDataPE = new ExternalDataPE();
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getProjectDAO();
                    will(returnValue(projectDAO));

                    one(projectDAO).tryFindProject("DB", "GROUP", "PROJECT");
                    ProjectPE projectPE = new ProjectPE();
                    projectPE.setCode("PROJECT");
                    GroupPE groupPE = new GroupPE();
                    groupPE.setCode("GROUP");
                    projectPE.setGroup(groupPE);
                    will(returnValue(projectPE));

                    allowing(daoFactory).getExperimentDAO();
                    will(returnValue(experimentDAO));

                    one(experimentDAO).tryFindByCodeAndProject(projectPE, "EXP");
                    ExperimentPE experimentPE = new ExperimentPE();
                    ProcedurePE procedurePE = new ProcedurePE();
                    procedurePE.setData(new HashSet<DataPE>(Arrays.asList(externalDataPE)));
                    experimentPE.addProcedure(procedurePE);
                    will(returnValue(experimentPE));
                }
            });

        ExternalDataTable externalDataTable = createExternalDataTable();
        externalDataTable.loadByExperimentIdentifier(identifier);
        
        List<ExternalDataPE> list = externalDataTable.getExternalData();
        assertEquals(1, list.size());
        assertSame(externalDataPE, list.get(0));
        
        context.assertIsSatisfied();
    }
}
