/*
 * Copyright 2018 ETH Zuerich, SIS
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

import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.PluginType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.ScriptType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.PluginSearchCriteria;

/**
 * @author Franz-Josef Elmer
 */
public class SearchPluginTest extends AbstractTest
{
    @Test
    public void testSearchWithName()
    {
        // Given
        String sessionToken = v3api.login(TEST_OBSERVER_CISD, PASSWORD);
        PluginSearchCriteria searchCriteria = new PluginSearchCriteria();
        searchCriteria.withName().thatContains("ida");
        PluginFetchOptions fetchOptions = new PluginFetchOptions();
        fetchOptions.sortBy().name().desc();

        // When
        List<Plugin> plugins = v3api.searchPlugins(sessionToken, searchCriteria, fetchOptions).getObjects();

        // Then
        assertEquals(plugins.stream().map(p -> p.getName()).collect(Collectors.toList()).toString(),
                "[validateUpdateFAIL, validateOK, validateFAIL, validateChildren]");
        assertEquals(plugins.get(0).getScriptType(), ScriptType.ENTITY_VALIDATION);
        assertEquals(plugins.get(0).getPluginType(), PluginType.JYTHON);
        assertEquals(plugins.get(0).getFetchOptions().isWithScript(), false);
        assertEquals(plugins.get(0).getScript(), null);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithNameAndScriptType()
    {
        // Given
        String sessionToken = v3api.login(TEST_POWER_USER_CISD, PASSWORD);
        PluginSearchCriteria searchCriteria = new PluginSearchCriteria();
        searchCriteria.withName().thatContains("date");
        searchCriteria.withScriptType().thatEquals(ScriptType.DYNAMIC_PROPERTY);
        PluginFetchOptions fetchOptions = new PluginFetchOptions();
        fetchOptions.withScript().sortBy().name().asc();

        // When
        List<Plugin> plugins = v3api.searchPlugins(sessionToken, searchCriteria, fetchOptions).getObjects();

        // Then
        assertEquals(plugins.stream().map(p -> p.getName()).collect(Collectors.toList()).toString(),
                "[code_date, date]");
        assertEquals(plugins.get(0).getScriptType(), ScriptType.DYNAMIC_PROPERTY);
        assertEquals(plugins.get(0).getPluginType(), PluginType.JYTHON);
        assertEquals(plugins.get(0).getFetchOptions().isWithScript(), true);
        assertEquals(plugins.get(0).getScript(), "\"%s %s\" % (entity.code(), str(currentDate().getTime()))");

        v3api.logout(sessionToken);
    }
    
    @Test
    public void testSearchWithPluginType()
    {
        // Given
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        PluginSearchCriteria searchCriteria = new PluginSearchCriteria();
        searchCriteria.withPluginType().thatEquals(PluginType.PREDEPLOYED);
        PluginFetchOptions fetchOptions = new PluginFetchOptions();
        
        // When
        List<Plugin> plugins = v3api.searchPlugins(sessionToken, searchCriteria, fetchOptions).getObjects();
        
        // Then
        assertEquals(plugins.stream().map(p -> p.getName()).collect(Collectors.toList()).toString(), "[]");
        
        v3api.logout(sessionToken);
    }
}
