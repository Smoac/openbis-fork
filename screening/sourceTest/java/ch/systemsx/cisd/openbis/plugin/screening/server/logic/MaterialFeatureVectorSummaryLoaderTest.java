/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import static ch.systemsx.cisd.common.test.AssertionUtil.assertArraysEqual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.IWellExtendedData;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialAllReplicasFeatureVectors;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialBiologicalReplicateFeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialIdFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.WellExtendedData;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.WellDataCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaSummaryAggregationType;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSummarySettings;

/**
 * Test of {@link MaterialFeatureVectorSummaryLoader}
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses = MaterialFeatureVectorSummaryLoader.class)
public class MaterialFeatureVectorSummaryLoaderTest extends AssertJUnit
{
    private static final String SIRNA_PROPERTY_TYPE_CODE = "sirna";

    @Test
    public void test()
    {
        MaterialSummarySettings settings = new MaterialSummarySettings();
        settings.setAggregationType(MaterialReplicaSummaryAggregationType.MEDIAN);
        settings.setBiologicalReplicatePropertyTypeCodes(SIRNA_PROPERTY_TYPE_CODE);
        int replId = 0;
        List<IWellExtendedData> wellDataList = Arrays.asList(
        // repl. 1 group 1
                createSIRNAWellData(replId, 1, 10, 100),

                createSIRNAWellData(replId, 1, 20, 200),

                createSIRNAWellData(replId, 1, 30, 1000),
                // repl. 1 group 2
                createSIRNAWellData(replId, 2, 100, 300),

                createSIRNAWellData(replId, 2, 200, 3000),

                createSIRNAWellData(replId, 2, 300, 2000),
                // repl. 2
                createSIRNAWellData(replId + 1, 1, 500, 0),
                // repl. 3
                createSIRNAWellData(replId + 2, 1, 0, 500));
        List<CodeAndLabel> featuresDesc =
                Arrays.asList(new CodeAndLabel("A", "A"), new CodeAndLabel("B", "B"));
        WellDataCollection wellDataCollection = new WellDataCollection(wellDataList, featuresDesc);

        MaterialAllReplicasFeatureVectors featureVectors =
                new MaterialFeatureVectorSummaryLoader(null, null, null, settings)
                        .tryLoadMaterialFeatureVectors(new TechId(replId), wellDataCollection);

        MaterialIdFeatureVectorSummary generalSummary = featureVectors.getGeneralSummary();
        assertArraysEqual(new float[]
            { 65, 650 }, generalSummary.getFeatureVectorSummary());
        assertArraysEqual(new int[]
            { 2, 3 }, generalSummary.getFeatureVectorRanks());
        assertEquals(replId, generalSummary.getMaterial().longValue());

        assertEquals(featuresDesc, featureVectors.getFeatureDescriptions());
        int groupId = 1;
        for (MaterialBiologicalReplicateFeatureVector subgroup : featureVectors
                .getBiologicalReplicates())
        {
            switch (groupId)
            {
                case 1:
                    assertArraysEqual(new float[]
                        { 20, 200 }, subgroup.getAggregatedSummary());
                    assertEquals("siRNA 1", subgroup.getSubgroupLabel());
                    assertEquals(3, subgroup.getTechnicalReplicatesValues().size());
                    break;
                case 2:
                    assertArraysEqual(new float[]
                        { 200, 2000 }, subgroup.getAggregatedSummary());
                    assertEquals("siRNA 2", subgroup.getSubgroupLabel());
                    assertEquals(3, subgroup.getTechnicalReplicatesValues().size());
                    break;
                default:
                    fail("unexpected subgroup " + groupId);
            }
            groupId++;
        }

    }

    private static IWellExtendedData createSIRNAWellData(long replicaId, long siRNAId,
            float... featureValues)
    {
        MaterialEntityProperty subgroupProperty = new MaterialEntityProperty();
        Material material = new Material();
        material.setId(siRNAId);
        subgroupProperty.setMaterial(material);
        PropertyType propertyType = new PropertyType();
        propertyType.setLabel("siRNA");
        propertyType.setCode(SIRNA_PROPERTY_TYPE_CODE);
        propertyType.setDataType(new DataType(DataTypeCode.MATERIAL));
        subgroupProperty.setPropertyType(propertyType);
        return createWellData(replicaId, subgroupProperty, featureValues);
    }

    private static IWellExtendedData createWellData(long replicaId,
            IEntityProperty subgroupProperty, float... featureValues)
    {
        Material material = new Material();
        material.setId(replicaId);

        Sample well = new Sample();
        ArrayList<IEntityProperty> properties = new ArrayList<IEntityProperty>();
        properties.add(subgroupProperty);
        material.setProperties(properties);
        well.setProperties(properties);

        return new WellExtendedData(replicaId, featureValues, well, material);
    }
}