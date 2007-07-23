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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.ClassUtils;

/**
 * An abstract <code>IParserObjectFactory</code> which already implements and offers convenience methods.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractParserObjectFactory<E> implements IParserObjectFactory<E>
{

    /** The <code>IPropertyMapper</code> implementation. */
    private final IPropertyMapper propertyMapper;

    /**
     * A <code>Map</code> of <code>PropertyDescriptor</code>s for typed <code>Object</code>, keyed by their name ({@link PropertyDescriptor#getName()}).
     */
    private final Map<String, PropertyDescriptor> propertyDescriptors;

    /** The list of mandatory <code>Field</code>s for {@link NewExperiment}, keyed by their name. */
    private final Map<String, Field> mandatoryFields;

    protected AbstractParserObjectFactory(Class<E> clazz, IPropertyMapper propertyMapper)
    {
        propertyDescriptors = BeanUtils.getPropertyDescriptors(clazz);
        mandatoryFields = createMandatoryFields(clazz);
        checkPropertyMapper(clazz, propertyMapper);
        this.propertyMapper = propertyMapper;
    }

    /** For given property name returns corresponding <code>IPropertyModel</code>. */
    protected final IPropertyModel getPropertyModel(String name)
    {
        return propertyMapper.getProperty(name);
    }

    /** Returns an unmodifiable list of <code>PropertyDescriptor</code>s. */
    protected final Collection<PropertyDescriptor> getPropertyDescriptors()
    {
        return Collections.unmodifiableCollection(propertyDescriptors.values());
    }

    /**
     * Checks given <code>IPropertyMapper</code>.
     * <p>
     * This method tries to find properties declared in given <code>IPropertyMapper</code> that are not in
     * {@link #propertyDescriptors}.
     * </p>
     */
    private final void checkPropertyMapper(Class<E> clazz, IPropertyMapper propMapper) throws UserFailureException
    {
        assert propMapper != null;
        assert clazz != null;
        assert propertyDescriptors != null;

        Set<String> propertyNames = new HashSet<String>(propMapper.getAllPropertyNames());
        propertyNames.removeAll(propertyDescriptors.keySet());
        if (propertyNames.size() > 0)
        {
            throw UserFailureException.fromTemplate("Following properties '%s' are not part of '%s'.", propertyNames,
                    clazz.getSimpleName());
        }
    }

    /**
     * Analyzes given <code>Class</code> and returns a <code>Map</code> containing the mandatory <code>Field</code>s
     * keyed by {@link Field#getName()}.
     */
    private final static Map<String, Field> createMandatoryFields(Class clazz)
    {
        Map<String, Field> map = new HashMap<String, Field>();
        List<Field> fields = ClassUtils.getMandatoryFields(clazz);
        for (Field field : fields)
        {
            map.put(field.getName(), field);
        }
        return map;
    }

    /** Whether given field name is mandatory. */
    protected final boolean isMandatory(String fieldName)
    {
        assert mandatoryFields != null;

        return mandatoryFields.containsKey(fieldName);
    }

}