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
import ch.systemsx.cisd.bds.storage.INode;

/**
 * Implementation of {@link IFormatParameters} which allows to add {@link FormatParameter} instances.
 * 
 * @author Franz-Josef Elmer
 */
final class FormatParameters implements IFormatParameters, IStorable
{
    private final Map<String, FormatParameter> parameters =
            new LinkedHashMap<String, FormatParameter>();

    /**
     * The <code>IFormatParameterFactory</code> implementation used here.
     * <p>
     * Initialized with the default implementation {@link IFormatParameterFactory#DEFAULT_FORMAT_PARAMETER_FACTORY}.
     * </p>
     */
    private IFormatParameterFactory formatParameterFactory =
            IFormatParameterFactory.DEFAULT_FORMAT_PARAMETER_FACTORY;

    final void loadFrom(final IDirectory directory)
    {
        parameters.clear();
        for (INode node : directory)
        {
            final FormatParameter formatParameter =
                    formatParameterFactory.createFormatParameter(node);
            if (formatParameter != null)
            {
                addParameter(formatParameter);
            }
        }
    }

    //
    // IStorable
    //

    public final void saveTo(final IDirectory directory)
    {
        for (final FormatParameter parameter : parameters.values())
        {
            final Object value = parameter.getValue();
            assert value != null : "Parameter value can not be null.";
            if (value instanceof IStorable)
            {
                ((IStorable) value).saveTo(directory);
            } else
            {
                directory.addKeyValuePair(parameter.getName(), value.toString());
            }
        }
    }

    /** Sets a different <code>IFormatParameterFactory</code> implementation than the default one. */
    final void setFormatParameterFactory(final IFormatParameterFactory formatParameterFactory)
    {
        this.formatParameterFactory = formatParameterFactory;
    }

    /**
     * Adds the specified parameter.
     * 
     * @throws IllegalArgumentException if a parameter with same name as given <var>parameter</var> already exists. To
     *             check whether given <var>parameter</var> is already present, use {@link #containsParameter(String)}.
     */
    final void addParameter(final FormatParameter parameter)
    {
        final String parameterName = parameter.getName();
        if (containsParameter(parameterName))
        {
            throw new IllegalArgumentException("There is already a parameter named '"
                    + parameterName + "'.");
        }
        parameters.put(parameterName, parameter);
    }

    //
    // IFormatParameters
    //

    public final Object getValue(final String parameterName)
    {
        FormatParameter formatParameter = parameters.get(parameterName);
        if (formatParameter == null)
        {
            throw new IllegalArgumentException("Unknown parameter '" + parameterName + "'.");
        }
        return formatParameter.getValue();
    }

    public final Iterator<FormatParameter> iterator()
    {
        return parameters.values().iterator();
    }

    public final boolean containsParameter(final String parameterName)
    {
        return parameters.containsKey(parameterName);
    }
}
