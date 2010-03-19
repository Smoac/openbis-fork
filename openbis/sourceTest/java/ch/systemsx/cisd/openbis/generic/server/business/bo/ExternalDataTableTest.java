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
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivizationStatus.ACTIVATION_IN_PROGRESS;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivizationStatus.ACTIVE;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivizationStatus.ARCHIVED;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivizationStatus.ARCHIVIZATION_IN_PROGRESS;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivizationStatus.LOCKED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivizationStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * Test cases for corresponding {@link ExternalDataTable} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = ExternalDataTable.class)
public final class ExternalDataTableTest extends AbstractBOTest
{
    private IDataStoreServiceFactory dssFactory;

    private DataStorePE dss1;

    private DataStorePE dss2;

    private DataStorePE dss3;

    private IDataStoreService dataStoreService1;

    private IDataStoreService dataStoreService2;

    private IDataStoreService dataStoreService3;

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
        dss1 = createDataStore("dss1", false);
        dss2 = createDataStore("dss2", true);
        dss3 = createDataStore("dss3", true);
        dataStoreService1 = context.mock(IDataStoreService.class, "dataStoreService1");
        dataStoreService2 = context.mock(IDataStoreService.class, "dataStoreService2");
        dataStoreService3 = context.mock(IDataStoreService.class, "dataStoreService3");
        context.checking(new Expectations()
            {
                {
                    allowing(dssFactory).create(dss1.getRemoteUrl());
                    will(returnValue(dataStoreService1));

                    allowing(dssFactory).create(dss2.getRemoteUrl());
                    will(returnValue(dataStoreService2));

                    allowing(dssFactory).create(dss3.getRemoteUrl());
                    will(returnValue(dataStoreService3));
                }
            });
    }

    @Test
    public final void testLoadBySampleTechIdWithNullSampleId()
    {
        final ExternalDataTable externalDataTable = createExternalDataTable();
        boolean fail = true;
        try
        {
            externalDataTable.loadBySampleTechId(null);
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
    public final void testLoadBySampleTechId()
    {
        final ExternalDataTable externalDataTable = createExternalDataTable();
        final TechId sampleId = CommonTestUtils.TECH_ID;
        final String sampleCode = "CP-01";
        final SamplePE sample = new SamplePE();
        sample.setId(sampleId.getId());
        sample.setCode(sampleCode);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));

                    one(sampleDAO).getByTechId(sampleId);
                    will(returnValue(sample));

                    one(externalDataDAO).listExternalData(sample);
                }
            });
        externalDataTable.loadBySampleTechId(sampleId);
        context.assertIsSatisfied();
    }

    @Test
    public void testLoadByExperimentTechId()
    {
        final TechId experimentId = CommonTestUtils.TECH_ID;
        final ExperimentIdentifier identifier =
                new ExperimentIdentifier(new ProjectIdentifier("db", "group", "project"), "exp");
        final ExperimentPE experimentPE = CommonTestUtils.createExperiment(identifier);
        experimentPE.setId(experimentId.getId());
        final ExternalDataPE data1 = new ExternalDataPE();
        data1.setCode("d1");
        data1.setDataSetType(new DataSetTypePE());
        final ExternalDataPE data2 = new ExternalDataPE();
        data2.setCode("d2");
        // data2.setDeleted(true);
        data2.setDataSetType(new DataSetTypePE());
        // TODO 2009-06-02, Piotr Buczek: are these datasets used at all?

        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getExperimentDAO();
                    will(returnValue(experimentDAO));

                    one(experimentDAO).getByTechId(experimentId);
                    will(returnValue(experimentPE));

                    one(externalDataDAO).listExternalData(experimentPE);
                }
            });

        ExternalDataTable externalDataTable = createExternalDataTable();
        externalDataTable.loadByExperimentTechId(experimentId);

        context.assertIsSatisfied();
    }

    @Test
    public void testLoadByDataSetCodes()
    {
        final ExternalDataPE d1 = createDataSet("d1", dss1);
        final ExternalDataPE d2 = createDataSet("d2", dss2);
        prepareFindFullDataset(d1, true);
        prepareFindFullDataset(d2, false);

        ExternalDataTable externalDataTable = createExternalDataTable();
        externalDataTable.loadByDataSetCodes(Arrays.asList(d1.getCode(), d2.getCode()));

        assertEquals(1, externalDataTable.getExternalData().size());
        assertSame(d1, externalDataTable.getExternalData().get(0));

        context.assertIsSatisfied();
    }

    private void prepareFindFullDataset(final ExternalDataPE result, final boolean found)
    {
        context.checking(new Expectations()
            {
                {
                    one(externalDataDAO).tryToFindFullDataSetByCode(result.getCode(), true);
                    will(returnValue(found ? result : null));
                }
            });
    }

    @Test
    public void testDeleteLoadedDataSetsButOneDataSetIsUnknown()
    {
        final ExternalDataPE d1 = createDataSet("d1", dss1);
        final ExternalDataPE d2 = createDataSet("d2", dss2);
        context.checking(new Expectations()
            {
                {
                    prepareFindFullDataset(d1, true);
                    prepareFindFullDataset(d2, true);

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
    public void testDeleteLoadedDataSetsButNotAllAreAvailable()
    {
        final ExternalDataPE d1 = createDataSet("d1", dss1, ACTIVE);
        final ExternalDataPE d2 = createDataSet("d2", dss2, LOCKED);
        final ExternalDataPE d3 = createDataSet("d3n", dss2, ARCHIVED);
        final ExternalDataPE d4 = createDataSet("d4n", dss2, ARCHIVIZATION_IN_PROGRESS);
        final ExternalDataPE d5 = createDataSet("d5n", dss2, ACTIVATION_IN_PROGRESS);
        final ExternalDataPE[] allDataSets =
            { d1, d2, d3, d4, d5 };
        context.checking(new Expectations()
            {
                {
                    for (ExternalDataPE dataset : allDataSets)
                    {
                        prepareFindFullDataset(dataset, true);
                    }
                }
            });

        ExternalDataTable externalDataTable = createExternalDataTable();
        externalDataTable.loadByDataSetCodes(Code.extractCodes(Arrays.asList(allDataSets)));
        try
        {
            externalDataTable.deleteLoadedDataSets("");
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("Operation failed because following data sets are not available "
                    + "(they are archived or their status is pending): [d3n, d4n, d5n]. "
                    + "Unarchive these data sets or filter them out using data set status "
                    + "before performing the operation once again.", e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteLoadedDataSets()
    {
        final ExternalDataPE d1 = createDataSet("d1", dss1, ACTIVE);
        final ExternalDataPE d2 = createDataSet("d2", dss2, LOCKED);
        final String reason = "reason";
        context.checking(new Expectations()
            {
                {
                    prepareFindFullDataset(d1, true);
                    prepareFindFullDataset(d2, true);

                    List<String> d2Locations = Arrays.asList(d2.getLocation());
                    one(dataStoreService2).getKnownDataSets(dss2.getSessionToken(), d2Locations);
                    will(returnValue(d2Locations));

                    PersonPE person = EXAMPLE_SESSION.tryGetPerson();
                    one(eventDAO).persist(createDeletionEvent(d1, person, reason));
                    one(externalDataDAO).delete(d1);
                    one(eventDAO).persist(createDeletionEvent(d2, person, reason));
                    one(externalDataDAO).delete(d2);

                    one(dataStoreService2).deleteDataSets(dss2.getSessionToken(), d2Locations);
                }
            });

        ExternalDataTable externalDataTable = createExternalDataTable();
        externalDataTable.loadByDataSetCodes(Arrays.asList(d1.getCode(), d2.getCode()));
        externalDataTable.deleteLoadedDataSets(reason);

        context.assertIsSatisfied();
    }

    private EventPE createDeletionEvent(ExternalDataPE dataset, PersonPE person, String reason)
    {
        return ExternalDataTable.createDeletionEvent(dataset, person, reason);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUploadDataSets()
    {
        final ExternalDataPE d1PE = createDataSet("d1", dss1, ACTIVE);
        final ExternalDataPE d2PE = createDataSet("d2", dss2, LOCKED);
        final DataSetUploadContext uploadContext = new DataSetUploadContext();
        uploadContext.setCifexURL("cifexURL");
        uploadContext.setUserID(EXAMPLE_SESSION.getUserName());
        uploadContext.setPassword("pwd");
        uploadContext.setUserEMail(EXAMPLE_SESSION.getPrincipal().getEmail());
        uploadContext.setComment(ExternalDataTable.createUploadComment(Arrays.asList(d1PE, d2PE)));
        context.checking(new Expectations()
            {
                {
                    prepareFindFullDataset(d1PE, true);
                    prepareFindFullDataset(d2PE, true);

                    List<String> d2Locations = Arrays.asList(d2PE.getLocation());
                    one(dataStoreService2).getKnownDataSets(dss2.getSessionToken(), d2Locations);
                    will(returnValue(d2Locations));

                    one(dataStoreService2).uploadDataSetsToCIFEX(
                            with(equal(dss2.getSessionToken())), with(new BaseMatcher<List>()
                                {

                                    public boolean matches(Object item)
                                    {
                                        List<ExternalData> list = (List<ExternalData>) item;
                                        if (list.size() != 1)
                                        {
                                            return false;
                                        }
                                        ExternalData data = list.get(0);
                                        return d2PE.getCode().equals(data.getCode());
                                    }

                                    public void describeTo(Description description)
                                    {
                                        description.appendText("Data set d2");
                                    }
                                }), with(same(uploadContext)));
                }
            });

        ExternalDataTable externalDataTable = createExternalDataTable();
        externalDataTable.loadByDataSetCodes(Arrays.asList(d1PE.getCode(), d2PE.getCode()));
        String message = externalDataTable.uploadLoadedDataSetsToCIFEX(uploadContext);

        assertEquals(
                "The following data sets couldn't been uploaded because of unkown data store: d1",
                message);
        context.assertIsSatisfied();
    }

    @Test
    public void testUploadDataSetsButNotAllAreAvailable()
    {
        final ExternalDataPE d1 = createDataSet("d1", dss1, ACTIVE);
        final ExternalDataPE d2 = createDataSet("d2", dss2, LOCKED);
        final ExternalDataPE d3 = createDataSet("d3n", dss2, ARCHIVED);
        final ExternalDataPE d4 = createDataSet("d4n", dss2, ARCHIVIZATION_IN_PROGRESS);
        final ExternalDataPE d5 = createDataSet("d5n", dss2, ACTIVATION_IN_PROGRESS);
        final ExternalDataPE[] allDataSets =
            { d1, d2, d3, d4, d5 };
        final DataSetUploadContext uploadContext = new DataSetUploadContext();
        uploadContext.setCifexURL("cifexURL");
        uploadContext.setUserID(EXAMPLE_SESSION.getUserName());
        uploadContext.setPassword("pwd");
        uploadContext.setUserEMail(EXAMPLE_SESSION.getPrincipal().getEmail());
        uploadContext.setComment(ExternalDataTable.createUploadComment(Arrays.asList(d1, d2, d3,
                d4, d5)));
        context.checking(new Expectations()
            {
                {
                    for (ExternalDataPE dataset : allDataSets)
                    {
                        prepareFindFullDataset(dataset, true);
                    }
                }
            });

        ExternalDataTable externalDataTable = createExternalDataTable();
        externalDataTable.loadByDataSetCodes(Code.extractCodes(Arrays.asList(allDataSets)));
        try
        {
            externalDataTable.uploadLoadedDataSetsToCIFEX(uploadContext);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("Operation failed because following data sets are not available "
                    + "(they are archived or their status is pending): [d3n, d4n, d5n]. "
                    + "Unarchive these data sets or filter them out using data set status "
                    + "before performing the operation once again.", e.getMessage());
        }

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

    private void createAndCheckUploadComment(int expectedCodesShown, int codeLength,
            int dataSetCount)
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
                builder.append(String.format(ExternalDataTable.AND_MORE_TEMPLATE, dataSetCount
                        - expectedCodesShown));
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
        experiment.setExperimentType(new ExperimentTypePE());
        ProjectPE project = new ProjectPE();
        project.setCode("p1");
        GroupPE group = new GroupPE();
        group.setCode("g1");
        DatabaseInstancePE instance = new DatabaseInstancePE();
        instance.setCode("instance");
        instance.setOriginalSource(true);
        group.setDatabaseInstance(instance);
        project.setGroup(group);
        experiment.setProject(project);
        data.setExperiment(experiment);
        DataSetTypePE type = new DataSetTypePE();
        data.setDataSetType(type);
        return data;
    }

    private ExternalDataPE createDataSet(String code, DataStorePE dataStore,
            DataSetArchivizationStatus status)
    {
        ExternalDataPE data = createDataSet(code, dataStore);
        data.setStatus(status);
        return data;
    }

    private DataStorePE createDataStore(String code, boolean withRemoteURL)
    {
        DataStorePE dataStore = new DataStorePE();
        dataStore.setCode(code);
        if (withRemoteURL)
        {
            dataStore.setRemoteUrl("http://" + code);
        }
        dataStore.setSessionToken("session-" + code);
        return dataStore;
    }

    @Test
    public void testArchiveDataSets()
    {
        final ExternalDataPE d2Active1 = createDataSet("d2a1", dss2, ACTIVE);
        final ExternalDataPE d2Active2 = createDataSet("d2a2", dss2, ACTIVE);
        final ExternalDataPE d2NonActive1 = createDataSet("d2n1", dss2, ACTIVATION_IN_PROGRESS);
        final ExternalDataPE d2NonActive2 = createDataSet("d2n2", dss2, ARCHIVIZATION_IN_PROGRESS);
        final ExternalDataPE d2NonActive3 = createDataSet("d2n3", dss2, LOCKED);
        final ExternalDataPE d3Active = createDataSet("d3a", dss3, ACTIVE);
        final ExternalDataPE d3NonActive = createDataSet("d3n", dss3, ARCHIVED);
        final ExternalDataPE[] allDataSets =
                    { d2Active1, d2Active2, d2NonActive1, d2NonActive2, d3Active, d3NonActive,
                            d2NonActive3 };
        context.checking(new Expectations()
            {
                {
                    for (ExternalDataPE dataSet : allDataSets)
                    {
                        prepareFindFullDataset(dataSet, true);
                    }

                    prepareUpdateDatasetStatus(d2Active1, ARCHIVIZATION_IN_PROGRESS);
                    prepareUpdateDatasetStatus(d2Active2, ARCHIVIZATION_IN_PROGRESS);
                    prepareUpdateDatasetStatus(d3Active, ARCHIVIZATION_IN_PROGRESS);

                    prepareArchivization(dataStoreService2, dss2, d2Active1, d2Active2);
                    prepareArchivization(dataStoreService3, dss3, d3Active);
                }
            });

        ExternalDataTable externalDataTable = createExternalDataTable();
        externalDataTable.loadByDataSetCodes(Code.extractCodes(Arrays.asList(allDataSets)));
        externalDataTable.archiveDatasets();

        context.assertIsSatisfied();
    }

    @Test
    public void testUnarchiveDataSets()
    {
        final ExternalDataPE d2Archived1 = createDataSet("d2a1", dss2, ARCHIVED);
        final ExternalDataPE d2Archived2 = createDataSet("d2a2", dss2, ARCHIVED);
        final ExternalDataPE d2NonArchived1 = createDataSet("d2n1", dss2, ACTIVATION_IN_PROGRESS);
        final ExternalDataPE d2NonArchived2 =
                createDataSet("d2n2", dss2, ARCHIVIZATION_IN_PROGRESS);
        final ExternalDataPE d2NonActive3 = createDataSet("d2n3", dss2, LOCKED);
        final ExternalDataPE d3Archived = createDataSet("d3a", dss3, ARCHIVED);
        final ExternalDataPE d3NonArchived = createDataSet("d3n", dss3, ACTIVE);
        final ExternalDataPE[] allDataSets =
                    { d2Archived1, d2Archived2, d2NonArchived1, d2NonArchived2, d3Archived,
                            d3NonArchived, d2NonActive3 };
        context.checking(new Expectations()
            {
                {
                    for (ExternalDataPE dataSet : allDataSets)
                    {
                        prepareFindFullDataset(dataSet, true);
                    }

                    prepareUpdateDatasetStatus(d2Archived1, ACTIVATION_IN_PROGRESS);
                    prepareUpdateDatasetStatus(d2Archived2, ACTIVATION_IN_PROGRESS);
                    prepareUpdateDatasetStatus(d3Archived, ACTIVATION_IN_PROGRESS);

                    prepareUnarchivization(dataStoreService2, dss2, d2Archived1, d2Archived2);
                    prepareUnarchivization(dataStoreService3, dss3, d3Archived);
                }
            });

        ExternalDataTable externalDataTable = createExternalDataTable();
        externalDataTable.loadByDataSetCodes(Code.extractCodes(Arrays.asList(allDataSets)));
        externalDataTable.unarchiveDatasets();

        context.assertIsSatisfied();
    }

    private void prepareUpdateDatasetStatus(final ExternalDataPE dataSet,
            final DataSetArchivizationStatus newStatus)
    {
        context.checking(new Expectations()
            {
                {

                    one(externalDataDAO).validateAndSaveUpdatedEntity(
                            with(new BaseMatcher<ExternalDataPE>()
                                {

                                    public boolean matches(Object item)
                                    {
                                        if (item instanceof ExternalDataPE)
                                        {
                                            ExternalDataPE actualDataSet = (ExternalDataPE) item;
                                            return dataSet.equals(actualDataSet)
                                                    && newStatus == actualDataSet.getStatus();
                                        }
                                        return false;
                                    }

                                    public void describeTo(Description description)
                                    {
                                        description.appendValue("Dataset " + dataSet.getCode()
                                                + " with status " + newStatus);
                                    }

                                }));
                }
            });
    }

    @SuppressWarnings("unchecked")
    private void prepareArchivization(final IDataStoreService service, final DataStorePE store,
            final ExternalDataPE... dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).archiveDatasets(with(equal(store.getSessionToken())),
                            with(createDatasetDescriptionsMatcher(dataSets)),
                            with(equal(ManagerTestTool.EXAMPLE_PERSON.getEmail())));
                }
            });
    }

    @SuppressWarnings("unchecked")
    private void prepareUnarchivization(final IDataStoreService service, final DataStorePE store,
            final ExternalDataPE... dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).unarchiveDatasets(with(equal(store.getSessionToken())),
                            with(createDatasetDescriptionsMatcher(dataSets)),
                            with(equal(ManagerTestTool.EXAMPLE_PERSON.getEmail())));
                }
            });
    }

    @SuppressWarnings("unchecked")
    private BaseMatcher<List> createDatasetDescriptionsMatcher(final ExternalDataPE... dataSets)
    {
        return new BaseMatcher<List>()
            {

                public boolean matches(Object item)
                {
                    List<DatasetDescription> list = (List<DatasetDescription>) item;
                    if (list.size() != dataSets.length)
                    {
                        return false;
                    }
                    for (int i = 0; i < list.size(); i++)
                    {
                        if (false == list.get(i).getDatasetCode().equals(dataSets[i].getCode()))
                        {
                            return false;
                        }
                    }
                    return true;
                }

                public void describeTo(Description description)
                {
                    description.appendText("[");
                    for (ExternalDataPE dataSet : dataSets)
                    {
                        description.appendText("Dataset '" + dataSet.getCode() + "', ");
                    }
                    description.appendText("]");
                }
            };
    }
}
