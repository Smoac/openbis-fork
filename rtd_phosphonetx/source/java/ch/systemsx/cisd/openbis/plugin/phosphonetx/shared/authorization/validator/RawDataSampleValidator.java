package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.authorization.validator;

import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.IValidator;
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
    private IValidator<Sample> validator = new ParentSampleValidator();

    public boolean isValid(PersonPE person, MsInjectionSample sample)
    {
        return validator.isValid(person, sample.getSample());
    }
}