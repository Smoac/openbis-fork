package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.ICreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.ICreateExperimentTypeExecutor;

@Component
public class CreateExperimentTypeMethodExecutor extends AbstractCreateMethodExecutor<EntityTypePermId, ExperimentTypeCreation>
        implements ICreateExperimentTypeMethodExecutor
{
    @Autowired
    private ICreateExperimentTypeExecutor createExperimentTypeExecutor;

    @Override
    protected ICreateEntityExecutor<ExperimentTypeCreation, EntityTypePermId> getCreateExecutor()
    {
        return createExperimentTypeExecutor;
    }

}
