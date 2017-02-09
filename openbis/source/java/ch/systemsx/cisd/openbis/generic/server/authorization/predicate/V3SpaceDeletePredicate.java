package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3ToV1.SpaceIdTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;

public class V3SpaceDeletePredicate extends AbstractPredicate<ISpaceId> {

    protected final SpaceIdentifierPredicate spacePredicate;
    
    public V3SpaceDeletePredicate()
    {
        this.spacePredicate = new SpaceIdentifierPredicate();
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        spacePredicate.init(provider);
    }
    
	@Override
	public String getCandidateDescription() {
		return "v3 space id object";
	}

	@Override
	protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, ISpaceId value) {
		assert spacePredicate.initialized : "Predicate has not been initialized";
		return spacePredicate.doEvaluation(person, allowedRoles, SpaceIdTranslator.translate(value));
	}
}
