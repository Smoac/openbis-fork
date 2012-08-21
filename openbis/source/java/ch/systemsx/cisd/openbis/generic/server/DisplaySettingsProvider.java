/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.WebAppSettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * A provider for user display settings, independent of the session.
 * 
 * @author Bernd Rinn
 */
public class DisplaySettingsProvider
{
    // user id to display settings map
    private final Map<String, DisplaySettings> displaySettingsMap =
            new HashMap<String, DisplaySettings>();

    public synchronized void addDisplaySettingsForPerson(PersonPE person)
    {
        DisplaySettings settings = displaySettingsMap.get(person.getUserId());
        if (settings == null)
        {
            settings = person.getDisplaySettings();
            displaySettingsMap.put(person.getUserId(), settings);
        }
    }

    public synchronized DisplaySettings getCurrentDisplaySettings(PersonPE person)
    {
        DisplaySettings settings = displaySettingsMap.get(person.getUserId());
        if (settings == null)
        {
            settings = person.getDisplaySettings();
            displaySettingsMap.put(person.getUserId(), settings);
        }
        settings = new DisplaySettings(settings);
        return settings;
    }

    @SuppressWarnings("deprecation")
    public synchronized DisplaySettings getRegularDisplaySettings(PersonPE person)
    {
        DisplaySettings settings = displaySettingsMap.get(person.getUserId());
        if (settings == null)
        {
            settings = person.getDisplaySettings();
            displaySettingsMap.put(person.getUserId(), settings);
        }
        settings = new DisplaySettings(settings);
        settings.clearCustomWebAppSettings();
        return settings;
    }

    @SuppressWarnings("deprecation")
    public synchronized DisplaySettings replaceRegularDisplaySettings(PersonPE person,
            DisplaySettings settings)
    {
        final DisplaySettings oldSettings = displaySettingsMap.get(person.getUserId());
        if (oldSettings != null)
        {
            settings.overwriteCustomWebAppSettings(oldSettings);
            settings.overwriteColumnSettings(oldSettings);
        }
        displaySettingsMap.put(person.getUserId(), settings);
        person.setDisplaySettings(settings);
        return settings;
    }

    @SuppressWarnings("deprecation")
    public synchronized WebAppSettings getWebAppSettings(PersonPE person, String webAppId)
    {
        DisplaySettings settings = displaySettingsMap.get(person.getUserId());
        if (settings == null)
        {
            settings = person.getDisplaySettings();
            displaySettingsMap.put(person.getUserId(), settings);
        }
        return new WebAppSettings(webAppId, settings.getCustomWebAppSettings(webAppId));
    }

    @SuppressWarnings("deprecation")
    public synchronized DisplaySettings replaceWebAppSettings(PersonPE person,
            WebAppSettings webAppSettings)
    {
        DisplaySettings settings = displaySettingsMap.get(person.getUserId());
        if (settings == null)
        {
            settings = person.getDisplaySettings();
            displaySettingsMap.put(person.getUserId(), settings);
        }
        settings.setCustomWebAppSettings(webAppSettings.getWebAppId(), webAppSettings.getSettings());
        person.setDisplaySettings(settings);
        return settings;
    }
}
