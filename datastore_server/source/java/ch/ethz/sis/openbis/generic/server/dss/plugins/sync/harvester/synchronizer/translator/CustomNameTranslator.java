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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.translator;

import java.util.HashMap;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
public class CustomNameTranslator implements INameTranslator
{
    private final HashMap<String, String> spaceMappings;

    public CustomNameTranslator(HashMap<String, String> spaceMappings)
    {
        this.spaceMappings = spaceMappings;
    }

    @Override
    public String translate(String name)
    {
        if (spaceMappings == null)
        {
            throw new ConfigurationFailureException("Space mappings cannot be null");
        }
        String newName = spaceMappings.get(name);
        if (newName == null)
        {
            throw new ConfigurationFailureException("No corresponding mapping found for '" + name + "'");
        }
        return newName;
    }

}
