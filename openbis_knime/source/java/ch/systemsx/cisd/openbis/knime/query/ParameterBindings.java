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

package ch.systemsx.cisd.openbis.knime.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class ParameterBindings
{
    static final String PARAMETER_KEYS_KEY = "query-parameter-keys";
    static final String PARAMETER_VALUES_KEY = "query-parameter-values";

    private Map<String, String> bindings = new LinkedHashMap<String, String>();
    
    void removeAllBindings()
    {
        bindings.clear();
    }
    
    void bind(String parameter, String value)
    {
        bindings.put(parameter, value);
    }
    
    Map<String, String> getBindings()
    {
        return bindings;
    }
    
    String tryToGetBinding(String parameter)
    {
        return bindings.get(parameter);
    }

    void loadValidatedSettingsFrom(NodeSettingsRO settings)
    {
        String[] parameterKeys;
        String[] parameterValues;
        try
        {
            parameterKeys = settings.getStringArray(PARAMETER_KEYS_KEY);
            parameterValues = settings.getStringArray(PARAMETER_VALUES_KEY);
        } catch (InvalidSettingsException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        bindings.clear();
        for (int i = 0, n = Math.min(parameterKeys.length, parameterValues.length); i < n; i++)
        {
            bindings.put(parameterKeys[i], parameterValues[i]);
        }
    }
    
    void saveSettingsTo(NodeSettingsWO settings)
    {
        List<String> parameterKeys = new ArrayList<String>(bindings.size());
        List<String> parameterValues = new ArrayList<String>(bindings.size());
        Set<Entry<String, String>> entrySet = bindings.entrySet();
        for (Entry<String, String> entry : entrySet)
        {
            parameterKeys.add(entry.getKey());
            parameterValues.add(entry.getValue());
        }
        settings.addStringArray(PARAMETER_KEYS_KEY, parameterKeys.toArray(new String[0]));
        settings.addStringArray(PARAMETER_VALUES_KEY, parameterValues.toArray(new String[0]));
    }
}
