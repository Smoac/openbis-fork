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

package ch.systemsx.cisd.common.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * super class for constructing and holding key-value pairs
 * 
 * @author Tomasz Pylak on Oct 26, 2007
 */
abstract public class AbstractPropertiesSetter<ConstructedType> implements IPropertiesSetter<ConstructedType>
{
    abstract public ConstructedType done();

    // ---------------------

    private final Map<String, String> properties; // map: property name -> valueOrNull

    private final Set<String> mandatoryFields;

    private final Set<String> availableProperties;

    protected AbstractPropertiesSetter(Set<String> availableProperties, Set<String> mandatoryFields)
    {
        this.properties = new HashMap<String, String>();
        this.mandatoryFields = toLowerCase(mandatoryFields);
        this.availableProperties = toLowerCase(availableProperties);
    }

    private static Set<String> toLowerCase(Set<String> set)
    {
        Set<String> result = new HashSet<String>();
        for (String elem : set)
        {
            result.add(elem.toLowerCase());
        }
        return result;
    }

    public void setProperty(String name, String valueOrNull)
    {
        String propName = name.toLowerCase();
        Boolean mandatory = isMandatory(propName);
        if (mandatory != null)
        {
            if (mandatory == true && valueOrNull == null)
            {
                throw createMandatoryException(propName);
            }
            properties.put(propName, valueOrNull);
        } else
        {
            throw UserFailureException.fromTemplate("Unknown property name '%s', failed to set value to '%s'",
                    propName, valueOrNull);
        }
    }

    private static UserFailureException createMandatoryException(String name)
    {
        return UserFailureException.fromTemplate("Property '%s' is mandatory and cannot be set to the empty value.",
                name);
    }

    /** return true if property is mandatory, false if it is optional and null if the property does not exist */
    private Boolean isMandatory(String name)
    {
        if (availableProperties.contains(name))
        {
            return mandatoryFields.contains(name);
        } else
        {
            return null;
        }
    }

    protected void checkMandatoryConstraint()
    {
        for (String name : availableProperties)
        {
            String value = properties.get(name);
            if (mandatoryFields.contains(name) && value == null)
            {
                throw createMandatoryException(name);
            }
        }
    }

    protected String tryGetValue(String name)
    {
        return properties.get(name.toLowerCase());
    }
}
