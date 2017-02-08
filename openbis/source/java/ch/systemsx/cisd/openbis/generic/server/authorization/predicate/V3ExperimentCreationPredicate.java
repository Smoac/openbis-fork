package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3ToV1.ProjectIdTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;

public class V3ExperimentCreationPredicate extends AbstractPredicate<ExperimentCreation> {

    protected final ProjectIdPredicate projectIdPredicate;
    
    public V3ExperimentCreationPredicate()
    {
        this.projectIdPredicate = new ProjectIdPredicate();
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
    	projectIdPredicate.init(provider);
    }
    
	@Override
	public String getCandidateDescription() {
		return "experiment creation object";
	}

	@Override
	protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, ExperimentCreation value) {
		return projectIdPredicate.doEvaluation(person, allowedRoles, ProjectIdTranslator.translate(value.getProjectId()));
	}
}
