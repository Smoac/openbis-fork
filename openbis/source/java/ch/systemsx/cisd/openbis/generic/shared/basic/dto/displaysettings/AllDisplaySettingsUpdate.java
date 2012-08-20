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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto.displaysettings;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;

/**
 * @author pkupczyk
 */
public class AllDisplaySettingsUpdate implements IDisplaySettingsUpdate
{

    private static final long serialVersionUID = 1L;

    private DisplaySettings newDisplaySettings;

    // GWT
    @SuppressWarnings("unused")
    private AllDisplaySettingsUpdate()
    {
    }

    public AllDisplaySettingsUpdate(DisplaySettings newDisplaySettings)
    {
        if (newDisplaySettings == null)
        {
            throw new IllegalArgumentException("New display settings cannot be null");
        }
        this.newDisplaySettings = newDisplaySettings;
    }

    @Override
    public DisplaySettings update(DisplaySettings displaySettings)
    {
        return newDisplaySettings;
    }

    @Override
    public String toString()
    {
        return "update of all display settings";
    }

}
