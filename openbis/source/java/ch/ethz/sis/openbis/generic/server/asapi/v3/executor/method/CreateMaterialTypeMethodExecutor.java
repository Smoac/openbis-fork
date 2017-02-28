package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialTypeCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.ICreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.ICreateMaterialTypeExecutor;

@Component
public class CreateMaterialTypeMethodExecutor extends AbstractCreateMethodExecutor<EntityTypePermId, MaterialTypeCreation>
        implements ICreateMaterialTypeMethodExecutor
{
    @Autowired
    private ICreateMaterialTypeExecutor createMaterialTypeExecutor;

    @Override
    protected ICreateEntityExecutor<MaterialTypeCreation, EntityTypePermId> getCreateExecutor()
    {
        return createMaterialTypeExecutor;
    }

}
