package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RelatedDataSetCriteria;

/**
 * A {@link IOriginalDataProvider} implementation for data sets related to other entities.
 * 
 * @author Piotr Buczek
 */
final class ListRelatedDataSetOriginalDataProvider extends
        AbstractOriginalDataProvider<ExternalData>
{

    private final RelatedDataSetCriteria criteria;

    ListRelatedDataSetOriginalDataProvider(final ICommonServer commonServer,
            final String sessionToken, final RelatedDataSetCriteria criteria)
    {
        super(commonServer, sessionToken);
        this.criteria = criteria;
    }

    //
    // AbstractOriginalDataProvider
    //

    public final List<ExternalData> getOriginalData()
    {
        final List<ExternalData> hits = commonServer.listRelatedDataSets(sessionToken, criteria);
        return hits;
    }
}