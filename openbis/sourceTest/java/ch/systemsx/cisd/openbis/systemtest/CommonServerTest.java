/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * @author Franz-Josef Elmer
 */
public class CommonServerTest extends SystemTestCase
{
    @Test
    public void testGetSampleWithAssignedPropertyTypesAndProperties()
    {
        Sample sample = commonServer.getSampleInfo(systemSessionToken, new TechId(1)).getParent();

        assertEquals("/CISD/CL1", sample.getIdentifier());
        EntityType entityType = sample.getEntityType();
        assertEquals("CONTROL_LAYOUT", entityType.getCode());
        assertAssignedPropertyTypes("[$PLATE_GEOMETRY*, DESCRIPTION]", entityType);
        assertProperties("[$PLATE_GEOMETRY: 384_WELLS_16X24, DESCRIPTION: test control layout]",
                sample);
    }

    @Test
    public void testGetExperimentWithAssignedPropertyTypesAndProperties()
    {
        Experiment experiment = commonServer.getExperimentInfo(systemSessionToken, new TechId(2));

        assertEquals("/CISD/NEMO/EXP1", experiment.getIdentifier());
        EntityType entityType = experiment.getEntityType();
        assertEquals("SIRNA_HCS", entityType.getCode());
        assertAssignedPropertyTypes("[DESCRIPTION*, GENDER, PURCHASE_DATE]", entityType);
        assertProperties("[DESCRIPTION: A simple experiment, GENDER: MALE]", experiment);
    }

    @Test
    public void testGetExperimentWithAttachments()
    {
        Experiment experiment = commonServer.getExperimentInfo(systemSessionToken, new TechId(2));

        assertEquals("/CISD/NEMO/EXP1", experiment.getIdentifier());
        List<Attachment> attachments = experiment.getAttachments();
        assertEquals("exampleExperiments.txt", attachments.get(0).getFileName());
        assertEquals(4, attachments.size());
    }

    @Test
    public void testGetDataSetWithAssignedPropertyTypesAndProperties()
    {
        AbstractExternalData dataSet = commonServer.getDataSetInfo(systemSessionToken, new TechId(14));

        assertEquals("20110509092359990-11", dataSet.getCode());
        DataSetType dataSetType = dataSet.getDataSetType();
        assertEquals("HCS_IMAGE", dataSetType.getCode());
        assertAssignedPropertyTypes("[ANY_MATERIAL, BACTERIUM, COMMENT*, GENDER]", dataSetType);
        assertEquals("[COMMENT: non-virtual comment]", dataSet.getProperties().toString());
        assertEquals("/CISD/DEFAULT/EXP-REUSE", dataSet.getExperiment().getIdentifier());
    }

    @Test
    public void testGetContainerDataSetWithContainedDataSets()
    {
        AbstractExternalData dataSet = commonServer.getDataSetInfo(systemSessionToken, new TechId(13));

        assertEquals("20110509092359990-10", dataSet.getCode());
        assertEquals(true, dataSet.isContainer());
        ContainerDataSet containerDataSet = dataSet.tryGetAsContainerDataSet();
        List<AbstractExternalData> containedDataSets = containerDataSet.getContainedDataSets();
        assertEntities("[20110509092359990-11, 20110509092359990-12]", containedDataSets);
    }

    @Test
    public void testGetDataSetWithChildrenAndParents()
    {
        AbstractExternalData dataSet = commonServer.getDataSetInfo(systemSessionToken, new TechId(10));

        assertEquals("20081105092259900-0", dataSet.getCode());
        assertEntities("[20081105092359990-2]", dataSet.getChildren());
        assertEntities("[20081105092259000-9]", new ArrayList<AbstractExternalData>(dataSet.getParents()));
    }

    @Test
    public void testGetDataSetWithSample()
    {
        AbstractExternalData dataSet = commonServer.getDataSetInfo(systemSessionToken, new TechId(5));

        assertEquals("20081105092159111-1", dataSet.getCode());
        assertEquals("/CISD/CP-TEST-1", dataSet.getSampleIdentifier());
    }

    private void assertAssignedPropertyTypes(String expected, EntityType entityType)
    {
        List<? extends EntityTypePropertyType<?>> propTypes = entityType.getAssignedPropertyTypes();
        List<String> propertyCodes = new ArrayList<String>();
        for (EntityTypePropertyType<?> entityTypePropertyType : propTypes)
        {
            String code = entityTypePropertyType.getPropertyType().getCode();
            if (entityTypePropertyType.isMandatory())
            {
                code = code + "*";
            }
            propertyCodes.add(code);
        }
        Collections.sort(propertyCodes);
        assertEquals(expected, propertyCodes.toString());
    }
}
