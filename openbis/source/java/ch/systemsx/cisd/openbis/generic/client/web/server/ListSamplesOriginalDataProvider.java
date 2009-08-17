package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;

/**
 * A {@link IOriginalDataProvider} implementation for listing samples.
 * 
 * @author Christian Ribeaud
 */
final class ListSamplesOriginalDataProvider extends AbstractOriginalDataProvider<Sample>
{

    private final ListSampleCriteria listCriteria;

    ListSamplesOriginalDataProvider(final ICommonServer commonServer, final String sessionToken,
            final ListSampleCriteria listCriteria)
    {
        super(commonServer, sessionToken);
        this.listCriteria = listCriteria;
    }

    //
    // AbstractOriginalDataProvider
    //

    public final List<Sample> getOriginalData()
    {
        return commonServer.listSamples(sessionToken, listCriteria);
    }

}