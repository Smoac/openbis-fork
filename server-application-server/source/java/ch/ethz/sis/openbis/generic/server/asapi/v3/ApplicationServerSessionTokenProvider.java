package ch.ethz.sis.openbis.generic.server.asapi.v3;

import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.roleassignment.RoleAssignmentUtils;
import ch.ethz.sis.transaction.ISessionTokenProvider;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

public class ApplicationServerSessionTokenProvider implements ISessionTokenProvider
{

    private final IOpenBisSessionManager sessionManager;

    public ApplicationServerSessionTokenProvider(final IOpenBisSessionManager sessionManager)
    {
        this.sessionManager = sessionManager;
    }

    @Override public boolean isValid(final String sessionToken)
    {
        return sessionManager.isSessionActive(sessionToken);
    }

    @Override public boolean isInstanceAdminOrSystem(final String sessionToken)
    {
        Session session = sessionManager.tryGetSession(sessionToken);

        if (session == null)
        {
            return false;
        }

        PersonPE person = session.tryGetPerson();

        if (person == null)
        {
            return false;
        }

        return person.isSystemUser() || RoleAssignmentUtils.isInstanceAdmin(person);
    }
}
