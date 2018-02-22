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

import java.util.Arrays;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.update.PluginUpdate;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Franz-Josef Elmer
 */
public class UpdatePluginTest extends AbstractTest
{
    @Test
    public void testUpdate()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PluginPermId id = new PluginPermId("properties");
        PluginUpdate update = new PluginUpdate();
        update.setPluginId(id);
        update.setDescription("test");
        update.setAvailable(false);
        update.setScript("'test'");

        // When
        v3api.updatePlugins(sessionToken, Arrays.asList(update));

        // Then
        Plugin plugin = v3api.getPlugins(sessionToken, Arrays.asList(id), new PluginFetchOptions()).get(id);
        assertEquals(plugin.getName(), "properties");
        assertEquals(plugin.getScript(), update.getScript().getValue());
        assertEquals(plugin.isAvailable(), false);

        v3api.logout(sessionToken);
    }

    @Test
    public void testPluginIdMissing()
    {
        PluginUpdate update = new PluginUpdate();
        assertUserFailureException(update, "Plugin id cannot be null.");
    }

    private void assertUserFailureException(PluginUpdate update, String expectedMessage)
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    // When
                    v3api.updatePlugins(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                expectedMessage);
        v3api.logout(sessionToken);
    }
}
