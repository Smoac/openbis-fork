package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.authorization.validator;

import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.SpaceValidator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.IValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.MsInjectionSample;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public final class RawDataSampleValidator implements IValidator<MsInjectionSample>
{
    private IValidator<Space> validator = new SpaceValidator();

    public boolean isValid(PersonPE person, MsInjectionSample sample)
    {
        Sample parent = sample.getSample().getGeneratedFrom();
        if (parent != null)
        {
            Space space = parent.getSpace();
            if (space == null || validator.isValid(person, space))
            {
                return true;
            }
        }
        return false;
    }
}