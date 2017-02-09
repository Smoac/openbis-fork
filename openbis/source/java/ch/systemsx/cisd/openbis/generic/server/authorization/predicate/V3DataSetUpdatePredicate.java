package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3ToV1.DataSetIdTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;;

public class V3DataSetUpdatePredicate extends AbstractPredicate<DataSetUpdate>
{

    protected final DataSetCodePredicate datasetCodePredicate;

    public V3DataSetUpdatePredicate()
    {
        this.datasetCodePredicate = new DataSetCodePredicate();
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        datasetCodePredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "v3 dataset update object";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, DataSetUpdate value)
    {
        assert datasetCodePredicate.initialized : "Predicate has not been initialized";
        return datasetCodePredicate.doEvaluation(person, allowedRoles, DataSetIdTranslator.translate(value.getDataSetId()));
    }
}
