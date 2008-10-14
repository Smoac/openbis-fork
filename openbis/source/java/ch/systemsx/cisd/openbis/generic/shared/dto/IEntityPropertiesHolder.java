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

import java.util.List;

/**
 * Interface of classes having entity properties.
 * 
 * @author Christian Ribeaud
 */
public interface IEntityPropertiesHolder<T extends EntityPropertyPE>
{
    /**
     * Gets a copy of the entity properties. Note that the property collection returned must not be
     * modified directory or else the bidirectional relation of entities and properties might be
     * broken (and the second-level cache is going to cache these broken objects).
     */
    public List<T> getProperties();

    /**
     * Sets the entity properties.
     */
    public void setProperties(final List<T> properties);

    /**
     * Adds the <var>property</var> to the list of properties of this property holder.
     */
    public void addProperty(T property);

}