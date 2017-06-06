package ch.systemsx.cisd.openbis.plugin.screening.server.authorization;

import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.authorization.project.IProjectAuthorization;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.ProjectAuthorizationBuilder;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromExperimentIdentifierString;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.role.RolesProviderFromPersonPE;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.user.UserProviderFromPersonPE;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.AbstractValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;

/**
 * Filters the experiments for spaces to which the user has permissions. This code works only in the case of one database instance.
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Bernd Rinn
 */
public final class ScreeningExperimentValidator extends AbstractValidator<ExperimentIdentifier>
{
    @Override
    public final boolean doValidation(final PersonPE person, final ExperimentIdentifier value)
    {
        final String spaceCode = value.getSpaceCode();
        final Set<RoleAssignmentPE> roleAssignments = person.getAllPersonRoles();

        for (final RoleAssignmentPE roleAssignment : roleAssignments)
        {
            if (roleAssignment.getSpace() == null && roleAssignment.getProject() == null)
            {
                // All roles on the db level allow full read access.
                // Note: Here we assume that we operate on _the only_ db instance (the home db)!
                return true;
            }
            final SpacePE group = roleAssignment.getSpace();
            if (group != null && group.getCode().equals(spaceCode))
            {
                return true;
            }
        }

        IProjectAuthorization<String> pa = new ProjectAuthorizationBuilder<String>()
                .withData(authorizationDataProvider)
                .withUser(new UserProviderFromPersonPE(person))
                .withRoles(new RolesProviderFromPersonPE(person))
                .withObjects(new ProjectProviderFromExperimentIdentifierString(value.getAugmentedCode()))
                .build();

        return pa.getObjectsWithoutAccess().isEmpty();
    }
}