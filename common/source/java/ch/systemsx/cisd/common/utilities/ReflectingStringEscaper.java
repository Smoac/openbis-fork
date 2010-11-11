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
 * Performs HTML escaping the string fields of an object. If desired, users can restrict the fields
 * that are escaped by the invoker. Support for controlling the behavior of the escaper by using
 * annotations may come in the future.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ReflectingStringEscaper
{

    /**
     * Escape all the string fields on the bean.
     */
    public static <T> T escapeShallow(T bean)
    {
        ReflectingStringEscaperUnrestricted<T> escaper =
                new ReflectingStringEscaperUnrestricted<T>(false, bean);
        return escaper.escape();
    }

    /**
     * Escape all the string fields on the bean and all fields of objects referred to by the bean.
     */
    public static <T> T escapeDeep(T bean)
    {
        ReflectingStringEscaperUnrestricted<T> escaper =
                new ReflectingStringEscaperUnrestricted<T>(true, bean);
        return escaper.escape();
    }

    /**
     * Escape the specified string fields on the bean.
     */
    public static <T> T escapeShallowRestricted(T bean, String... escapedProperties)
    {
        ReflectingStringEscaperRestricted<T> escaper =
                new ReflectingStringEscaperRestricted<T>(false, bean, escapedProperties);
        return escaper.escape();
    }

    /**
     * Escape all the string fields on the bean and all fields of objects referred to by the bean,
     * restricting the field names to those specified.
     */
    public static <T> T escapeDeepRestricted(T bean, String... escapedProperties)
    {
        ReflectingStringEscaperRestricted<T> escaper =
                new ReflectingStringEscaperRestricted<T>(true, bean, escapedProperties);
        return escaper.escape();
    }
}

/**
 * @author Chandrasekhar Ramakrishnan
 */
class ReflectingStringEscaperImpl<T>
{

    protected final boolean isDeep;

    protected final T bean;

    /**
     *
     *
     */
    protected ReflectingStringEscaperImpl(boolean isDeep, T bean)
    {
        this.isDeep = isDeep;
        this.bean = bean;
    }

    protected T traverse(ReflectionStringTraverser.ReflectionFieldVisitor visitor)
    {
        if (isDeep)
        {
            ReflectionStringTraverser.traverseDeep(bean, visitor);
        } else
        {
            ReflectionStringTraverser.traverseShallow(bean, visitor);
        }
        return bean;
    }

}

/**
 * Utility Class that preforms the restricted escaping.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class ReflectingStringEscaperRestricted<T> extends ReflectingStringEscaperImpl<T>
{
    private final HashSet<String> escapedProperties;

    private class Visitor implements ReflectionStringTraverser.ReflectionFieldVisitor
    {

        public String tryVisit(String value, Object object, Field fieldOrNull)
        {
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

    ReflectingStringEscaperRestricted(boolean isDeep, T bean, String... escapedProperties)
    {
        super(isDeep, bean);
        this.escapedProperties = new HashSet<String>();
        Collections.addAll(this.escapedProperties, escapedProperties);
    }

    T escape()
    {
        return traverse(new Visitor());
    }
}

/**
 * Utility Class that preforms the unrestricted escaping.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class ReflectingStringEscaperUnrestricted<T> extends ReflectingStringEscaperImpl<T>
{

    private static class Visitor implements ReflectionStringTraverser.ReflectionFieldVisitor
    {

        private final HashSet<String> unescapedProperties;

        private Visitor()
        {
            unescapedProperties = new HashSet<String>();
            // Don't escape these properties
            unescapedProperties.add("permlink");
        }

        public String tryVisit(String value, Object object, Field fieldOrNull)
        {
            if (null == fieldOrNull)
            {
                return StringEscapeUtils.escapeHtml(value);
            }

            // Don't escape the ones that are specified as not to be escaped
            if (unescapedProperties.contains(fieldOrNull.getName()))
            {
                return null;
            }

            return StringEscapeUtils.escapeHtml(value);

        }
    }

    ReflectingStringEscaperUnrestricted(boolean isDeep, T bean)
    {
        super(isDeep, bean);
    }

    T escape()
    {
        return traverse(new Visitor());
    }
}
