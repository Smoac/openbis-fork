/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.DataSetInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * @author Franz-Josef Elmer
 */
public class CacheTest extends AssertJUnit
{
    @Test
    public void testGetDataSet()
    {
        DataSetInitializer initializer = new DataSetInitializer();
        initializer.setCode("ds1");
        initializer.setDataSetTypeCode("A");
        initializer.setExperimentIdentifier("E");
        EntityRegistrationDetailsInitializer initializer2 = new EntityRegistrationDetailsInitializer();
        initializer.setRegistrationDetails(new EntityRegistrationDetails(initializer2));
        DataSet dataSet = new DataSet(initializer);
        Cache cache = new Cache();

        cache.putDataSet(dataSet);

        assertSame(dataSet, cache.getDataSet(dataSet.getCode()));
    }

    @Test
    public void testGetExternalData()
    {
        AbstractExternalData dataSet = new ContainerDataSet();
        dataSet.setCode("ds1");
        Cache cache = new Cache();

        cache.putExternalData(dataSet);

        assertSame(dataSet, cache.getExternalData(dataSet.getCode()));
    }

    @Test
    public void testGetExperiment()
    {
        Experiment experiment = new Experiment();
        experiment.setIdentifier("e1");
        Cache cache = new Cache();

        cache.putExperiment(experiment);

        assertSame(experiment, cache.getExperiment(experiment.getIdentifier()));
    }

}
