package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3ToV1.SpaceIdTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.SpaceCodeHelper;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;

@ShouldFlattenCollections(value = false)
public class V3SampleCreationPredicate extends AbstractPredicate<List<SampleCreation>>
{

    protected final SpaceCollectionPredicate spaceCollectionPredicate;

    public V3SampleCreationPredicate()
    {
        this.spaceCollectionPredicate = new SpaceCollectionPredicate();
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
    	spaceCollectionPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "v3 sample creation object";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, List<SampleCreation> value)
    {
        assert spaceCollectionPredicate.initialized : "Predicate has not been initialized";
    	Set<String> spaceCodes = new HashSet<String>();
    	for(SampleCreation spaceCreation:value) 
    	{
    		SpaceIdentifier spaceIdentifier = SpaceIdTranslator.translate(spaceCreation.getSpaceId());
    		final String spaceCode = SpaceCodeHelper.getSpaceCode(person, spaceIdentifier);
    		spaceCodes.add(spaceCode);
    	}
        return spaceCollectionPredicate.doEvaluation(person, allowedRoles, new ArrayList<String>(spaceCodes));
    }
}
