/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.systemtest.api.v3;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.ExternalDataCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.LocatorTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.datastore.DataStorePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.vocabulary.VocabularyTermCode;

/**
 * @author pkupczyk
 */
public class CreateDataSetTest extends AbstractDataSetTest
{

    @Test
    public void testCreatePhysicalData()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExternalDataCreation externalCreation = new ExternalDataCreation();
        externalCreation.setLocation("a/b/c");
        externalCreation.setFileFormatTypeId(new FileFormatTypePermId("TIFF"));
        externalCreation.setLocatorTypeId(new LocatorTypePermId("RELATIVE_LOCATION"));
        externalCreation.setStorageFormatId(new VocabularyTermCode("PROPRIETARY"));

        DataSetCreation creation = new DataSetCreation();
        creation.setCode("TEST_DATASET");
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setExternalData(externalCreation);

        List<DataSetPermId> ids = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withType();
        fetchOptions.withExperiment();
        fetchOptions.withDataStore();
        fetchOptions.withExternalData().withFileFormatType();
        fetchOptions.withExternalData().withLocatorType();
        fetchOptions.withExternalData().withStorageFormat();

        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, ids, fetchOptions);

        DataSet dataSet = map.get(ids.get(0));
        assertEquals(dataSet.getCode(), "TEST_DATASET");
        assertEquals(dataSet.getType().getCode(), "UNKNOWN");
        assertEquals(dataSet.getExperiment().getPermId().getPermId(), "200811050951882-1028");
        // TODO add to translator
        // assertEquals(dataSet.getDataStore(), "STANDARD");
        assertEquals(dataSet.getExternalData().getLocation(), "a/b/c");
        assertEquals(dataSet.getExternalData().getFileFormatType().getCode(), "TIFF");
        assertEquals(dataSet.getExternalData().getLocatorType().getCode(), "RELATIVE_LOCATION");
        assertEquals(dataSet.getExternalData().getStorageFormat().getCode(), "PROPRIETARY");
    }

}
