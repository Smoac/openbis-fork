package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.ICreateDataSetTypeExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.ICreateEntityExecutor;

@Component
public class CreateDataSetTypeMethodExecutor extends AbstractCreateMethodExecutor<EntityTypePermId, DataSetTypeCreation>
        implements ICreateDataSetTypeMethodExecutor
{
    @Autowired
    private ICreateDataSetTypeExecutor createDataSetTypeExecutor;

    @Override
    protected ICreateEntityExecutor<DataSetTypeCreation, EntityTypePermId> getCreateExecutor()
    {
        return createDataSetTypeExecutor;
    }

}
