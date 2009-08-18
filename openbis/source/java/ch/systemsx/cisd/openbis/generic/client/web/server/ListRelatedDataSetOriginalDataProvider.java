package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ExternalDataTranslator;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RelatedDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;

/**
 * A {@link IOriginalDataProvider} implementation for data sets related to other entities.
 * 
 * @author Piotr Buczek
 */
final class ListRelatedDataSetOriginalDataProvider extends
        AbstractOriginalDataProvider<ExternalData>
{

    private final RelatedDataSetCriteria criteria;

    private final String dataStoreBaseURL;

    private final String baseIndexURL;

    ListRelatedDataSetOriginalDataProvider(final ICommonServer commonServer,
            final String sessionToken, final RelatedDataSetCriteria criteria,
            String dataStoreBaseURL, String baseIndexURL)
    {
        super(commonServer, sessionToken);
        this.criteria = criteria;
        this.dataStoreBaseURL = dataStoreBaseURL;
        this.baseIndexURL = baseIndexURL;
    }

    //
    // AbstractOriginalDataProvider
    //

    public final List<ExternalData> getOriginalData()
    {
        final List<ExternalDataPE> hits = commonServer.listRelatedDataSets(sessionToken, criteria);
        final List<ExternalData> list = new ArrayList<ExternalData>(hits.size());
        for (final ExternalDataPE hit : hits)
        {
            list.add(ExternalDataTranslator.translate(hit, dataStoreBaseURL, baseIndexURL, false));
        }
        return list;
    }
}