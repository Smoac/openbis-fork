package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.authorization.validator;

import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.SpaceValidator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.IValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public final class RawDataSampleValidator implements IValidator<Sample>
{
    private IValidator<Space> validator = new SpaceValidator();

    public boolean isValid(PersonPE person, Sample sample)
    {
        Sample parent = sample.getGeneratedFrom();
        if (parent != null)
        {
            Space group = parent.getSpace();
            if (group == null || validator.isValid(person, group))
            {
                return true;
            }
        }
        return false;
    }
}