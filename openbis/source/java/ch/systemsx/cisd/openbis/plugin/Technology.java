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

package ch.systemsx.cisd.openbis.plugin;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A technology.
 * 
 * @author Christian Ribeaud
 */
public final class Technology
{
    private final String name;

    public Technology(final String name)
    {
        assert name != null : "Unspecified name.";
        this.name = name;
    }

    /**
     * Returns the description of this technology.
     */
    public final String getDescription()
    {
        return StringUtils.capitalize(getName().toLowerCase());
    }

    /**
     * Returns the name of this technology.
     */
    public final String getName()
    {
        return name;
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public final int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public final String toString()
    {
        return getDescription();
    }
}
