/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;

/**
 * The unique {@link IFeatureCountLimitProvider} implementation.
 * 
 * @author Piotr Buczek
 */
@Component(ResourceNames.FEATURE_COUNT_LIMIT_PROVIDER)
public final class FeatureCountLimitProvider implements IFeatureCountLimitProvider
{

    private static int DEFAULT_FEATURE_COUNT_LIMIT = 20;

    private int limit = DEFAULT_FEATURE_COUNT_LIMIT;

    public FeatureCountLimitProvider()
    {
    }

    public int getLimit()
    {
        return limit;
    }

    public void setLimitAsString(String limitAsString)
    {
        // This method is called by spring with value taken from properties file.
        // Ignore the new value and keep default value when the property wasn't properly specified.
        if (StringUtils.isNumeric(limitAsString))
        {
            limit = Integer.parseInt(limitAsString);
        }
    }

}
