/*
 * Copyright 2015 ETH Zuerich, CISD
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
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.IMaterialId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.MaterialPermId;
import ch.systemsx.cisd.common.test.AssertionUtil;

/**
 * @author Jakub Straszewski
 */
public class MapMaterialTest extends AbstractDataSetTest
{

    @Test
    public void testMapByPermId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialPermId virusId = new MaterialPermId("VIRUS2", "VIRUS");
        MaterialPermId bacteriaId = new MaterialPermId("BACTERIUM1", "BACTERIUM");

        Map<IMaterialId, Material> map = v3api.mapMaterials(sessionToken, Arrays.asList(virusId, bacteriaId),
                new MaterialFetchOptions());

        assertEquals(map.size(), 2);

        Material virus = map.get(virusId);
        Material bacteria = map.get(bacteriaId);

        assertEquals(virus.getCode(), "VIRUS2");
        assertEquals(true, virus.getModificationDate() != null);
        assertEquals(true, virus.getRegistrationDate() != null);
        assertPropertiesNotFetched(virus);

        assertEquals(bacteria.getCode(), "BACTERIUM1");
        assertEquals(true, bacteria.getModificationDate() != null);
        assertEquals(true, bacteria.getRegistrationDate() != null);
        assertPropertiesNotFetched(bacteria);

        assertEquals(bacteria.getPermId(), bacteriaId);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByPermIdWithProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialPermId virusId = new MaterialPermId("VIRUS2", "VIRUS");
        MaterialPermId bacteriaId = new MaterialPermId("BACTERIUM1", "BACTERIUM");

        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withRegistrator();
        fetchOptions.withTags();

        Map<IMaterialId, Material> map = v3api.mapMaterials(sessionToken, Arrays.asList(virusId, bacteriaId), fetchOptions);

        assertEquals(map.size(), 2);

        Material virus = map.get(virusId);
        Material bacteria = map.get(bacteriaId);

        assertEquals(virus.getPermId(), virusId);
        assertEquals(bacteria.getPermId(), bacteriaId);

        assertEquals(virus.getProperties().get("DESCRIPTION"), "test virus 2");
        assertEquals(bacteria.getProperties().get("DESCRIPTION"), "test bacterium 1");

        v3api.logout(sessionToken);
    }

    @Test
    public void testWithMaterialProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withMaterialProperties().withProperties();

        MaterialPermId selfParentId = new MaterialPermId("SRM_1", "SELF_REF");
        MaterialPermId selfChildId = new MaterialPermId("SRM_1A", "SELF_REF");

        Map<IMaterialId, Material> map = v3api.mapMaterials(sessionToken, Arrays.asList(selfChildId, selfParentId), fetchOptions);

        Material parent = map.get(selfParentId);
        Material child = map.get(selfChildId);

        Map<String, Material> materialProperties = parent.getMaterialProperties();

        Material childFromProperties = materialProperties.get("ANY_MATERIAL");

        assertEquals(selfChildId, childFromProperties.getPermId());
        assertEquals(true, child == childFromProperties);
    }

    @Test
    public void testWithType()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withType();

        MaterialPermId selfId = new MaterialPermId("SRM_1A", "SELF_REF");
        Map<IMaterialId, Material> map = v3api.mapMaterials(sessionToken, Arrays.asList(selfId), fetchOptions);
        Material item = map.get(selfId);
        assertEquals(item.getType().getCode(), "SELF_REF");
    }

    @Test
    public void testWithTags()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withTags();

        MaterialPermId matId = new MaterialPermId("AD3", "VIRUS");
        Map<IMaterialId, Material> map = v3api.mapMaterials(sessionToken, Arrays.asList(matId), fetchOptions);
        Material item = map.get(matId);
        Set<Tag> tags = item.getTags();
        AssertionUtil.assertSize(tags, 1);
        for (Tag tag : tags) // only one
        {
            assertEquals(tag.getCode(), "TEST_METAPROJECTS");
        }
    }
}
