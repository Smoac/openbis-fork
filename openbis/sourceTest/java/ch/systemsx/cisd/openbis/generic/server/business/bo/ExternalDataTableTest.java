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

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;
import static ch.systemsx.cisd.openbis.generic.server.business.bo.ExternalDataTable.DELETION_DESCRIPTION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.HierarchyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
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
    private IDataStoreServiceFactory dssFactory;
    private DataStorePE dss1;
    private DataStorePE dss2;
    private IDataStoreService dataStoreService1;
    private IDataStoreService dataStoreService2;

    private final ExternalDataTable createExternalDataTable()
    {
        return new ExternalDataTable(daoFactory, dssFactory, ManagerTestTool.EXAMPLE_SESSION);
    }

    @BeforeMethod
    @Override
    public void beforeMethod()
    {
        super.beforeMethod();
        dssFactory = context.mock(IDataStoreServiceFactory.class);
        dss1 = createDataStore("dss1");
        dss2 = createDataStore("dss2");
        dataStoreService1 = context.mock(IDataStoreService.class, "dataStoreService1");
        dataStoreService2 = context.mock(IDataStoreService.class, "dataStoreService2");
        context.checking(new Expectations()
            {
                {
                    allowing(dssFactory).create(dss1.getRemoteUrl());
                    will(returnValue(dataStoreService1));
                    
                    allowing(dssFactory).create(dss2.getRemoteUrl());
                    will(returnValue(dataStoreService2));
                }
            });
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
                    experimentPE.setDataSets(Arrays.<DataPE>asList(data1, data2));
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
    public void testLoadByDataSetCodes()
    {
        final ExternalDataPE d1 = createDataSet("d1", dss1);
        final ExternalDataPE d2 = createDataSet("d2", dss2);
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
        final ExternalDataPE d1 = createDataSet("d1", dss1);
        final ExternalDataPE d2 = createDataSet("d2", dss2);
        context.checking(new Expectations()
            {
                {
                    one(externalDataDAO).tryToFindFullDataSetByCode(d1.getCode());
                    will(returnValue(d1));

                    one(externalDataDAO).tryToFindFullDataSetByCode(d2.getCode());
                    will(returnValue(d2));

                    one(dataStoreService1).getKnownDataSets(dss1.getSessionToken(),
                            Arrays.asList(d1.getLocation()));
                    will(returnValue(Arrays.asList(d1.getLocation())));

                    one(dataStoreService2).getKnownDataSets(dss2.getSessionToken(),
                            Arrays.asList(d2.getLocation()));
                    will(returnValue(Arrays.asList()));
                }
            });
        
        ExternalDataTable externalDataTable = createExternalDataTable();
        externalDataTable.loadByDataSetCodes(Arrays.asList(d1.getCode(), d2.getCode()));
        try
        {
            externalDataTable.deleteLoadedDataSets("");
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
        final ExternalDataPE d1 = createDataSet("d1", dss1);
        final ExternalDataPE d2 = createDataSet("d2", dss2);
        context.checking(new Expectations()
            {
                {
                    one(externalDataDAO).tryToFindFullDataSetByCode(d1.getCode());
                    will(returnValue(d1));

                    one(externalDataDAO).tryToFindFullDataSetByCode(d2.getCode());
                    will(returnValue(d2));
                    
                    List<String> d1Locations = Arrays.asList(d1.getLocation());
                    one(dataStoreService1).getKnownDataSets(dss1.getSessionToken(), d1Locations);
                    will(returnValue(d1Locations));

                    List<String> d2Locations = Arrays.asList(d2.getLocation());
                    one(dataStoreService2).getKnownDataSets(dss2.getSessionToken(), d2Locations);
                    will(returnValue(d2Locations));

                    PersonPE person = EXAMPLE_SESSION.tryGetPerson();
                    one(externalDataDAO).markAsDeleted(d1, person, DELETION_DESCRIPTION, "reason");
                    one(externalDataDAO).markAsDeleted(d2, person, DELETION_DESCRIPTION, "reason");
                    
                    one(dataStoreService1).deleteDataSets(dss1.getSessionToken(), d1Locations);
                    one(dataStoreService2).deleteDataSets(dss2.getSessionToken(), d2Locations);
                }
            });

        ExternalDataTable externalDataTable = createExternalDataTable();
        externalDataTable.loadByDataSetCodes(Arrays.asList(d1.getCode(), d2.getCode()));
        externalDataTable.deleteLoadedDataSets("reason");

        context.assertIsSatisfied();
    }
    
    @Test
    public void testUploadDataSets()
    {
        final ExternalDataPE d1 = createDataSet("d1", dss1);
        final ExternalDataPE d2 = createDataSet("d2", dss2);
        final DataSetUploadContext uploadContext = new DataSetUploadContext();
        uploadContext.setCifexURL("cifexURL");
        uploadContext.setUserID(EXAMPLE_SESSION.getUserName());
        uploadContext.setPassword("pwd");
        uploadContext.setUserEMail(EXAMPLE_SESSION.getPrincipal().getEmail());
        uploadContext.setComment(ExternalDataTable.createUploadComment(Arrays.asList(d1, d2)));
        context.checking(new Expectations()
            {
                {
                    one(externalDataDAO).tryToFindFullDataSetByCode(d1.getCode());
                    will(returnValue(d1));

                    one(externalDataDAO).tryToFindFullDataSetByCode(d2.getCode());
                    will(returnValue(d2));
                    
                    List<String> d1Locations = Arrays.asList(d1.getLocation());
                    one(dataStoreService1).getKnownDataSets(dss1.getSessionToken(), d1Locations);
                    will(returnValue(d1Locations));

                    List<String> d2Locations = Arrays.asList(d2.getLocation());
                    one(dataStoreService2).getKnownDataSets(dss2.getSessionToken(), d2Locations);
                    will(returnValue(d2Locations));

                    one(dataStoreService1).uploadDataSetsToCIFEX(dss1.getSessionToken(),
                            Arrays.asList(d1), uploadContext);
                    one(dataStoreService2).uploadDataSetsToCIFEX(dss2.getSessionToken(),
                            Arrays.asList(d2), uploadContext);
                }
            });

        ExternalDataTable externalDataTable = createExternalDataTable();
        externalDataTable.loadByDataSetCodes(Arrays.asList(d1.getCode(), d2.getCode()));
        externalDataTable.uploadLoadedDataSetsToCIFEX(uploadContext);

        context.assertIsSatisfied();
    }
    
    @Test
    public void testCreateUploadComment()
    {
        createAndCheckUploadComment(18, 50, 18);
        createAndCheckUploadComment(18, 50, 19);
        createAndCheckUploadComment(18, 50, 20);
        createAndCheckUploadComment(18, 50, 21);
        
        createAndCheckUploadComment(17, 51, 29);
        createAndCheckUploadComment(17, 52, 29);
        createAndCheckUploadComment(17, 53, 29);
        createAndCheckUploadComment(17, 54, 29);
        createAndCheckUploadComment(16, 54, 129);
    }
    
    private void createAndCheckUploadComment(int expectedCodesShown, int codeLength, int dataSetCount)
    {
        List<ExternalDataPE> dataSets = new ArrayList<ExternalDataPE>(dataSetCount);
        StringBuilder builder = new StringBuilder(ExternalDataTable.UPLOAD_COMMENT_TEXT);
        for (int i = 0; i < dataSetCount; i++)
        {
            ExternalDataPE dataSet = new ExternalDataPE();
            String code = generateDataSetCode(codeLength, i);
            dataSet.setCode(code);
            dataSets.add(dataSet);
            if (i < expectedCodesShown)
            {
                builder.append(ExternalDataTable.NEW_LINE);
                builder.append(code);
            } else if (i == expectedCodesShown)
            {
                builder.append(ExternalDataTable.NEW_LINE);
                builder.append(String.format(ExternalDataTable.AND_MORE_TEMPLATE, dataSetCount - expectedCodesShown));
            }
        }
        String comment = ExternalDataTable.createUploadComment(dataSets);
        System.out.println(comment.length() + ":" + comment);
        assertEquals(builder.toString(), comment);
        assertTrue(comment.length() <= BasicConstant.MAX_LENGTH_OF_CIFEX_COMMENT);
    }
    
    private String generateDataSetCode(int codeLength, int codeIndex)
    {
        String result = "-" + (codeIndex + 1);
        String sequence = StringUtils.repeat("1234567890", (codeLength / 10) + 1);
        return sequence.substring(0, codeLength - result.length()) + result;
    }
    
    private ExternalDataPE createDataSet(String code, DataStorePE dataStore)
    {
        ExternalDataPE data = new ExternalDataPE();
        data.setCode(code);
        data.setDataStore(dataStore);
        data.setLocation("here/" + code);
        ExperimentPE experiment = new ExperimentPE();
        experiment.setCode("exp1");
        ProjectPE project = new ProjectPE();
        project.setCode("p1");
        GroupPE group = new GroupPE();
        group.setCode("g1");
        project.setGroup(group);
        experiment.setProject(project);
        data.setExperiment(experiment);
        return data;
    }
    
    private DataStorePE createDataStore(String code)
    {
        DataStorePE dataStore = new DataStorePE();
        dataStore.setCode(code);
        dataStore.setRemoteUrl("http://" + code);
        dataStore.setSessionToken("session-" + code);
        return dataStore;
    }
}
