package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3ToV1.ExperimentIdTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;

public class V3ExperimentUpdatePredicate extends AbstractPredicate<ExperimentUpdate> {

    protected final ExperimentIdPredicate experimentPredicate;
    
    public V3ExperimentUpdatePredicate()
    {
        this.experimentPredicate = new ExperimentIdPredicate();
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
    	experimentPredicate.init(provider);
    }
    
	@Override
	public String getCandidateDescription() {
		return "v3 experiment update object";
	}

	@Override
	protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, ExperimentUpdate value) {
		assert experimentPredicate.initialized : "Predicate has not been initialized";
		return experimentPredicate.doEvaluation(person, allowedRoles, ExperimentIdTranslator.translate(value.getExperimentId()));
	}
}
