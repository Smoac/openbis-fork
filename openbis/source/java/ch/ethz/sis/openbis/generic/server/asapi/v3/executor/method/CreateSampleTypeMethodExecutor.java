package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.ICreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.ICreateSampleTypeExecutor;

@Component
public class CreateSampleTypeMethodExecutor extends AbstractCreateMethodExecutor<EntityTypePermId, SampleTypeCreation>
        implements ICreateSampleTypeMethodExecutor
{
    @Autowired
    private ICreateSampleTypeExecutor createSampleTypeExecutor;

    @Override
    protected ICreateEntityExecutor<SampleTypeCreation, EntityTypePermId> getCreateExecutor()
    {
        return createSampleTypeExecutor;
    }

}
