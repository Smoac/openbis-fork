package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3ToV1.ProjectIdTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;

public class V3ProjectUpdatePredicate extends AbstractPredicate<ProjectUpdate>
{

    protected final ProjectIdPredicate projectPredicate;

    public V3ProjectUpdatePredicate()
    {
        this.projectPredicate = new ProjectIdPredicate();
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        projectPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "v3 project update object";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, ProjectUpdate value)
    {
        assert projectPredicate.initialized : "Predicate has not been initialized";
        return projectPredicate.doEvaluation(person, allowedRoles, ProjectIdTranslator.translate(value.getProjectId()));
    }
}
