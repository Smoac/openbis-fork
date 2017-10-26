/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.authorizationgroup;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.authorizationgroup.AuthorizationGroupBaseRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.authorizationgroup.IAuthorizationGroupBaseTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.authorizationgroup.IAuthorizationGroupRegistratorTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.authorizationgroup.IAuthorizationGroupUserTranslator;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class GroupTranslator extends AbstractCachingTranslator<Long, AuthorizationGroup, AuthorizationGroupFetchOptions>  implements IGroupTranslator
{
    @Autowired
    private IAuthorizationGroupBaseTranslator baseTranslator;
    
    @Autowired
    private IAuthorizationGroupRegistratorTranslator registratorTranslator;
    
    @Autowired
    private IAuthorizationGroupUserTranslator userTranslator;

    @Override
    protected AuthorizationGroup createObject(TranslationContext context, Long input, AuthorizationGroupFetchOptions fetchOptions)
    {
        AuthorizationGroup authorizationGroup = new AuthorizationGroup();
        authorizationGroup.setFetchOptions(fetchOptions);
        return authorizationGroup;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> ids, AuthorizationGroupFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();
        relations.put(IAuthorizationGroupBaseTranslator.class, baseTranslator.translate(context, ids, null));
        
        if (fetchOptions.hasRegistrator())
        {
            relations.put(IAuthorizationGroupRegistratorTranslator.class, registratorTranslator.translate(context, ids, fetchOptions.withRegistrator()));
        }
        if (fetchOptions.hasUsers())
        {
            relations.put(IAuthorizationGroupUserTranslator.class, userTranslator.translate(context, ids, fetchOptions.withUsers()));
        }
        
        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long id, AuthorizationGroup group, Object objectRelations,
            AuthorizationGroupFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        AuthorizationGroupBaseRecord baseRecord = relations.get(IAuthorizationGroupBaseTranslator.class, id);
        
        group.setCode(baseRecord.code);
        group.setDescription(baseRecord.description);
        group.setRegistrationDate(baseRecord.registrationDate);
        group.setModificationDate(baseRecord.modificationDate);
        
        if (fetchOptions.hasRegistrator())
        {
            group.setRegistrator(relations.get(IAuthorizationGroupRegistratorTranslator.class, id));
            group.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }
        if (fetchOptions.hasUsers())
        {
            group.setUsers((List<Person>) relations.get(IAuthorizationGroupUserTranslator.class, id));
            group.getFetchOptions().withUsersUsing(fetchOptions.withUsers());
        }
    }

}
