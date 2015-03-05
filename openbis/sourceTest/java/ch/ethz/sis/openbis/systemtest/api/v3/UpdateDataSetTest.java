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

import java.util.Collections;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateActionAdd;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateActionRemove;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.ExternalDataUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.systemsx.cisd.common.test.AssertionUtil;

/**
 * @author pkupczyk
 */
public class UpdateDataSetTest extends AbstractSampleTest
{
    // test update dataSet

    @Test
    public void testUpdateWithDataSetExisting()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092259000-18");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.setProperty("COMMENT", "Updated description");

        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withProperties();
        DataSet result = v3api.mapDataSets(sessionToken, Collections.singletonList(dataSetId), fe).get(dataSetId);

        assertEquals(result.getProperties().get("COMMENT"), "Updated description");
    }

    @Test
    public void testUpdateExternalDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092259000-18");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        ExternalDataUpdate edupt = new ExternalDataUpdate();
        edupt.setFileFormatTypeId(new FileFormatTypePermId("PLKPROPRIETARY"));
        update.setExternalData(edupt);

        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withProperties();
        fe.withExternalData().withFileFormatType();
        DataSet result = v3api.mapDataSets(sessionToken, Collections.singletonList(dataSetId), fe).get(dataSetId);

        assertEquals(result.getExternalData().getFileFormatType().getCode(), "PLKPROPRIETARY");
    }

    @Test
    public void testUpdateExperiment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092259000-18");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.setExperimentId(new ExperimentPermId("200811050951882-1028"));

        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withProperties();
        fe.withExperiment();
        DataSet result = v3api.mapDataSets(sessionToken, Collections.singletonList(dataSetId), fe).get(dataSetId);

        assertEquals(result.getExperiment().getPermId().getPermId(), "200811050951882-1028");
    }

    // 18 jest childem 17tki

    @Test
    public void testRemovingParent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092259000-18");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        ListUpdateAction<IDataSetId> removeAction = new ListUpdateActionRemove<IDataSetId>();
        removeAction.setItems(Collections.singletonList(new DataSetPermId("20110805092359990-17")));

        update.setParentActions(Collections.singletonList(removeAction));

        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withProperties();
        fe.withExperiment();
        fe.withParents();
        DataSet result = v3api.mapDataSets(sessionToken, Collections.singletonList(dataSetId), fe).get(dataSetId);

        AssertionUtil.assertSize(result.getParents(), 2);
    }

    @Test
    public void testAddingParent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092259000-18");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        ListUpdateAction<IDataSetId> addAction = new ListUpdateActionAdd<IDataSetId>();
        addAction.setItems(Collections.singletonList(new DataSetPermId("20081105092259000-20")));

        update.setParentActions(Collections.singletonList(addAction));

        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withProperties();
        fe.withExperiment();
        fe.withParents();
        DataSet result = v3api.mapDataSets(sessionToken, Collections.singletonList(dataSetId), fe).get(dataSetId);

        AssertionUtil.assertSize(result.getParents(), 2);
    }
}
