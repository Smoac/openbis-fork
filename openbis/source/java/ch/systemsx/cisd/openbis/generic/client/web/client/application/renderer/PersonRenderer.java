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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DOMUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;

/**
 * A <i>static</i> class to render {@link Person}.
 * 
 * @author Christian Ribeaud
 */
public final class PersonRenderer
{

    private static final char EMAIL_END = '>';

    private static final char EMAIL_START = '<';

    private static final char LOGIN_END = ']';

    private static final char LOGIN_START = '[';

    private PersonRenderer()
    {
        // This class can not be instantiated
    }

    private final static StringBuilder createPersonName(final Person person)
    {
        final StringBuilder builder = new StringBuilder();
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
        return builder;
    }

    /**
     * Creates an <i>HTML</i> A element for given <var>person</var> representation.
     */
    public final static String createPersonAnchor(final Person person)
    {
        final String email = person.getEmail();
        final String name = createPersonName(person).toString();
        if (email != null)
        {
            final Element anchor = DOMUtils.createAnchorElement(null, "mailto:" + email, email);
            DOM.setInnerText(anchor, name);
            return DOM.toString(anchor);
        } else
        {
            return name;
        }
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
        final StringBuilder builder = new StringBuilder();
        builder.append(createPersonName(person));

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
