package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;

@ShouldFlattenCollections(value = false)
public class SamplePermIdCollectionPredicate extends AbstractSampleIdCollectionPredicate<String>
{

    public SamplePermIdCollectionPredicate(boolean isReadAccess)
    {
        super(isReadAccess);
    }

    public SamplePermIdCollectionPredicate()
    {
        super(true);
    }

    @Override
    protected Set<SampleAccessPE> getSampleAccessData(List<String> ids)
    {
        return authorizationDataProvider.getSampleCollectionAccessDataByPermId(ids);
    }

    @Override
    public final String getCandidateDescription()
    {
        return "sample perm ids";
    }

}
