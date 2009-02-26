package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetSearchHit;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.DataSetSearchHitTranslator;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetSearchHitDTO;

/**
 * A {@link IOriginalDataProvider} implementation for search data sets.
 * 
 * @author Izbaela Adamczyk
 */
final class ListDataSetsOriginalDataProvider extends AbstractOriginalDataProvider<DataSetSearchHit>
{

    private final DataSetSearchCriteria criteria;
    private final String dataStoreBaseURL;

    ListDataSetsOriginalDataProvider(final ICommonServer commonServer, final String sessionToken,
            final DataSetSearchCriteria criteria, String dataStoreBaseURL)
    {
        super(commonServer, sessionToken);
        this.criteria = criteria;
        this.dataStoreBaseURL = dataStoreBaseURL;
    }

    //
    // AbstractOriginalDataProvider
    //

    public final List<DataSetSearchHit> getOriginalData()
    {
        final List<DataSetSearchHitDTO> hits =
                commonServer.searchForDataSets(sessionToken, criteria);
        final List<DataSetSearchHit> list = new ArrayList<DataSetSearchHit>(hits.size());
        for (final DataSetSearchHitDTO hit : hits)
        {
            list.add(DataSetSearchHitTranslator.translate(hit, dataStoreBaseURL));
        }
        return list;
    }
}