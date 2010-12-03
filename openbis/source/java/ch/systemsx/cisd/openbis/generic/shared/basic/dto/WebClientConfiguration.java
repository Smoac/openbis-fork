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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.ViewMode;

/**
 * Stores Web Client configuration.
 * 
 * @author Izabela Adamczyk
 */
public class WebClientConfiguration implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Map<String, DetailViewConfiguration> views =
            new HashMap<String, DetailViewConfiguration>();

    private Map<String, Map<String, String>> technologyProperties =
            new HashMap<String, Map<String, String>>();

    private ViewMode defaultViewMode;

    public String getPropertyOrNull(String technology, String key)
    {
        Map<String, String> properties = technologyProperties.get(technology);
        return properties == null ? null : properties.get(key);
    }

    public void addPropertiesForTechnology(String technology, Map<String, String> properties)
    {
        technologyProperties.put(technology, properties);
    }

    public Map<String, DetailViewConfiguration> getViews()
    {
        return views;
    }

    public void setViews(Map<String, DetailViewConfiguration> views)
    {
        this.views = views;
    }

    public ViewMode getDefaultViewMode()
    {
        return defaultViewMode;
    }

    public void setDefaultViewMode(ViewMode defaultViewMode)
    {
        this.defaultViewMode = defaultViewMode;
    }

    public WebClientConfiguration()
    {
    }

}
