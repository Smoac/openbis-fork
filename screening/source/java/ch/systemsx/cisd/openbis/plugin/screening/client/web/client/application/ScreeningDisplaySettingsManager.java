/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningDisplaySettings;

/**
 * @author Piotr Buczek
 */
public class ScreeningDisplaySettingsManager
{
    private final ScreeningDisplaySettings screeningSettings;

    @SuppressWarnings("deprecation")
    public ScreeningDisplaySettingsManager(IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        DisplaySettingsManager displaySettingsManager = viewContext.getDisplaySettingsManager();
        ScreeningDisplaySettings settingsOrNull =
                (ScreeningDisplaySettings) displaySettingsManager
                        .tryGetTechnologySpecificSettings(viewContext.getTechnology());
        if (settingsOrNull == null)
        {
            settingsOrNull = new ScreeningDisplaySettings();
            displaySettingsManager.setTechnologySpecificSettings(viewContext.getTechnology(),
                    settingsOrNull);
        }
        screeningSettings = settingsOrNull;
    }

    // delegate

    @SuppressWarnings("deprecation")
    public String tryGetDefaultChannel(String displayTypeID)
    {
        return screeningSettings.getDefaultChannels().get(displayTypeID);
    }

    @SuppressWarnings("deprecation")
    public void setDefaultChannel(String displayTypeID, String channel)
    {
        screeningSettings.getDefaultChannels().put(displayTypeID, channel);
    }

}
