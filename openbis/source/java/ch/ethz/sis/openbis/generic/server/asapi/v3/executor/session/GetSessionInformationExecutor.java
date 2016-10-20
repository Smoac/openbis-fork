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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
@Component
public class GetSessionInformationExecutor implements IGetSessionInformationExecutor
{

    @Autowired
    private ISessionAuthorizationExecutor authorizationExecutor;

    @Override
    public SessionInformation getInformation(IOperationContext context)
    {
        authorizationExecutor.canGet(context);

        IAuthSession session = null;

        try
        {
            session = context.getSession();
        } catch (Exception ex)
        {
            // Ignore, if session is no longer available and error is thrown
        }

        SessionInformation sessionInfo = null;
        if (session != null)
        {
            sessionInfo = new SessionInformation();
            sessionInfo.setUserName(session.getUserName());
            sessionInfo.setHomeGroupCode(session.tryGetHomeGroupCode());

            PersonPE personPE = session.tryGetPerson();
            Person person = new Person();
            person.setFirstName(personPE.getFirstName());
            person.setLastName(personPE.getLastName());
            person.setUserId(personPE.getUserId());
            person.setEmail(personPE.getEmail());
            person.setRegistrationDate(personPE.getRegistrationDate());
            person.setActive(personPE.isActive());
            sessionInfo.setPerson(person);

            PersonPE creatorPersonPE = session.tryGetCreatorPerson();
            Person creatorPerson = new Person();
            creatorPerson.setFirstName(creatorPersonPE.getFirstName());
            creatorPerson.setLastName(creatorPersonPE.getLastName());
            creatorPerson.setUserId(creatorPersonPE.getUserId());
            creatorPerson.setEmail(creatorPersonPE.getEmail());
            creatorPerson.setRegistrationDate(creatorPersonPE.getRegistrationDate());
            creatorPerson.setActive(creatorPersonPE.isActive());
            sessionInfo.setCreatorPerson(creatorPerson);
        }

        return sessionInfo;
    }

}
