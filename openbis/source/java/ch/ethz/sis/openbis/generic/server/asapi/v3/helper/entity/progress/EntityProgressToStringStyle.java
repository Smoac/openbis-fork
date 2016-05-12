/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue;

/**
 * @author pkupczyk
 */
public final class EntityProgressToStringStyle extends ToStringStyle
{

    public static final ToStringStyle ENTITY_PROGRESS_STYLE = new EntityProgressToStringStyle();

    private static final long serialVersionUID = 1L;

    EntityProgressToStringStyle()
    {
        super();
        this.setContentStart("[");
        this.setContentEnd("]");
        this.setArrayStart("[");
        this.setArrayEnd("]");
        this.setArraySeparator(", ");
        this.setFieldSeparator(", ");
        this.setFieldSeparatorAtStart(false);
        this.setUseShortClassName(true);
        this.setUseIdentityHashCode(false);
        this.setNullText("null");
    }

    private Object readResolve()
    {
        return ENTITY_PROGRESS_STYLE;
    }

    @Override
    public void append(StringBuffer buffer, String fieldName, Object value, Boolean fullDetail)
    {
        if (value != null)
        {
            if (value instanceof FieldUpdateValue<?>)
            {
                FieldUpdateValue<?> fieldValue = (FieldUpdateValue<?>) value;
                if (fieldValue.isModified())
                {
                    append(buffer, fieldName, fieldValue.getValue(), fullDetail);
                }
            } else if (value instanceof ListUpdateValue<?, ?, ?, ?>)
            {
                ListUpdateValue<?, ?, ?, ?> listValue = (ListUpdateValue<?, ?, ?, ?>) value;
                if (listValue.hasActions())
                {
                    Collection<?> added = listValue.getAdded();
                    if (added != null && false == added.isEmpty())
                    {
                        append(buffer, fieldName + ".ADD", added, fullDetail);
                    }
                    Collection<?> removed = listValue.getRemoved();
                    if (removed != null && false == removed.isEmpty())
                    {
                        append(buffer, fieldName + ".REMOVE", removed, fullDetail);
                    }
                    Collection<?> set = listValue.getSet();
                    if (set != null && false == set.isEmpty())
                    {
                        append(buffer, fieldName + ".SET", set, fullDetail);
                    }
                }
            } else
            {
                super.append(buffer, fieldName, value, fullDetail);
            }
        }
    }

    @Override
    protected void appendDetail(StringBuffer buffer, String fieldName, Object value)
    {
        if (value.getClass().getName().startsWith("ch.ethz") && value.getClass().getName().contains("v3"))
        {
            buffer.append(ReflectionToStringBuilder.toString(value, this));
        } else
        {
            buffer.append(value);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void appendDetail(StringBuffer buffer, String fieldName, Collection coll)
    {
        buffer.append("[");

        int size = coll.size();
        int index = 0;

        for (Object item : coll)
        {
            appendInternalOrNull(buffer, fieldName, item);
            index++;
            if (index < size)
            {
                buffer.append(", ");
            }
        }

        buffer.append("]");
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void appendDetail(StringBuffer buffer, String fieldName, Map map)
    {
        buffer.append("{");

        int size = map.size();
        int index = 0;

        for (Object key : map.keySet())
        {
            appendInternalOrNull(buffer, fieldName, key);
            buffer.append("=");
            appendInternalOrNull(buffer, fieldName, map.get(key));
            index++;
            if (index < size)
            {
                buffer.append(", ");
            }
        }

        buffer.append("}");
    }

    private void appendInternalOrNull(StringBuffer buffer, String fieldName, Object value)
    {
        if (value == null)
        {
            appendNullText(buffer, fieldName);
        } else
        {
            appendInternal(buffer, fieldName, value, true);
        }
    }

}
