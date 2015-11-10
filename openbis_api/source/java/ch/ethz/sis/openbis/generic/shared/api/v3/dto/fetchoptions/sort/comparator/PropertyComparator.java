/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.comparator;

import java.util.Map;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IPropertiesHolder;

/**
 * @author pkupczyk
 */
public class PropertyComparator<OBJECT> extends AbstractStringComparator<OBJECT>
{

    public static final String PROPERTY = "PROPERTY";

    private String propertyName;

    public PropertyComparator(String propertyName)
    {
        this.propertyName = propertyName;
    }

    @Override
    protected String getValue(OBJECT o)
    {
        if (o instanceof IPropertiesHolder)
        {
            Map<String, String> properties = ((IPropertiesHolder) o).getProperties();
            return properties != null ? properties.get(propertyName) : null;
        } else
        {
            throw new IllegalArgumentException("Object " + o + " does not implement " + IPropertiesHolder.class
                    + " interface therefore cannot be sorted by a property value.");
        }
    }
}
