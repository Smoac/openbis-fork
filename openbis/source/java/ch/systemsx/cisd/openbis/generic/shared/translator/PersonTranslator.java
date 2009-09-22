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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * A {@link Person} &lt;---&gt; {@link PersonPE} translator.
 * 
 * @author    Franz-Josef Elmer
 */
public class PersonTranslator
{
    private PersonTranslator()
    {
        // Can not be instantiated.
    }

    public final static List<Person> translate(final Collection<PersonPE> persons)
    {
        final List<Person> result = new ArrayList<Person>();
        for (final PersonPE person : persons)
        {
            result.add(PersonTranslator.translate(person));
        }
        return result;
    }

    public final static Person translate(final PersonPE person)
    {
        return translate(person, true);
    }

    private final static Person translate(final PersonPE person, final boolean recursively)
    {
        if (person == null)
        {
            return null;
        }
        final Person result = new Person();
        result.setFirstName(StringEscapeUtils.escapeHtml(person.getFirstName()));
        result.setLastName(StringEscapeUtils.escapeHtml(person.getLastName()));
        result.setEmail(StringEscapeUtils.escapeHtml(person.getEmail()));
        result.setUserId(StringEscapeUtils.escapeHtml(person.getUserId()));
        result.setRegistrationDate(person.getRegistrationDate());
        if (recursively)
        {
            result.setRegistrator(PersonTranslator.translate(person.getRegistrator(), false));
        }
        return result;
    }

}
