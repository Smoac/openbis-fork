package ch.systemsx.cisd.cifex.server;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.server.IRemoteHostProvider;

/**
 * A <code>IRemoteHostProvider</code> implementation which adapts an encapsulated <code>IRequestContextProvider</code>.
 * 
 * @author Christian Ribeaud
 */
public final class RequestContextProviderAdapter implements IRemoteHostProvider
{
    private final IRequestContextProvider requestContextProvider;

    public RequestContextProviderAdapter(final IRequestContextProvider requestContextProvider)
    {
        assert requestContextProvider != null : "Undefined IRequestContextProvider.";
        this.requestContextProvider = requestContextProvider;
    }

    //
    // IRemoteHostProvider
    //

    public final String getRemoteHost()
    {
        final HttpServletRequest request = requestContextProvider.getHttpServletRequest();
        if (request == null)
        {
            return UNKNOWN;
        }
        final String remoteHost = request.getRemoteHost();
        if (StringUtils.isEmpty(remoteHost))
        {
            return StringUtils.defaultIfEmpty(request.getRemoteAddr(), UNKNOWN);
        }
        return remoteHost;
    }
}