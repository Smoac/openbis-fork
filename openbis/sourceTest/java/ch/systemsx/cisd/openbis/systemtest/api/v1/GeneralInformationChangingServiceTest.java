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

package ch.systemsx.cisd.openbis.systemtest.api.v1;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class GeneralInformationChangingServiceTest extends SystemTestCase
{
    @SuppressWarnings("hiding")
    @Autowired
    private ICommonServer commonServer;
    
    @Autowired
    private IGeneralInformationService generalInformationService;

    @Autowired
    private IGeneralInformationChangingService generalInformationChangingService;
    
    private String sessionToken;

    @BeforeMethod
    public void beforeMethod()
    {
        sessionToken = generalInformationService.tryToAuthenticateForAllServices("test", "a");
    }

    @AfterMethod
    public void afterMethod()
    {
        generalInformationService.logout(sessionToken);
    }

    @Test
    public void testUpdateSampleProperties()
    {
        TechId id = new TechId(1043L);
        commonServer.assignPropertyType(sessionToken, new NewETPTAssignment(EntityKind.SAMPLE,
                "DESCRIPTION", "CELL_PLATE", false, null, null, 1L, false, false, null));
        commonServer.assignPropertyType(sessionToken, new NewETPTAssignment(EntityKind.SAMPLE,
                "GENDER", "CELL_PLATE", false, null, null, 1L, false, false, null));
        assertProperties("[ANY_MATERIAL: 2 (GENE), BACTERIUM: BACTERIUM-Y (BACTERIUM), "
                + "COMMENT: extremely simple stuff, ORGANISM: GORILLA, SIZE: 321]", commonServer
                .getSampleInfo(sessionToken, id).getParent());
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("SIZE", "42");
        properties.put("any_material", "1 (GENE)");
        properties.put("Organism", "DOG");
        properties.put("DESCRIPTION", "hello example");
        properties.put("gender", "FEMALE");
        
        generalInformationChangingService.updateSampleProperties(sessionToken, id.getId(), properties);
        
        assertProperties("[ANY_MATERIAL: 1 (GENE), BACTERIUM: BACTERIUM-Y (BACTERIUM), "
                + "COMMENT: extremely simple stuff, DESCRIPTION: hello example, GENDER: FEMALE, "
                + "ORGANISM: DOG, SIZE: 42]", commonServer.getSampleInfo(sessionToken, id)
                .getParent());
    }
}
