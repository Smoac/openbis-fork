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

import java.sql.SQLException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.ExperimentProjectGroupCodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * Test cases for {@link ISampleListingQuery}.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { DatasetRecord.class, ExperimentProjectGroupCodeRecord.class, IDatasetListingQuery.class,
            DatasetListerDAO.class, IDatasetListingFullQuery.class })
@Test(groups =
    { "db", "dataset" })
// TODO 2009-09-01, Tomasz Pylak: replace test stubs. Now they test only that the sql is
// gramaticaly correct, but the answer is not checked anyhow.
public class DatasetListingQueryTest extends AbstractDAOTest
{

    private long dbInstanceId;

    private ExperimentPE firstExperiment;

    private IDatasetListingQuery query;

    private long datasetId;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        DatasetListerDAO dao = createDatasetListerDAO(daoFactory);
        dbInstanceId = dao.getDatabaseInstanceId();
        firstExperiment = daoFactory.getExperimentDAO().listExperiments().get(0);
        // TODO 2009-09-01, Tomasz Pylak: get the real dataset id
        datasetId = 1;
        query = dao.getQuery();
    }

    public static DatasetListerDAO createDatasetListerDAO(IDAOFactory daoFactory)
    {
        IDatasetListingFullQuery query =
                EntityListingTestUtils.createQuery(daoFactory, IDatasetListingFullQuery.class);
        return DatasetListerDAO.create(daoFactory, query);
    }

    @Test
    public void testDataset()
    {
        // NOTE: test stub
        query.getDataset(datasetId);
    }

    @Test
    public void testDatasets()
    {
        // NOTE: test stub
        query.getDatasets();
    }

    @Test
    public void testDatasetsForExperiment()
    {
        // NOTE: test stub
        query.getDatasetsForExperiment(firstExperiment.getId());
    }

    @Test
    public void testDatasetTypes()
    {
        // NOTE: test stub
        query.getDatasetTypes(dbInstanceId);
    }

    @Test
    public void testDataStores()
    {
        // NOTE: test stub
        query.getDataStores(dbInstanceId);
    }

    @Test
    public void testLocatorTypes()
    {
        // NOTE: test stub
        query.getLocatorTypes();
    }

    @Test
    public void testEntityPropertyGenericValues()
    {
        // NOTE: test stub
        query.getEntityPropertyGenericValues();
    }

    @Test
    public void testEntityPropertyGenericValuesForDataset()
    {
        // NOTE: test stub
        query.getEntityPropertyGenericValues(datasetId);
    }

    @Test
    public void testEntityPropertyMaterialValues()
    {
        // NOTE: test stub
        query.getEntityPropertyMaterialValues();
    }

    @Test
    public void testEntityPropertyMaterialValuesForDataset()
    {
        // NOTE: test stub
        query.getEntityPropertyMaterialValues(datasetId);
    }

    @Test
    public void testEntityPropertyVocabularyTermValues()
    {
        // NOTE: test stub
        query.getEntityPropertyVocabularyTermValues();
    }

    @Test
    public void testEntityPropertyVocabularyTermValuesForDataset()
    {
        // NOTE: test stub
        query.getEntityPropertyVocabularyTermValues(datasetId);
    }

    @Test
    public void testFileFormatTypes()
    {
        // NOTE: test stub
        query.getFileFormatTypes(dbInstanceId);
    }
}