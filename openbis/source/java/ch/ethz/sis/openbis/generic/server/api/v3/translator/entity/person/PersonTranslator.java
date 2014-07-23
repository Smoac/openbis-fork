/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.space.SpaceTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.person.PersonFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
public class PersonTranslator extends AbstractCachingTranslator<PersonPE, Person, PersonFetchOptions>
{
    public PersonTranslator(TranslationContext translationContext, PersonFetchOptions fetchOptions)
    {
        super(translationContext, fetchOptions);
    }

    @Override
    protected Person createObject(PersonPE person)
    {
        Person result = new Person();

        result.setUserId(person.getUserId());
        result.setFirstName(person.getFirstName());
        result.setLastName(person.getLastName());
        result.setEmail(person.getEmail());
        result.setRegistrationDate(person.getRegistrationDate());
        result.setActive(person.isActive());
        result.setFetchOptions(new PersonFetchOptions());

        return result;
    }

    @Override
    protected void updateObject(PersonPE person, Person result)
    {
        if (getFetchOptions().hasSpace())
        {
            result.setSpace(new SpaceTranslator(getTranslationContext(), getFetchOptions().fetchSpace()).translate(person.getHomeSpace()));
            result.getFetchOptions().fetchSpace(getFetchOptions().fetchSpace());
        }

        if (getFetchOptions().hasRegistrator())
        {
            result.setRegistrator(new PersonTranslator(getTranslationContext(), getFetchOptions().fetchRegistrator()).translate(person
                    .getRegistrator()));
            result.getFetchOptions().fetchRegistrator(getFetchOptions().fetchRegistrator());
        }
    }

}
