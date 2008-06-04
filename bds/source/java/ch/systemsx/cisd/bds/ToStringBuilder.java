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

package ch.systemsx.cisd.bds;

/**
 * An helper class to construct a nice {@link Object#toString()} output.
 * 
 * @author Christian Ribeaud
 */
public final class ToStringBuilder
{

    /**
     * The content start <code>'['</code>.
     */
    private final static String CONTENT_START = "[";

    /**
     * The content end <code>']'</code>.
     */
    private final static String CONTENT_END = "]";

    /**
     * The field name value separator <code>'='</code>.
     */
    private final static String FIELD_NAME_VALUE_SEPARATOR = "=";

    /**
     * The field separator <code>','</code>.
     */
    private final static String DEFAULT_FIELD_SEPARATOR = ",";

    private final StringBuilder builder;

    private final String fieldSeparator;

    public ToStringBuilder(final String fieldSeparator)
    {
        this.fieldSeparator = fieldSeparator;
        this.builder = new StringBuilder(CONTENT_START);
    }

    public ToStringBuilder()
    {
        this(DEFAULT_FIELD_SEPARATOR);
    }

    private final void appendFieldSeparator()
    {
        if (builder.length() > CONTENT_START.length())
        {
            builder.append(fieldSeparator);
        }
    }

    public final void append(final String fieldName, final Object fieldValue)
    {
        appendFieldSeparator();
        builder.append(fieldName).append(FIELD_NAME_VALUE_SEPARATOR).append(fieldValue);
    }

    public final void append(final Object object)
    {
        appendFieldSeparator();
        builder.append(object.getClass().getSimpleName()).append(FIELD_NAME_VALUE_SEPARATOR)
                .append(object);
    }

    @Override
    public final String toString()
    {
        builder.append(CONTENT_END);
        return builder.toString();
    }
}
