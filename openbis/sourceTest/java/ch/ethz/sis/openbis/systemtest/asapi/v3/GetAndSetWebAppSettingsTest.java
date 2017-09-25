/*
 * Copyright 2017 ETH Zuerich, SIS
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

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.WebAppSettings;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class GetAndSetWebAppSettingsTest extends AbstractTest
{
    @Test
    public void testSetAndGetSettings()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        WebAppSettings webAppSettings = new WebAppSettings();
        webAppSettings.setWebAppId("my-web-app");
        Map<String, String> settings = new HashMap<>();
        settings.put("key1", "value1");
        settings.put("key2", "value2");
        webAppSettings.setSettings(settings);
        
        v3api.setWebAppSettings(sessionToken, webAppSettings);
        
        WebAppSettings webAppSettings2 = v3api.getWebAppSettings(sessionToken, "my-web-app");
        
        assertEquals(webAppSettings2.getWebAppId(), "my-web-app");
        assertEquals(webAppSettings2.getSettings().get("key1"), "value1");
        assertEquals(webAppSettings2.getSettings().get("key2"), "value2");
        assertEquals(webAppSettings2.getSettings().size(), 2);
        
        v3api.logout(sessionToken);
    }
    
    @Test
    public void testGetUnknownSettings()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        
        WebAppSettings webAppSettings = v3api.getWebAppSettings(sessionToken, "unknown-app");
        
        assertEquals(webAppSettings.getWebAppId(), "unknown-app");
        assertEquals(webAppSettings.getSettings().size(), 0);
        
        v3api.logout(sessionToken);
    }
}
