package ch.systemsx.cisd.openbis.plugin.screening.shared.api.authorization;

import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.AbstractValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;

/**
 * Filters only samples from the spaces to which the user has rights. This code works only in the
 * case of one database instance.
 * 
 * @author Tomasz Pylak
 */
public final class ScreeningPlateValidator extends AbstractValidator<Plate>
{
    @Override
    public final boolean doValidation(final PersonPE person, final Plate value)
    {
        final String spaceCode = value.tryGetSpaceCode();
        if (spaceCode != null)
        {
            final Set<RoleAssignmentPE> roleAssignments = person.getAllPersonRoles();
            for (final RoleAssignmentPE roleAssignment : roleAssignments)
            {
                if (roleAssignment.getDatabaseInstance() != null)
                {
                    // all roles on db level allow full read access (we assume that we operate on
                    // home db always)
                    return true;
                }
                final GroupPE group = roleAssignment.getGroup();
                if (group != null && group.getCode().equals(spaceCode))
                {
                    return true;
                }
            }
            return false;
        } else
        {
            // all shared samples are accessible
            return true;
        }
    }
}