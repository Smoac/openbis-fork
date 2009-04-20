package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ListSampleCriteriaTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * A {@link IOriginalDataProvider} implementation for listing samples.
 * 
 * @author Christian Ribeaud
 */
final class ListSamplesOriginalDataProvider extends AbstractOriginalDataProvider<Sample>
{

    private final ListSampleCriteria listCriteria;

    private final boolean withExperimentAndProperties;

    ListSamplesOriginalDataProvider(final ICommonServer commonServer, final String sessionToken,
            final ListSampleCriteria listCriteria, boolean withExperimentAndProperties)
    {
        super(commonServer, sessionToken);
        this.listCriteria = listCriteria;
        this.withExperimentAndProperties = withExperimentAndProperties;
    }

    //
    // AbstractOriginalDataProvider
    //

    public final List<Sample> getOriginalData()
    {
        final List<SamplePE> samples =
                commonServer.listSamples(sessionToken, ListSampleCriteriaTranslator
                        .translate(listCriteria), withExperimentAndProperties);
        final List<Sample> list = new ArrayList<Sample>(samples.size());
        for (final SamplePE sample : samples)
        {
            list.add(SampleTranslator.translate(sample));
        }
        return list;
    }
}