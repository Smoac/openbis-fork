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

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.IMaterialId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.MaterialPermId;

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

}
