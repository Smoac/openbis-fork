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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningDisplaySettingsManager;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageResolution;

/**
 * @author Kaloyan Enimanev
 */
public class DefaultChannelState implements IDefaultChannelState
{
    private IViewContext<?> viewContext;

    private String displayTypeId;

    public DefaultChannelState(IViewContext<?> viewContext, String displayTypeId)
    {
        this.viewContext = viewContext;
        this.displayTypeId = displayTypeId;
    }

    public void setDefaultChannels(List<String> channels)
    {
        getDisplaySettingManager().setDefaultChannels(displayTypeId, channels);
    }

    public List<String> tryGetDefaultChannels()
    {
        return getDisplaySettingManager().tryGetDefaultChannels(displayTypeId);
    }

    private ScreeningDisplaySettingsManager getDisplaySettingManager()
    {
        return ScreeningViewContext.getTechnologySpecificDisplaySettingsManager(viewContext);
    }

    public void setDefaultTransformation(String channel, String codes)
    {
        getTransformations().put(channel, codes);
    }

    public String tryGetDefaultTransformation(String channel)
    {
        return getTransformations().get(channel);
    }

    private Map<String, String> getTransformations()
    {
        return getDisplaySettingManager().getDefaultTransformationsForChannels(displayTypeId);
    }

    public ImageResolution tryGetDefaultResolution()
    {
        return getDisplaySettingManager().getDefaultResolution(displayTypeId);
    }

    public void setDefaultResolution(ImageResolution resolution)
    {
        getDisplaySettingManager().setDefaultResolution(displayTypeId, resolution);
    }

}
