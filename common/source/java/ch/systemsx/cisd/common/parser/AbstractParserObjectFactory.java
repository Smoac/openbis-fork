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

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ch.systemsx.cisd.common.converter.Converter;
import ch.systemsx.cisd.common.converter.ConverterPool;
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

    /** The pool of {@link Converter}s. */
    private final ConverterPool converterPool;

    /** The class of object bean we are going to create here. */
    private final Class<E> beanClass;

    /** Analyzes specified <code>beanClass</code> for its mandatory resp. optional properties. */
    private final BeanAnalyzer<E> beanAnalyzer;

    protected AbstractParserObjectFactory(final Class<E> beanClass, final IAliasPropertyMapper propertyMapper)
    {
        assert beanClass != null : "Given bean class can not be null.";
        assert propertyMapper != null : "Given property mapper can not be null.";
        beanAnalyzer = new BeanAnalyzer<E>(beanClass);
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
    private final IPropertyModel tryGetPropertyModel(final String name)
    {
        if (propertyMapper.containsPropertyName(name))
        {
            return propertyMapper.getPropertyModel(name);
        }
        return null;
    }

    /**
     * Checks given <code>IPropertyMapper</code>.
     * <p>
     * This method tries to find properties declared in given <code>IPropertyMapper</code> that are not in labels in
     * annotated write methods (throws a <code>UnmatchedPropertiesException</code>) or mandatory fields that could
     * not be found in the same annotated write methods (throws a <code>MandatoryPropertyMissingException</code>).
     * </p>
     */
    private final void checkPropertyMapper(final Class<E> clazz, final IAliasPropertyMapper propMapper)
            throws ParserException
    {
        final Set<String> allPropertyNames = propMapper.getAllPropertyNames();
        final Set<String> propertyNames = new LinkedHashSet<String>(allPropertyNames);
        final Set<String> missingProperties = new LinkedHashSet<String>();
        final Set<String> fieldNames = beanAnalyzer.getLabelToWriteMethods().keySet();
        for (final String fieldName : fieldNames)
        {
            String fieldNameInLowerCase = fieldName.toLowerCase();
            if (propertyNames.contains(fieldNameInLowerCase))
            {
                propertyNames.remove(fieldNameInLowerCase);
            } else if (beanAnalyzer.isMandatory(fieldName))
            {
                missingProperties.add(fieldName);
            }
        }
        final Set<String> mandatoryPropertyNames = getPropertyNames(beanAnalyzer.getMandatoryProperties(), propMapper);
        if (missingProperties.size() > 0)
        {
            throw new MandatoryPropertyMissingException(mandatoryPropertyNames, getPropertyNames(missingProperties,
                    propMapper));
        }
        if (propertyNames.size() > 0)
        {
            Set<String> names = getPropertyNames(beanAnalyzer.getOptionalProperties(), propMapper);
            throw new UnmatchedPropertiesException(clazz, allPropertyNames, mandatoryPropertyNames, names,
                    propertyNames);
        }
    }

    private final static Set<String> getPropertyNames(final Set<String> beanProperties,
            final IAliasPropertyMapper propertyMapper)
    {
        final Set<String> aliases = propertyMapper.getAllAliases();
        final Set<String> propertyNames = new TreeSet<String>();
        for (final String beanProperty : beanProperties)
        {
            if (aliases.contains(beanProperty.toLowerCase()))
            {
                propertyNames.add(propertyMapper.getPropertyNameForAlias(beanProperty));
            } else
            {
                propertyNames.add(beanProperty);
            }
        }
        return propertyNames;
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
        final E object = ClassUtils.createInstance(beanClass);
        for (final Map.Entry<String, Method> entry : beanAnalyzer.getLabelToWriteMethods().entrySet())
        {
            final Method writeMethod = entry.getValue();
            final IPropertyModel propertyModel = tryGetPropertyModel(entry.getKey());
            // They could have some optional descriptors that are not found in the file header.
            // Just ignore them.
            if (propertyModel != null)
            {
                final String propertyValue = getPropertyValue(lineTokens, propertyModel);
                ClassUtils
                        .invokeMethod(writeMethod, object, convert(propertyValue, writeMethod.getParameterTypes()[0]));
            }
        }
        return object;
    }
}