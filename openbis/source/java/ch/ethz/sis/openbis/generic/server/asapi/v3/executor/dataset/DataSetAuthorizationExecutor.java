package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataPEPredicate;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

@Component
public class DataSetAuthorizationExecutor implements IDataSetAuthorizationExecutor
{

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_DATASET")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DATA_SET)
    public void canCreate(IOperationContext context, @AuthorizationGuard(guardClass = DataPEPredicate.class) DataPE dataSet)
    {
    }

}
