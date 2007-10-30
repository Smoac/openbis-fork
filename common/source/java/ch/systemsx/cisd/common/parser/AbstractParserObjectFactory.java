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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.converter.Converter;
import ch.systemsx.cisd.common.converter.ConverterPool;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
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

    /** The set of mandatory field names for {@link NewExperiment} */
    private final Set<String> mandatoryFields;

    /** The pool of {@link Converter}s. */
    private final ConverterPool converterPool;

    /** The class of object bean we are going to create here. */
    private final Class<E> beanClass;

    protected AbstractParserObjectFactory(Class<E> beanClass, IPropertyMapper propertyMapper)
    {
        assert beanClass != null;
        assert propertyMapper != null;
        propertyDescriptors = BeanUtils.getPropertyDescriptors(beanClass);
        mandatoryFields = ClassUtils.getMandatoryFields(beanClass);
        assert mandatoryFields != null;
        checkPropertyMapper(beanClass, propertyMapper);
        this.propertyMapper = propertyMapper;
        converterPool = createConverterPool();
        this.beanClass = beanClass;
    }

    private final static ConverterPool createConverterPool()
    {
        return new ConverterPool();
    }

    /**
     * Registers given <var>converter</var> for given class type.
     * <p>
     * If given <var>converter</var>, then it will unregister it.
     * </p>
     */
    protected final <T> void registerConverter(Class<T> clazz, Converter<T> converter)
    {
        if (converter == null)
        {
            converterPool.unregisterConverter(clazz);
        } else
        {
            converterPool.registerConverter(clazz, converter);
        }
    }

    private final <T> T convert(String value, Class<T> type)
    {
        return converterPool.convert(value, type);
    }

    /** For given property name returns corresponding <code>IPropertyModel</code>. */
    private final IPropertyModel getPropertyModel(String name)
    {
        return propertyMapper.getProperty(name);
    }

    /** Returns an unmodifiable list of <code>PropertyDescriptor</code>s. */
    private final Collection<PropertyDescriptor> getPropertyDescriptors()
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
        assert propertyDescriptors != null;

        Set<String> propertyNames = new HashSet<String>(propMapper.getAllPropertyNames());
        propertyNames.removeAll(propertyDescriptors.keySet());
        if (propertyNames.size() > 0)
        {
            throw UserFailureException.fromTemplate("The following header columns are not part of '%s': %s", clazz
                    .getSimpleName(), format(propertyNames));
        }
    }

    private final String format(Set<String> set)
    {
        final StringBuilder builder = new StringBuilder();
        for (String s : set)
        {
            builder.append("'");
            builder.append(s);
            builder.append("', ");
        }
        // Remove trailing ", "
        builder.setLength(builder.length() - 2);
        return builder.toString();
    }

    /** Whether given field name is mandatory. */
    private final boolean isMandatory(String fieldName)
    {
        return mandatoryFields.contains(fieldName);
    }

    private String getPropertyValue(final String[] lineTokens, final IPropertyModel propertyModel)
    {
        int column = propertyModel.getColumn();
        if (column >= lineTokens.length)
        {
            throw UserFailureException.fromTemplate("Not enough tokens are available (index: %d, available: %d)",
                    column, lineTokens.length);
        }
        return lineTokens[column];
    }

    //
    // IParserObjectFactory
    //

    @SuppressWarnings("unchecked")
    public E createObject(String[] lineTokens)
    {
        try
        {
            Object object = beanClass.newInstance();
            for (PropertyDescriptor descriptor : getPropertyDescriptors())
            {
                final Method writeMethod = descriptor.getWriteMethod();
                final IPropertyModel propertyModel = getPropertyModel(descriptor.getName());
                if (propertyModel != null)
                {
                    writeMethod.invoke(object, convert(getPropertyValue(lineTokens, propertyModel), writeMethod
                            .getParameterTypes()[0]));
                } else
                {
                    // If the corresponding bean field is mandatory and <code>propertyModel</code> is null,
                    // then we should throw an exception.
                    final String fieldName = descriptor.getName();
                    if (isMandatory(fieldName))
                    {
                        throw UserFailureException.fromTemplate("Field/Property name '%s' is mandatory.", fieldName);
                    }
                }
            }
            return (E) object;
        } catch (IllegalAccessException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        } catch (InvocationTargetException ex)
        {
            // We are interested in the cause exception.
            throw CheckedExceptionTunnel.wrapIfNecessary((Exception) ex.getCause());
        } catch (InstantiationException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        }
    }
}