package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3ToV1.SampleIdTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.ISampleId;

@ShouldFlattenCollections(value = false)
public class V3SampleUpdatePredicate extends AbstractPredicate<List<SampleUpdate>>
{

    protected final SampleIdPredicate sampleIdPredicate;

    public V3SampleUpdatePredicate()
    {
        this.sampleIdPredicate = new SampleIdPredicate();
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        sampleIdPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "v3 sample update object";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, List<SampleUpdate> value)
    {
        assert sampleIdPredicate.initialized : "Predicate has not been initialized";
    	for(SampleUpdate sampleUpdate:value) {
    		ISampleId sampleId = SampleIdTranslator.translate(sampleUpdate.getSampleId());
    		Status result = sampleIdPredicate.doEvaluation(person, allowedRoles, sampleId);
            if (result != Status.OK)
            {
                return result;
            }
    	}
        return Status.OK;
    }
}
