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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.PropertyHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
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
    public void testMapByPermIdCaseInsensitive()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialPermId virusId = new MaterialPermId("VIRus2", "viRUS");

        Map<IMaterialId, Material> map = v3api.mapMaterials(sessionToken, Arrays.asList(virusId),
                new MaterialFetchOptions());

        assertEquals(map.size(), 1);

        Iterator<Material> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), virusId);

        assertEquals(map.get(virusId).getPermId().getCode(), "VIRUS2");
        assertEquals(map.get(virusId).getPermId().getTypeCode(), "VIRUS");

        assertEquals(map.get(new MaterialPermId("VIRUS2", "VIRUS")).getPermId().getCode(), "VIRUS2");
        assertEquals(map.get(new MaterialPermId("VIRUS2", "VIRUS")).getPermId().getTypeCode(), "VIRUS");

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithFetchOptionsEmpty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialPermId virusId = new MaterialPermId("VIRUS2", "VIRUS");
        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();

        Map<IMaterialId, Material> map = v3api.mapMaterials(sessionToken, Arrays.asList(virusId), fetchOptions);

        assertEquals(map.size(), 1);
        Material virus = map.get(virusId);

        assertEquals(virus.getCode(), "VIRUS2");
        assertEquals(virus.getPermId(), virusId);
        assertEqualsDate(virus.getModificationDate(), "2009-03-18 10:50:19");
        assertEqualsDate(virus.getRegistrationDate(), "2008-11-05 09:18:25");

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithProperties()
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

        assertEquals(virus.getRegistrator().getUserId(), TEST_USER);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithMaterialProperties()
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
        assertEquals(childFromProperties.getProperties().get("DESCRIPTION"), "Material wich is attached material");
    }

    @Test
    public void testMapWithType()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withType();

        MaterialPermId selfId = new MaterialPermId("SRM_1A", "SELF_REF");
        Map<IMaterialId, Material> map = v3api.mapMaterials(sessionToken, Arrays.asList(selfId), fetchOptions);

        Material item = map.get(selfId);
        MaterialType type = item.getType();

        assertEquals(type.getCode(), "SELF_REF");
        assertEquals(type.getPermId(), new EntityTypePermId("SELF_REF"));
        assertEquals(type.getDescription(), "Self Referencing Material");
        assertEqualsDate(type.getModificationDate(), "2012-03-13 15:34:44");
    }

    @Test
    public void testMapWithRegistrator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withRegistrator();

        MaterialPermId selfId = new MaterialPermId("SRM_1A", "SELF_REF");
        Map<IMaterialId, Material> map = v3api.mapMaterials(sessionToken, Arrays.asList(selfId), fetchOptions);
        Material item = map.get(selfId);

        assertEquals(item.getRegistrator().getUserId(), TEST_USER);
    }

    @Test
    public void testMapWithHistoryEmpty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialPermId id = new MaterialPermId("VIRUS1", "VIRUS");

        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withHistory();

        Map<IMaterialId, Material> map = v3api.mapMaterials(sessionToken, Arrays.asList(id), fetchOptions);

        assertEquals(map.size(), 1);
        Material material = map.get(id);

        List<HistoryEntry> history = material.getHistory();
        assertEquals(history, Collections.emptyList());

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithHistoryProperty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        IMaterialId id = new MaterialPermId("VIRUS1", "VIRUS");

        MaterialUpdate update = new MaterialUpdate();
        update.setMaterialId(id);
        update.setProperty("DESCRIPTION", "new description");

        v3api.updateMaterials(sessionToken, Arrays.asList(update));

        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withHistory();

        Map<IMaterialId, Material> map = v3api.mapMaterials(sessionToken, Arrays.asList(id), fetchOptions);

        assertEquals(map.size(), 1);
        Material material = map.get(id);

        List<HistoryEntry> history = material.getHistory();
        assertEquals(history.size(), 1);

        PropertyHistoryEntry entry = (PropertyHistoryEntry) history.get(0);
        assertEquals(entry.getPropertyName(), "DESCRIPTION");
        assertEquals(entry.getPropertyValue(), "test virus 1");

        v3api.logout(sessionToken);
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
