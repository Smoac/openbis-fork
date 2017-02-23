package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.IObjectAuthorizationExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

public interface IDataSetAuthorizationExecutor extends IObjectAuthorizationExecutor
{
    void canCreate(IOperationContext context, DataPE dataSet);

}
