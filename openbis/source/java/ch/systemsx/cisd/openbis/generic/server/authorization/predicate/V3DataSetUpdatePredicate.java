package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3ToV1.DataSetIdTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;;

@ShouldFlattenCollections(value = false)
public class V3DataSetUpdatePredicate extends AbstractPredicate<List<DataSetUpdate>>
{

    protected final DataSetCodeCollectionPredicate datasetCodeCollectionPredicate;

    public V3DataSetUpdatePredicate()
    {
        this.datasetCodeCollectionPredicate = new DataSetCodeCollectionPredicate();
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
    	datasetCodeCollectionPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "v3 dataset update object";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, List<DataSetUpdate> values)
    {
        assert datasetCodeCollectionPredicate.initialized : "Predicate has not been initialized";
	    List<String> valuesAsCodes = new ArrayList<String>();
		for(DataSetUpdate value:values) 
		{
			valuesAsCodes.add(DataSetIdTranslator.translate(value.getDataSetId()));
		}
	    return datasetCodeCollectionPredicate.doEvaluation(person, allowedRoles, valuesAsCodes);
    }
}
