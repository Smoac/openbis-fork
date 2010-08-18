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

package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import static ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils.asList;
import static ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils.findCode;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import it.unimi.dsi.fastutil.longs.LongArraySet;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.utilities.Counters;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.ExperimentProjectGroupCodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * Test cases for {@link ISampleListingQuery}.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { DatasetRecord.class, ExperimentProjectGroupCodeRecord.class, IDatasetListingQuery.class,
            DatasetListerDAO.class })
@Test(groups =
    { "db", "dataset" })
public class DatasetListingQueryTest extends AbstractDAOTest
{

    private long dbInstanceId;

    private DatabaseInstance dbInstance;

    private IDatasetListingQuery query;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        DatasetListerDAO dao = createDatasetListerDAO(daoFactory);
        dbInstanceId = dao.getDatabaseInstanceId();
        dbInstance = dao.getDatabaseInstance();

        query = dao.getQuery();
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws SQLException
    {
        if (query != null)
        {
            query.commit();
        }

    }

    public static DatasetListerDAO createDatasetListerDAO(IDAOFactory daoFactory)
    {
        IDatasetListingQuery query =
                EntityListingTestUtils.createQuery(daoFactory, IDatasetListingQuery.class);
        return DatasetListerDAO.create(daoFactory, query);
    }

    @Test
    public void testDataset()
    {
        long datasetId = 2;
        DatasetRecord dataset = query.getDataset(datasetId);
        assertDatasetCorrect(dataset);
    }

    private void assertDatasetCorrect(DatasetRecord dataset)
    {
        assertNotNull(dataset);
        assertNotNull(dataset.location);
        assertNotNull(dataset.code);
        assertNotNull(dataset.registration_timestamp);
    }

    @Test
    public void testDatasets()
    {
        List<DatasetRecord> datasets = asList(query.getDatasets(dbInstanceId));
        assertTrue(datasets.size() > 0);
        assertEqualWithFetchedById(datasets.get(0));
        assertEqualWithFetchedById(datasets.get(datasets.size() - 1));
    }

    private void assertEqualWithFetchedById(DatasetRecord datasetRecord)
    {
        DatasetRecord sameDataset = query.getDataset(datasetRecord.id);
        assertTrue(EqualsBuilder.reflectionEquals(sameDataset, datasetRecord));
    }

    @Test
    public void testDatasetsForExperiments()
    {
        LongArraySet experimentIds = new LongArraySet();
        ExperimentPE experiment1 =
                getExperiment(dbInstance.getCode(), "CISD", "NEMO", "EXP-TEST-1", daoFactory);
        ExperimentPE experiment2 =
            getExperiment(dbInstance.getCode(), "CISD", "NEMO", "EXP-TEST-2", daoFactory);
        experimentIds.add((long) experiment1.getId());
        experimentIds.add((long) experiment2.getId());
        List<DatasetRecord> datasets = asList(query.getDatasetsForExperiment(experimentIds));
        assertTrue(datasets.size() > 0);
        Counters<Long> counters = new Counters<Long>();
        for (DatasetRecord record : datasets)
        {
            assertDatasetCorrect(record);
            assertEqualWithFetchedById(record);
            counters.count(record.expe_id);
        }
        
        assertEquals(1, counters.getCountOf(experiment1.getId()));
        assertEquals(1, counters.getCountOf(experiment2.getId()));
        assertEquals(2, counters.getNumberOfDifferentObjectsCounted());
    }

    @Test
    public void testDatasetsForSample()
    {
        SamplePE sample = getSample("CISD", "CP-TEST-1", dbInstanceId, daoFactory);
        long sampleId = sample.getId();
        List<DatasetRecord> datasets = asList(query.getDatasetsForSample(sampleId));
        assertTrue(datasets.size() > 0);
        for (DatasetRecord record : datasets)
        {
            assertDatasetCorrect(record);
            assertEqualWithFetchedById(record);
            assertEquals(sampleId, record.samp_id.longValue());
        }
        // check coherence between the method which fetches only ids for the sample datasets
        Set<Long> datasetIds = EntityListingTestUtils.asSet(query.getDatasetIdsForSample(sampleId));
        assertEquals(datasets.size(), datasetIds.size());
        for (DatasetRecord record : datasets)
        {
            assertTrue(datasetIds.contains(record.id));
        }

    }

    static ExperimentPE getExperiment(String dbInstanceCode, String groupCode, String projectCode,
            String expCode, IDAOFactory daoFactory)
    {
        ProjectPE project =
                daoFactory.getProjectDAO().tryFindProject(dbInstanceCode, groupCode, projectCode);
        assertNotNull(project);
        ExperimentPE experiment =
                daoFactory.getExperimentDAO().tryFindByCodeAndProject(project, expCode);
        assertNotNull(experiment);
        return experiment;
    }

    static SamplePE getSample(String groupCode, String sampleCode, long dbInstanceId,
            IDAOFactory daoFactory)
    {
        DatabaseInstancePE dbInstancePE = new DatabaseInstancePE();
        dbInstancePE.setId(dbInstanceId);
        GroupPE group =
                daoFactory.getGroupDAO().tryFindGroupByCodeAndDatabaseInstance(groupCode,
                        dbInstancePE);
        assertNotNull(group);
        SamplePE sample = daoFactory.getSampleDAO().tryFindByCodeAndGroup(sampleCode, group);
        assertNotNull(sample);
        return sample;
    }

    @Test
    public void testParentDatasetsForChild()
    {
        long datasetId = 4;
        List<DatasetRecord> parents = asList(query.getParentDatasetsForChild(datasetId));
        assertEquals(1, parents.size());
    }

    @Test
    public void testChildDatasetsForParent()
    {
        long datasetId = 2;
        List<DatasetRecord> children = asList(query.getChildDatasetsForParent(datasetId));
        assertEquals(2, children.size());
    }

    @Test
    public void testDatasetTypes()
    {
        CodeRecord[] datasetTypes = query.getDatasetTypes(dbInstanceId);
        assertEqualsOrGreater(3, datasetTypes.length);
        findCode(Arrays.asList(datasetTypes), "UNKNOWN");
    }

    @Test
    public void testDataStores()
    {
        CodeRecord[] codes = query.getDataStores(dbInstanceId);
        assertEqualsOrGreater(1, codes.length);
        findCode(Arrays.asList(codes), "STANDARD");
    }

    @Test
    public void testLocatorTypes()
    {
        CodeRecord[] codes = query.getLocatorTypes();
        assertEqualsOrGreater(1, codes.length);
        findCode(Arrays.asList(codes), "RELATIVE_LOCATION");
    }

    @Test
    public void testFileFormatTypes()
    {
        CodeRecord[] codes = query.getFileFormatTypes(dbInstanceId);
        assertEqualsOrGreater(8, codes.length);
        findCode(Arrays.asList(codes), "XML");
    }

}