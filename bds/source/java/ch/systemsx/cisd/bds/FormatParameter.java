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

/**
 * A format parameter with a name and a string value.
 * 
 * @author Franz-Josef Elmer
 */
public final class FormatParameter
{
    private final String name;

    private final Object value;

    /**
     * Creates an instance for the specified name and value.
     * 
     * @param name A non-empty string as the name of the parameter.
     * @param value A non-<code>null</code> string as the value.
     */
    public FormatParameter(final String name, final Object value)
    {
        assert name != null && name.length() > 0 : "Unspecified parameter name.";
        this.name = name;
        assert value != null : "Unspecified parameter value.";
        this.value = value;
    }

    /**
     * Returns the name of this parameter.
     */
    public final String getName()
    {
        return name;
    }

    /**
     * Returns the value of this parameter.
     */
    public final Object getValue()
    {
        return value;
    }
}
