/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.shared.util;

import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.generic.shared.WebClientConfigurationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebClientConfiguration;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class WebClientConfigUtils
{
    public static String getTranslatedDescription(SearchableEntity entity)
    {
        String description = entity.getDescription();
        if (getWebClientConfiguration() != null)
        {
            if (entity == SearchableEntity.SAMPLE)
            {
                description = getSampleText();
            } else if (entity == SearchableEntity.EXPERIMENT)
            {
                description = getExperimentText();
            }
        }
        return description;
    }

    public static String getTranslatedDescription(EntityKind entityKind)
    {
        String description = entityKind.getDescription();
        if (getWebClientConfiguration() != null)
        {
            if (entityKind == EntityKind.SAMPLE)
            {
                description = getSampleText();
            } else if (entityKind == EntityKind.EXPERIMENT)
            {
                description = getExperimentText();
            }
        }
        return description;
    }

    public static String getSampleText()
    {
        return getWebClientConfiguration().getSampleText();
    }
    
    public static String getExperimentText()
    {
        return getWebClientConfiguration().getExperimentText();
    }
    
    public static WebClientConfiguration getWebClientConfiguration()
    {
        WebClientConfigurationProvider provider =
                (WebClientConfigurationProvider) CommonServiceProvider.tryToGetBean(
                        ResourceNames.WEB_CLIENT_CONFIGURATION_PROVIDER);
        return provider == null ? null : provider.getWebClientConfiguration();
    }

}
