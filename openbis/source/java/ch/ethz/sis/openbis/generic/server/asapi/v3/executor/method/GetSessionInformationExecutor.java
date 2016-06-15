package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

@Component
public class GetSessionInformationExecutor extends AbstractMethodExecutor implements IGetSessionInformationExecutor
{

    @Override
    public SessionInformation getSessionInformation(final String sessionToken)
    {
        return executeInContext(sessionToken, new IMethodAction<SessionInformation>()
            {

                @Override
                public SessionInformation execute(IOperationContext context)
                {
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

            });
    }

}
