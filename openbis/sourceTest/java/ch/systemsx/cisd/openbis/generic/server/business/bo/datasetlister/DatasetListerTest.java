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
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityListingQueryTest;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { DatasetRecord.class, DatasetRelationRecord.class })
@Test(groups =
    { "db", "dataset" })
// TODO 2009-09-01, Tomasz Pylak: replace test stubs.
public class DatasetListerTest extends AbstractDAOTest
{
    private ExperimentPE firstExperiment;

    private IDatasetLister lister;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        DatasetListerDAO datasetListerDAO =
                DatasetListingQueryTest.createDatasetListerDAO(daoFactory);
        SecondaryEntityDAO secondaryEntityDAO =
                SecondaryEntityListingQueryTest.createSecondaryEntityDAO(daoFactory);
        lister = DatasetLister.create(datasetListerDAO, secondaryEntityDAO, "www");
        firstExperiment = daoFactory.getExperimentDAO().listExperiments().get(0);
    }

    @Test
    public void testListByExperimentTechId()
    {
        // NOTE: test stub
        lister.listByExperimentTechId(new TechId(firstExperiment.getId()));
    }

    @Test
    public void testListBySampleTechId()
    {
        // NOTE: test stub
        lister.listBySampleTechId(new TechId(1L));
    }

}
