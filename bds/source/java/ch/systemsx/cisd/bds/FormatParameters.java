/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.INode;

/**
 * Implementation of {@link IFormatParameters} which allows to add {@link FormatParameter} instances.
 * 
 * @author Franz-Josef Elmer
 */
class FormatParameters implements IFormatParameters, IStorable
{
    private final Map<String, FormatParameter> parameters = new LinkedHashMap<String, FormatParameter>();

    void loadFrom(IDirectory directory)
    {
        parameters.clear();
        for (INode node : directory)
        {
            if (node instanceof IFile)
            {
                IFile file = (IFile) node;
                addParameter(new FormatParameter(file.getName(), file.getStringContent().trim()));
            }
        }
    }

    //
    // IStorable
    //

    public final void saveTo(final IDirectory directory)
    {
        for (FormatParameter parameter : parameters.values())
        {
            directory.addKeyValuePair(parameter.getName(), parameter.getValue());
        }
    }

    /**
     * Adds the specified parameter.
     * 
     * @throws IllegalArgumentException if they is already a parameter with same name as <code>parameter</code>.
     */
    void addParameter(FormatParameter parameter)
    {
        String name = parameter.getName();
        if (parameters.containsKey(name))
        {
            throw new IllegalArgumentException("There is already a parameter named '" + name + "'.");
        }
        parameters.put(name, parameter);
    }

    public String getValue(String parameterName)
    {
        FormatParameter formatParameter = parameters.get(parameterName);
        if (formatParameter == null)
        {
            throw new IllegalArgumentException("Unknown parameter '" + parameterName + "'.");
        }
        return formatParameter.getValue();
    }

    public Iterator<FormatParameter> iterator()
    {
        return parameters.values().iterator();
    }

}
