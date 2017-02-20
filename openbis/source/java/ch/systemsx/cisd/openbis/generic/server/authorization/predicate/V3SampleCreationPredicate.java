package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

@ShouldFlattenCollections(value = false)
public class V3SampleCreationPredicate extends AbstractPredicate<List<SampleCreation>>
{

    private SampleOwnerIdentifierPredicate delegate;

    public V3SampleCreationPredicate()
    {
        delegate = new SampleOwnerIdentifierPredicate(false);
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        delegate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "v3 sample creation object";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, List<SampleCreation> value)
    {
        Set<SampleOwnerIdentifier> checked = new HashSet<>();

        for (SampleCreation spaceCreation : value)
        {
            ISpaceId spaceId = spaceCreation.getSpaceId();
            SampleOwnerIdentifier ownerIdentifier = new SampleOwnerIdentifier();
            if (spaceId instanceof SpacePermId)
            {
                String spaceCode = ((SpacePermId) spaceId).getPermId();
                ownerIdentifier = new SampleOwnerIdentifier(new SpaceIdentifier(spaceCode));
            }

            if (false == checked.contains(ownerIdentifier))
            {
                Status status = delegate.doEvaluation(person, allowedRoles, ownerIdentifier);
                if (status.isOK() == false)
                {
                    return status;
                }
                checked.add(ownerIdentifier);
            }

        }
        return Status.OK;
    }
}
