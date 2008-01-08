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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.converter.Converter;
import ch.systemsx.cisd.common.converter.ConverterPool;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
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

    /** The set of mandatory field names. */
    private final Set<String> mandatoryFields;

    /** The pool of {@link Converter}s. */
    private final ConverterPool converterPool;

    /** The class of object bean we are going to create here. */
    private final Class<E> beanClass;

    protected AbstractParserObjectFactory(final Class<E> beanClass, final IAliasPropertyMapper propertyMapper)
    {
        assert beanClass != null : "Given bean class can not be null.";
        assert propertyMapper != null : "Given property mapper can not be null.";
        propertyDescriptors = BeanUtils.getPropertyDescriptors(beanClass);
        mandatoryFields = ClassUtils.getMandatoryFields(beanClass, true);
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
    protected final <T> void registerConverter(final Class<T> clazz, final Converter<T> converter)
    {
        if (converter == null)
        {
            converterPool.unregisterConverter(clazz);
        } else
        {
            converterPool.registerConverter(clazz, converter);
        }
    }

    private final <T> T convert(final String value, final Class<T> type)
    {
        return converterPool.convert(value, type);
    }

    /** For given property name returns corresponding <code>IPropertyModel</code>. */
    private final IPropertyModel getPropertyModel(final String name)
    {
        return propertyMapper.getProperty(name);
    }

    /**
     * Checks given <code>IPropertyMapper</code>.
     * <p>
     * This method tries to find properties declared in given <code>IPropertyMapper</code> that are not in
     * {@link #propertyDescriptors}.
     * </p>
     */
    private final void checkPropertyMapper(final Class<E> clazz, final IAliasPropertyMapper propMapper)
            throws ParserException
    {
        final Set<String> allPropertyNames = propMapper.getAllPropertyNames();
        final Set<String> propertyNames = new LinkedHashSet<String>(allPropertyNames);
        final Set<String> missingProperties = new LinkedHashSet<String>();
        final Set<String> fieldNames = propertyDescriptors.keySet();
        for (final String fieldName : fieldNames)
        {
            if (propertyNames.contains(fieldName))
            {
                propertyNames.remove(fieldName);
            } else if (isMandatory(fieldName))
            {
                missingProperties.add(fieldName);
            }
        }
        if (missingProperties.size() > 0)
        {
            throw new MandatoryPropertyMissingException(getPropertyNames(mandatoryFields, propMapper),
                    getPropertyNames(missingProperties, propMapper));
        }
        if (propertyNames.size() > 0)
        {
            throw new UnmatchedPropertiesException(clazz, allPropertyNames, fieldNames, propertyNames);
        }
    }

    private final static Set<String> getPropertyNames(final Set<String> beanNames,
            final IAliasPropertyMapper propertyMapper)
    {
        final Set<String> propertyNames = new LinkedHashSet<String>(beanNames.size());
        for (final String beanName : beanNames)
        {
            final String propertyName = propertyMapper.tryGetPropertyName(beanName);
            if (propertyName == null)
            {
                propertyNames.add(beanName);
            } else
            {
                propertyNames.add(propertyName);
            }
        }
        return propertyNames;
    }

    /** Whether given field name is mandatory. */
    private final boolean isMandatory(final String fieldName)
    {
        return mandatoryFields.contains(fieldName);
    }

    private final String getPropertyValue(final String[] lineTokens, final IPropertyModel propertyModel)
    {
        final int column = propertyModel.getColumn();
        if (column >= lineTokens.length)
        {
            throw new IndexOutOfBoundsException(column, lineTokens);
        }
        return lineTokens[column];
    }

    //
    // IParserObjectFactory
    //

    public E createObject(final String[] lineTokens) throws ParserException
    {
        try
        {
            final E object = beanClass.newInstance();
            for (final PropertyDescriptor descriptor : propertyDescriptors.values())
            {
                final Method writeMethod = descriptor.getWriteMethod();
                final IPropertyModel propertyModel = getPropertyModel(descriptor.getName());
                // They could have some optional descriptors that are not in the property model.
                // Just ignore them.
                if (propertyModel != null)
                {
                    final String propertyValue = getPropertyValue(lineTokens, propertyModel);
                    writeMethod.invoke(object, convert(propertyValue, writeMethod.getParameterTypes()[0]));
                }
            }
            return object;
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