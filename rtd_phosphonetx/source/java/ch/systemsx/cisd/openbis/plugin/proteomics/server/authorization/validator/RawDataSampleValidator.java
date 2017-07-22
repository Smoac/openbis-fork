package ch.systemsx.cisd.openbis.plugin.proteomics.server.authorization.validator;

import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.IValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto.MsInjectionSample;

/**
 * @author Franz-Josef Elmer
 */
public final class RawDataSampleValidator implements IValidator<MsInjectionSample>
{
    private IValidator<Sample> validator = new ParentSampleValidator();

    @Override
    public boolean isValid(PersonPE person, MsInjectionSample sample)
    {
        return validator.isValid(person, sample.getSample());
    }

    @Override
    public void init(IAuthorizationDataProvider authorizationDataProvider)
    {
        validator.init(authorizationDataProvider);
    }
}