/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Performs HTML escaping the string fields of an object. Restricts itself to the properties
 * specified by the invoker.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ReflectingStringEscaper<T>
{
    public static <T> T escape(T bean, String... escapedProperties)
    {
        ReflectingStringEscaper<T> escaper =
                new ReflectingStringEscaper<T>(bean, escapedProperties);
        return escaper.escape();
    }

    private final T bean;

    private final HashSet<String> escapedProperties;

    private class Visitor implements ReflectionStringTraverser.ReflectionFieldVisitor
    {

        public String tryVisit(String value, Object object, Field fieldOrNull)
        {
            // Only change the value on the top-level object
            if (object != bean)
            {
                return null;
            }
            // Only change the value if the name of the field is in the list provided
            if (null == fieldOrNull)
            {
                return null;
            }
            if (escapedProperties.contains(fieldOrNull.getName()))
                return StringEscapeUtils.escapeHtml(value);
            return null;
        }
    }

    private ReflectingStringEscaper(T bean, String... escapedProperties)
    {
        this.bean = bean;
        this.escapedProperties = new HashSet<String>();
        Collections.addAll(this.escapedProperties, escapedProperties);
    }

    private T escape()
    {
        ReflectionStringTraverser.traverse(bean, new Visitor());
        return bean;
    }
}
