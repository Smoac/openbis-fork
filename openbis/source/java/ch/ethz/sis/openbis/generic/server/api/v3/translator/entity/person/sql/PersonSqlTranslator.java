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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.sql;

import java.util.Collection;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.person.PersonFetchOptions;

/**
 * @author pkupczyk
 */
@Component
public class PersonSqlTranslator extends AbstractCachingTranslator<Long, Person, PersonFetchOptions> implements IPersonSqlTranslator
{

    @Override
    protected Person createObject(TranslationContext context, Long personId, PersonFetchOptions fetchOptions)
    {
        Person result = new Person();
        result.setFetchOptions(new PersonFetchOptions());
        return result;
    }

    @Override
    protected Relations getObjectsRelations(TranslationContext context, Collection<Long> personIds, PersonFetchOptions fetchOptions)
    {
        Relations relations = new Relations();

        relations.add(createRelation(PersonBaseRelation.class, personIds));

        if (fetchOptions.hasSpace())
        {
            relations.add(createRelation(PersonSpaceRelation.class, context, personIds, fetchOptions.withSpace()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long personId, Person result, Relations relations, PersonFetchOptions fetchOptions)
    {
        PersonBaseRelation baseRelation = relations.get(PersonBaseRelation.class);
        PersonBaseRecord baseRecord = baseRelation.getRecord(personId);

        result.setFirstName(baseRecord.firstName);
        result.setLastName(baseRecord.lastName);
        result.setUserId(baseRecord.userId);
        result.setEmail(baseRecord.email);
        result.setRegistrationDate(baseRecord.registrationDate);
        result.setActive(baseRecord.isActive);

        if (fetchOptions.hasSpace())
        {
            PersonSpaceRelation relation = relations.get(PersonSpaceRelation.class);
            result.setSpace(relation.getRelated(personId));
            result.getFetchOptions().withSpaceUsing(fetchOptions.withSpace());
        }
    }
}
