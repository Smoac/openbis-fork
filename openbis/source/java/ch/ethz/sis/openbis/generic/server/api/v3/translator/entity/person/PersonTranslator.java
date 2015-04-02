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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.space.ISpaceTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.person.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.person.PersonPermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
@Component
public class PersonTranslator extends AbstractCachingTranslator<PersonPE, Person, PersonFetchOptions> implements IPersonTranslator
{

    @Autowired
    private ISpaceTranslator spaceTranslator;

    @Override
    protected Person createObject(TranslationContext context, PersonPE person, PersonFetchOptions fetchOptions)
    {
        Person result = new Person();

        result.setPermId(new PersonPermId(person.getUserId()));
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
    protected void updateObject(TranslationContext context, PersonPE person, Person result, Relations relations, PersonFetchOptions fetchOptions)
    {
        if (fetchOptions.hasSpace())
        {
            result.setSpace(spaceTranslator.translate(context, person.getHomeSpace(), fetchOptions.withSpace()));
            result.getFetchOptions().withSpaceUsing(fetchOptions.withSpace());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(translate(context, person.getRegistrator(), fetchOptions.withRegistrator()));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }
    }
}
