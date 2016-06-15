package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;

public interface IGetSessionInformationExecutor
{
    public SessionInformation getSessionInformation(final String sessionToken);
}
