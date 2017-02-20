package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

abstract class AbstractSampleIdCollectionPredicate<T> extends
        DelegatedPredicate<List<SampleOwnerIdentifier>, List<T>>
{
    AbstractSampleIdCollectionPredicate(boolean isReadAccess)
    {
        super(new SampleOwnerIdentifierCollectionPredicate(isReadAccess));
    }

    @Override
    public List<SampleOwnerIdentifier> tryConvert(List<T> ids)
    {
        ArrayList<SampleOwnerIdentifier> ownerIds = new ArrayList<SampleOwnerIdentifier>();

        Set<SampleAccessPE> accessData = getSampleAccessData(ids);

        for (SampleAccessPE accessDatum : accessData)
        {
            String ownerCode = accessDatum.getOwnerCode();
            switch (accessDatum.getOwnerType())
            {
                case SPACE:
                    ownerIds.add(new SampleOwnerIdentifier(new SpaceIdentifier(ownerCode)));
                    break;
                case DATABASE_INSTANCE:
                    ownerIds.add(new SampleOwnerIdentifier());
                    break;
            }
        }

        return ownerIds;
    }
    
    protected abstract Set<SampleAccessPE> getSampleAccessData(List<T> ids);
}