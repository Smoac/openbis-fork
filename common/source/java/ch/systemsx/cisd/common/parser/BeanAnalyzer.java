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

package ch.systemsx.cisd.common.parser;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ch.systemsx.cisd.common.annotation.BeanProperty;
import ch.systemsx.cisd.common.utilities.ClassUtils;

/**
 * A <i>Bean</i> class analyzer.
 * <p>
 * Its main role is to analyze a given <i>Bean</i> class and to list its mandatory resp. optional properties.
 * </p>
 * 
 * @author Christian Ribeaud
 */
final class BeanAnalyzer<T>
{

    private final Class<T> beanClass;

    private final Set<String> mandatoryProperties = new TreeSet<String>();

    private final Set<String> optionalProperties = new TreeSet<String>();

    BeanAnalyzer(final Class<T> beanClass)
    {
        this.beanClass = beanClass;
        fillProperties();
    }

    private final void fillProperties()
    {
        final List<Field> annotatedFields = ClassUtils.getAnnotatedFieldList(beanClass, BeanProperty.class);
        for (final Field field : annotatedFields)
        {
            final BeanProperty annotation = field.getAnnotation(BeanProperty.class);
            final String fieldName = field.getName();
            final boolean optional = annotation.optional();
            checkUnique(fieldName, optional);
            if (optional)
            {
                optionalProperties.add(fieldName);
            } else
            {
                mandatoryProperties.add(fieldName);
            }
        }
    }

    private final void checkUnique(final String fieldName, final boolean optional)
    {
        assert optionalProperties.contains(fieldName) == false && mandatoryProperties.contains(fieldName) == false : String
                .format("%s bean property '%s' already found and must be unique.", optional ? "Optional" : "Mandatory",
                        fieldName);
    }

    /** Whether given <code>property</code> is mandatory. */
    final boolean isMandatory(final String property)
    {
        return mandatoryProperties.contains(property);
    }

    /**
     * Returns the mandatory properties found in specified <code>beanClass</code>.
     * 
     * @return never <code>null</code> but could return an empty unmodifiable <code>Set</code>.
     */
    final Set<String> getMandatoryProperties()
    {
        return Collections.unmodifiableSet(mandatoryProperties);
    }

    /**
     * Returns the optional properties found in specified <code>beanClass</code>.
     * 
     * @return never <code>null</code> but could return an empty unmodifiable <code>Set</code>.
     */
    final Set<String> getOptionalProperties()
    {
        return Collections.unmodifiableSet(optionalProperties);
    }
}