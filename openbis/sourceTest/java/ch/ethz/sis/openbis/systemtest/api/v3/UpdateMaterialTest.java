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
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.MaterialUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.IMaterialId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.MaterialPermId;

/**
 * @author pkupczyk
 */
public class UpdateMaterialTest extends AbstractSampleTest
{

    @Test
    public void testSimpleMaterialUpdate()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        MaterialUpdate update = new MaterialUpdate();
        update.setMaterialId(new MaterialPermId("1", "GENE"));
        String description = "This is amodified gene description";
        update.setProperty("DESCRIPTION", description);

        v3api.updateMaterials(sessionToken, Arrays.asList(update));

        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withProperties();

        Map<IMaterialId, Material> map = v3api.mapMaterials(sessionToken, Arrays.asList(update.getMaterialId()), fetchOptions);

        Material material = map.get(update.getMaterialId());
        assertEquals(material.getProperties().get("DESCRIPTION"), description);
    }

}
