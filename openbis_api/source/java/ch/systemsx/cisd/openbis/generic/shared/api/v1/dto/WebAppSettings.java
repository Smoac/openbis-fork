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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * A map containing persistent settings for an openBIS web app.
 *
 * @author Bernd Rinn
 */
@JsonObject("WebAppSettings")
public class WebAppSettings implements Serializable, Map<String, String>
{
    private static final long serialVersionUID = 1L;

    private String webAppId;
    
    private Map<String, String> settings;

    public WebAppSettings(String webAppId, Map<String, String> settings)
    {
        this.webAppId = webAppId;
        this.settings = settings;
    }
    
    public String getWebAppId()
    {
        return webAppId;
    }

    public Map<String, String> getSettings()
    {
        return settings;
    }

    @Override
    public int size()
    {
        return settings.size();
    }

    @Override
    public boolean isEmpty()
    {
        return settings.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return settings.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return settings.containsValue(value);
    }

    @Override
    public String get(Object key)
    {
        return settings.get(key);
    }

    @Override
    public String put(String key, String value)
    {
        return settings.put(key, value);
    }

    @Override
    public String remove(Object key)
    {
        return settings.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m)
    {
        settings.putAll(m);
    }

    @Override
    public void clear()
    {
        settings.clear();
    }

    @Override
    public Set<String> keySet()
    {
        return settings.keySet();
    }

    @Override
    public Collection<String> values()
    {
        return settings.values();
    }

    @Override
    public Set<java.util.Map.Entry<String, String>> entrySet()
    {
        return settings.entrySet();
    }

    @Override
    public String toString()
    {
        final ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(getWebAppId());
        builder.append(settings);
        return builder.toString();
    }

    //
    // JSON-RPC
    //
    @SuppressWarnings("unused")
    private WebAppSettings()
    {
    }

    @SuppressWarnings("unused")
    private void setWebAppId(String webAppId)
    {
        this.webAppId = webAppId;
    }

    @SuppressWarnings("unused")
    private void setSettings(Map<String, String> settings)
    {
        this.settings = settings;
    }

}
