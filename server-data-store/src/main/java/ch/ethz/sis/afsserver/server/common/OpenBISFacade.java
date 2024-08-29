package ch.ethz.sis.afsserver.server.common;

import java.util.List;

import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.Event;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.fetchoptions.EventFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventSearchCriteria;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;

public class OpenBISFacade
{

    private final String openBISUrl;

    private final String openBISUser;

    private final String openBISPassword;

    private final Integer openBISTimeout;

    private volatile String sessionToken;

    public OpenBISFacade(String openBISUrl, String openBISUser, String openBISPassword, Integer openBISTimeout)
    {
        this.openBISUrl = openBISUrl;
        this.openBISUser = openBISUser;
        this.openBISPassword = openBISPassword;
        this.openBISTimeout = openBISTimeout;
    }

    public SearchResult<Event> searchEvents(EventSearchCriteria criteria, EventFetchOptions fetchOptions)
    {
        return executeOperation(openBIS -> openBIS.searchEvents(criteria, fetchOptions));
    }

    public SearchResult<DataSet> searchDataSets(DataSetSearchCriteria criteria, DataSetFetchOptions fetchOptions)
    {
        return executeOperation(openBIS -> openBIS.searchDataSets(criteria, fetchOptions));
    }

    public void updateDataSets(final List<DataSetUpdate> updates)
    {
        executeOperation(openBIS ->
        {
            openBIS.updateDataSets(updates);
            return null;
        });
    }

    private <T> T executeOperation(Operation<T> operation)
    {
        OpenBIS openBIS = new OpenBIS(openBISUrl, openBISTimeout);
        setSessionToken(openBIS);

        try
        {
            return operation.execute(openBIS);
        } catch (InvalidSessionException e)
        {
            setSessionToken(openBIS);
            return operation.execute(openBIS);
        }
    }

    private interface Operation<T>
    {
        T execute(OpenBIS openBIS);
    }

    private void setSessionToken(OpenBIS openBIS)
    {
        if (sessionToken != null)
        {
            openBIS.setSessionToken(sessionToken);
        } else
        {
            synchronized (this)
            {
                if (sessionToken != null)
                {
                    openBIS.setSessionToken(sessionToken);
                } else
                {
                    sessionToken = openBIS.login(openBISUser, openBISPassword);

                    if (sessionToken != null)
                    {
                        openBIS.setSessionToken(sessionToken);
                    } else
                    {
                        throw new RuntimeException(
                                "Could not login to the AS server. Please check openBIS user and openBIS password in the AFS server configuration.");
                    }
                }
            }
        }
    }

    public String getSessionToken()
    {
        return sessionToken;
    }

}