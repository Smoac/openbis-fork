/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.metadata.IUpdateMetaDataForEntityExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentTypeUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityTypeExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.IUpdateEntityTypePropertyTypesExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class UpdateExperimentTypeExecutor
        extends AbstractUpdateEntityTypeExecutor<ExperimentTypeUpdate, ExperimentTypePE>
        implements IUpdateExperimentTypeExecutor
{
    @Autowired
    private IExperimentTypeAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IUpdateExperimentTypePropertyTypesExecutor updateExperimentTypePropertyTypesExecutor;

    @Autowired
    private IUpdateMetaDataForEntityExecutor<ExperimentTypeUpdate, ExperimentTypePE>
            updateMetaDataForEntityExecutor;

    @Override
    protected EntityKind getDAOEntityKind()
    {
        return EntityKind.EXPERIMENT;
    }

    @Override
    protected void checkTypeSpecificFields(ExperimentTypeUpdate update)
    {
    }

    @Override
    protected void updateSpecific(ExperimentTypePE type, ExperimentTypeUpdate update)
    {
        updateMetaDataForEntityExecutor.updateSpecific(update, type);
    }

    @Override
    protected IUpdateEntityTypePropertyTypesExecutor<ExperimentTypeUpdate, ExperimentTypePE> getUpdateEntityTypePropertyTypeExecutor()
    {
        return updateExperimentTypePropertyTypesExecutor;
    }

    @Override
    protected void checkAccessTypeSpecific(IOperationContext context, IEntityTypeId id, ExperimentTypePE entity, ExperimentTypeUpdate update)
    {
        authorizationExecutor.canUpdate(context, entity, update);
    }

}
