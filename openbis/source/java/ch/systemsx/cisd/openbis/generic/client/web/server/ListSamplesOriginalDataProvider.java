package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * A {@link IOriginalDataProvider} implementation for listing (browsing or searching) samples.
 * 
 * @author Christian Ribeaud
 * @author Piotr Buczek
 */
final class ListSamplesOriginalDataProvider extends AbstractOriginalDataProvider<Sample>
{

    private final ListSampleDisplayCriteria criteria;

    ListSamplesOriginalDataProvider(final ICommonServer commonServer, final String sessionToken,
            final ListSampleDisplayCriteria criteria)
    {
        super(commonServer, sessionToken);
        this.criteria = criteria;
    }

    //
    // AbstractOriginalDataProvider
    //

    public final List<Sample> getOriginalData()
    {
        switch (criteria.getCriteriaKind())
        {
            case BROWSE:
                return commonServer.listSamples(sessionToken, criteria.getBrowseCriteria());
            case SEARCH:
                return commonServer.searchForSamples(sessionToken, criteria.getSearchCriteria());
        }
        return null; // not possible
    }

}