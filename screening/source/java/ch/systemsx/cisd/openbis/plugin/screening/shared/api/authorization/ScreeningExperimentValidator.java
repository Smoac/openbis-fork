package ch.systemsx.cisd.openbis.plugin.screening.shared.api.authorization;

import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.AbstractValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;

/**
 * Filters the experiments for spaces to which the user has permissions. This code works only in the
 * case of one database instance.
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
            if (roleAssignment.getDatabaseInstance() != null)
            {
                // All roles on the db level allow full read access.
                // Note: Here we assume that we operate on _the only_ db instance (the home db)!
                return true;
            }
            final GroupPE group = roleAssignment.getGroup();
            if (group != null && group.getCode().equals(spaceCode))
            {
                return true;
            }
        }
        return false;
    }
}