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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.MaterialCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.CreationId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.IMaterialId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.MaterialPermId;
import ch.systemsx.cisd.common.test.AssertionUtil;

/**
 * @author pkupczyk
 */
public class CreateMaterialTest extends AbstractSampleTest
{

    @Test
    public void testSimpleMaterialCreation()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialCreation m1 = geneCreation("1982");
        MaterialCreation m2 = geneCreation("1984");

        List<MaterialPermId> materialIds = v3api.createMaterials(sessionToken, Arrays.asList(m1, m2));

        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();

        Map<IMaterialId, Material> map = v3api.mapMaterials(sessionToken, materialIds, fetchOptions);

        AssertionUtil.assertCollectionSize(map.values(), 2);

        Material material = map.get(new MaterialPermId("1982", "GENE"));
        assertEquals(material.getCode(), "1982");
        assertEquals(material.getPermId().getTypeCode(), "GENE");

        material = map.get(new MaterialPermId("1984", "GENE"));
        assertEquals(material.getCode(), "1984");
        assertEquals(material.getPermId().getTypeCode(), "GENE");
    }

    // @Test broken
    public void testCreateTwoMaterialsWithMaterialLinks()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialPermId m1id = new MaterialPermId("FIRST", "SELF_REF");
        MaterialCreation m1 = materialCreation(m1id);
        HashMap<String, String> properties1 = new HashMap<String, String>();
        properties1.put("ANY_MATERIAL", "SECOND (SELF_REF)");
        properties1.put("DESCRIPTION", "mandatory material decsription");
        m1.setProperties(properties1);

        MaterialPermId m2id = new MaterialPermId("SECOND", "SELF_REF");
        MaterialCreation m2 = materialCreation(m2id);
        HashMap<String, String> properties2 = new HashMap<String, String>();
        properties2.put("ANY_MATERIAL", "FIRST (SELF_REF)");
        properties2.put("DESCRIPTION", "mandatory material decsription");
        m2.setProperties(properties2);

        List<MaterialPermId> materialIds = v3api.createMaterials(sessionToken, Arrays.asList(m1, m2));

        AssertionUtil.assertCollectionContainsOnly(materialIds, m1id, m2id);

        // circular materialProperties
        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withMaterialPropertiesUsing(fetchOptions);

        Map<IMaterialId, Material> map = v3api.mapMaterials(sessionToken, Arrays.asList(m1id), fetchOptions);

        AssertionUtil.assertCollectionSize(map.values(), 1);

        Material resultm1 = map.get(m1id);

        assertEquals(resultm1.getPermId(), m1id);
        Material resultm2 = resultm1.getMaterialProperties().get("ANY_MATERIAL");
        assertEquals(resultm2.getPermId(), m2id);
        Material resultm3 = resultm2.getMaterialProperties().get("ANY_MATERIAL");
        assertEquals(resultm1, resultm3);
    }

    // all potential error scenarios

    // create mateiral with mateiral properties

    private MaterialCreation materialCreation(MaterialPermId permId)
    {
        String code = permId.getCode();
        String type = permId.getTypeCode();
        MaterialCreation materialCreation = new MaterialCreation();
        materialCreation.setCode(code);
        materialCreation.setTypeId(new EntityTypePermId(type));
        materialCreation.setCreationId(new CreationId("creation " + code));
        materialCreation.setDescription("Material with code " + code);
        return materialCreation;
    }

    private MaterialCreation geneCreation(String code)
    {
        MaterialCreation materialCreation = new MaterialCreation();
        materialCreation.setCode(code);
        materialCreation.setTypeId(new EntityTypePermId("GENE"));
        materialCreation.setCreationId(new CreationId("creation " + code));
        materialCreation.setDescription("Material with code " + code);

        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("GENE_SYMBOL", "SYMBOL " + code);

        materialCreation.setProperties(properties);

        return materialCreation;
    }
}
