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

import static ch.systemsx.cisd.openbis.generic.server.business.bo.ExternalDataTable.DELETION_DESCRIPTION;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.DataStoreServerSessionManager;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.HierarchyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
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
@Friend(toClasses=ExternalDataTable.class)
public final class ExternalDataTableTest extends AbstractBOTest
{
    private DataStoreServerSessionManager dssSessionManager;
    private IDataStoreService dataStoreService;

    private final ExternalDataTable createExternalDataTable()
    {
        return new ExternalDataTable(daoFactory, ManagerTestTool.EXAMPLE_SESSION);
    }

    @BeforeMethod
    @Override
    public void beforeMethod()
    {
        super.beforeMethod();
        dssSessionManager = new DataStoreServerSessionManager();
        dataStoreService = context.mock(IDataStoreService.class);
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
        final ExternalDataPE data1 = new ExternalDataPE();
        data1.setCode("d1");
        final ExternalDataPE data2 = new ExternalDataPE();
        data2.setCode("d2");
        data2.setDeleted(true);
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
                    procedurePE.setData(new HashSet<DataPE>(Arrays.asList(data1, data2)));
                    experimentPE.addProcedure(procedurePE);
                    will(returnValue(experimentPE));
                }
            });

        ExternalDataTable externalDataTable = createExternalDataTable();
        externalDataTable.loadByExperimentIdentifier(identifier);
        
        List<ExternalDataPE> list = externalDataTable.getExternalData();
        assertEquals(1, list.size());
        assertSame(data1, list.get(0));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testDeleteLoadedDataSetsButDataStoreServerIsDown()
    {
        final ExternalDataPE d1 = createDataSet("d1");
        context.checking(new Expectations()
            {
                {
                    one(externalDataDAO).tryToFindFullDataSetByCode(d1.getCode());
                    will(returnValue(d1));
                }
            });

        ExternalDataTable externalDataTable = createExternalDataTable();
        externalDataTable.loadByDataSetCodes(Arrays.asList(d1.getCode()));
        try
        {
            externalDataTable.deleteLoadedDataSets(dssSessionManager, "");
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals(
                    "The following data sets are unknown by any registered Data Store Server. "
                            + "May be the responsible Data Store Server is not running.\n[d1]", e
                            .getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testLoadByDataSetCodes()
    {
        final ExternalDataPE d1 = createDataSet("d1");
        final ExternalDataPE d2 = createDataSet("d2");
        context.checking(new Expectations()
            {
                {
                    one(externalDataDAO).tryToFindFullDataSetByCode(d1.getCode());
                    will(returnValue(d1));

                    one(externalDataDAO).tryToFindFullDataSetByCode(d2.getCode());
                    will(returnValue(null));
                }
            });

        ExternalDataTable externalDataTable = createExternalDataTable();
        externalDataTable.loadByDataSetCodes(Arrays.asList(d1.getCode(), d2.getCode()));
        
        assertEquals(1, externalDataTable.getExternalData().size());
        assertSame(d1, externalDataTable.getExternalData().get(0));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testDeleteLoadedDataSetsButOneDataSetIsUnknown()
    {
        dssSessionManager.registerDataStoreServer(new DataStoreServerSession("url",
                dataStoreService));
        final ExternalDataPE d1 = createDataSet("d1");
        final ExternalDataPE d2 = createDataSet("d2");
        context.checking(new Expectations()
        {
            {
                one(externalDataDAO).tryToFindFullDataSetByCode(d1.getCode());
                will(returnValue(d1));
                
                one(externalDataDAO).tryToFindFullDataSetByCode(d2.getCode());
                will(returnValue(d2));
                
                List<String> locations = Arrays.asList(d1.getLocation(), d2.getLocation());
                one(dataStoreService).getKnownDataSets(with(any(String.class)),
                        with(equal(locations)));
                will(returnValue(Arrays.asList(d1.getLocation())));
                
            }
        });
        
        ExternalDataTable externalDataTable = createExternalDataTable();
        externalDataTable.loadByDataSetCodes(Arrays.asList(d1.getCode(), d2.getCode()));
        try
        {
            externalDataTable.deleteLoadedDataSets(dssSessionManager, "");
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals(
                    "The following data sets are unknown by any registered Data Store Server. "
                            + "May be the responsible Data Store Server is not running.\n[d2]", e
                            .getMessage());
        }

        context.assertIsSatisfied();
    }
    
    @Test
    public void testDeleteLoadedDataSets()
    {
        dssSessionManager.registerDataStoreServer(new DataStoreServerSession("url",
                dataStoreService));
        final ExternalDataPE d1 = createDataSet("d1");
        final ExternalDataPE d2 = createDataSet("d2");
        context.checking(new Expectations()
            {
                {
                    one(externalDataDAO).tryToFindFullDataSetByCode(d1.getCode());
                    will(returnValue(d1));

                    one(externalDataDAO).tryToFindFullDataSetByCode(d2.getCode());
                    will(returnValue(d2));

                    List<String> locations = Arrays.asList(d1.getLocation(), d2.getLocation());
                    one(dataStoreService).getKnownDataSets(with(any(String.class)),
                            with(equal(locations)));
                    will(returnValue(locations));

                    PersonPE person = ManagerTestTool.EXAMPLE_SESSION.tryGetPerson();
                    one(externalDataDAO).markAsDeleted(d1, person, DELETION_DESCRIPTION, "reason");
                    one(externalDataDAO).markAsDeleted(d2, person, DELETION_DESCRIPTION, "reason");
                    one(dataStoreService).deleteDataSets(with(any(String.class)),
                            with(equal(locations)));
                }
            });

        ExternalDataTable externalDataTable = createExternalDataTable();
        externalDataTable.loadByDataSetCodes(Arrays.asList(d1.getCode(), d2.getCode()));
        externalDataTable.deleteLoadedDataSets(dssSessionManager, "reason");

        context.assertIsSatisfied();
    }
    
    private ExternalDataPE createDataSet(String code)
    {
        ExternalDataPE data = new ExternalDataPE();
        data.setCode(code);
        data.setLocation("here/" + code);
        return data;
    }
}
