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
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Performs HTML escaping the string fields of an object. If desired, users can restrict the fields
 * that are escaped by the invoker. Support for controlling the behavior of the escaper by using
 * annotations may come in the future.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ReflectingStringEscaper
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ReflectingStringEscaper.class);

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
        long time = System.currentTimeMillis();
        try
        {
            ReflectingStringEscaperUnrestricted<T> escaper =
                    new ReflectingStringEscaperUnrestricted<T>(true, bean);
            return escaper.escape();
        } finally
        {
            operationLog.info((System.currentTimeMillis() - time) + "ms for escaping "
                    + (bean == null ? "" : bean.getClass()));
        }
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
            {
                return StringEscapeUtils.escapeHtml(value);
            }
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

    private static class Pair
    {
        Object first;

        Object second;

        public Pair(Object first, Object second)
        {
            super();
            this.first = first;
            this.second = second;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((first == null) ? 0 : first.hashCode());
            result = prime * result + ((second == null) ? 0 : second.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (!(obj instanceof Pair))
            {
                return false;
            }
            Pair other = (Pair) obj;
            if (first == null)
            {
                if (other.first != null)
                {
                    return false;
                }
            } else if (!first.equals(other.first))
            {
                return false;
            }
            if (second == null)
            {
                if (other.second != null)
                {
                    return false;
                }
            } else if (!second.equals(other.second))
            {
                return false;
            }
            return true;
        }

    }

    private static class Visitor implements ReflectionStringTraverser.ReflectionFieldVisitor
    {

        private final HashSet<String> unescapedProperties;

        private final HashSet<Pair> seenObjectFields = new HashSet<Pair>();

        private Visitor()
        {
            unescapedProperties = new HashSet<String>();
            // Don't escape these properties
            unescapedProperties.add("permlink");
        }

        public String tryVisit(String value, Object object, Field fieldOrNull)
        {
            Pair pair = new Pair(object, fieldOrNull);
            if (seenObjectFields.contains(pair))
            {
                return null;
            } else
            {
                seenObjectFields.add(pair);
            }
            if (null == fieldOrNull)
            {
                // TODO 2010-11-16, Piotr Buczek: how is this possible?
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
