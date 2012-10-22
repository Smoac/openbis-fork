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

package ch.systemsx.cisd.openbis.generic.shared.coreplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ch.systemsx.cisd.common.exception.ConfigurationFailureException;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.shared.Constants;

/**
 * Helper class for checking enabled module.
 * 
 * @author Franz-Josef Elmer
 */
public class ModuleEnabledChecker
{
    private final List<Pattern> enabledModulesPatterns;

    public ModuleEnabledChecker(Properties properties)
    {
        this(PropertyUtils.getList(properties, Constants.ENABLED_MODULES_KEY));
    }

    public ModuleEnabledChecker(List<String> moduleRegExs)
    {
        enabledModulesPatterns = new ArrayList<Pattern>();
        for (String regex : moduleRegExs)
        {
            try
            {
                enabledModulesPatterns.add(Pattern.compile(regex));
            } catch (PatternSyntaxException ex)
            {
                throw new ConfigurationFailureException("Invalid regular expression: "
                        + ex.getMessage());
            }
        }
    }

    public boolean isModuleEnabled(String module)
    {
        for (Pattern pattern : enabledModulesPatterns)
        {
            if (pattern.matcher(module).matches())
            {
                return true;
            }
        }
        return false;
    }
}
