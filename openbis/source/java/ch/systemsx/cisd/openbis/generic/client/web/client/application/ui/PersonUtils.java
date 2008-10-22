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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;

/**
 * Some utility methods around <code>Person</code>.
 * 
 * @author Christian Ribeaud
 */
public final class PersonUtils
{

    private static final char EMAIL_END = '>';

    private static final char EMAIL_START = '<';

    private static final char LOGIN_END = ']';

    private static final char LOGIN_START = '[';

    private PersonUtils()
    {
        // This class can not be instantiated
    }

    /**
     * Parses name (last name and/or first name) from given <var>toString</var> value.
     */
    public final static String parseName(String toString)
    {
        if (toString == null)
        {
            return null;
        }
        final int start = toString.indexOf(EMAIL_START);
        if (start < 0)
        {
            return toString;
        }
        return toString.substring(0, start).trim();
    }

    /**
     * Parses email from given <var>toString</var> value.
     */
    public final static String parseEmail(String toString)
    {
        if (toString == null)
        {
            return null;
        }
        final int start = toString.indexOf(EMAIL_START);
        final int end = toString.indexOf(EMAIL_END);
        if (start < 0 || end < 0)
        {
            return null;
        }
        return toString.substring(start + 1, end);
    }

    /**
     * Returns a pretty and short description of this object.
     * <p>
     * If the fields are not blank, the returned string will have the following format:
     * 
     * <pre>
     * &lt;lastName&gt;, &lt;firstName&gt; &lt;&lt;email&gt;&gt; [userID]
     * </pre>
     * 
     * </p>
     */
    public final static String toString(final Person person)
    {
        assert person != null : "Given person can not be null.";
        final StringBuffer builder = new StringBuffer();
        final String lastName = person.getLastName();
        final String firstName = person.getFirstName();
        if (StringUtils.isBlank(lastName) == false)
        {
            builder.append(lastName);
        }
        if (StringUtils.isBlank(firstName) == false)
        {
            if (builder.length() > 0)
            {
                builder.append(", ");
            }
            builder.append(firstName);
        }

        if (builder.length() != 0)
        {
            builder.append(' ');
        }
        builder.append(LOGIN_START);
        builder.append(person.getUserId());
        builder.append(LOGIN_END);

        final String email = person.getEmail();
        if (StringUtils.isBlank(email) == false && builder.length() > 0)
        {
            builder.append(" ").append(EMAIL_START);
            builder.append(email);
            builder.append(EMAIL_END);
        }
        return builder.toString();
    }
}
