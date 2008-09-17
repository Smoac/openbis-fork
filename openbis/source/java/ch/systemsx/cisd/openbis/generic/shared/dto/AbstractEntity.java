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

package ch.systemsx.cisd.openbis.generic.shared.dto;

/**
 * Abstract super class of all Data Transfer Objects having {@link SimpleEntityProperty}s.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractEntity<T extends AbstractEntity<T>> extends Code<T> implements
        ISimpleEntityPropertiesHolder
{
    private static final long serialVersionUID = 1L;

    private SimpleEntityProperty[] properties;

    //
    // IEntityPropertiesHolder
    //

    public final SimpleEntityProperty[] getProperties()
    {
        return properties;
    }

    public final void setProperties(final SimpleEntityProperty[] properties)
    {
        this.properties = properties;
    }

}
