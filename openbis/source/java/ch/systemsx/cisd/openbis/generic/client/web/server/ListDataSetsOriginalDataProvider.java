package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ExternalDataTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ExperimentTranslator.LoadableFields;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;

/**
 * A {@link IOriginalDataProvider} implementation for search data sets.
 * 
 * @author Izbaela Adamczyk
 */
final class ListDataSetsOriginalDataProvider extends AbstractOriginalDataProvider<ExternalData>
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

    public final List<ExternalData> getOriginalData()
    {
        final List<ExternalDataPE> hits =
                commonServer.searchForDataSets(sessionToken, criteria);
        final List<ExternalData> list = new ArrayList<ExternalData>(hits.size());
        for (final ExternalDataPE hit : hits)
        {
            list.add(ExternalDataTranslator.translate(hit, dataStoreBaseURL,true,
                    LoadableFields.PROPERTIES));
        }
        return list;
    }
}