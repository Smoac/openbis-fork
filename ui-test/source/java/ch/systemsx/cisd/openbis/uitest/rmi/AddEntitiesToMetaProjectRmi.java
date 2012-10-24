/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.rmi;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignmentsIds;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.DataSetCodeId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.MetaprojectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleIdentifierId;
import ch.systemsx.cisd.openbis.uitest.dsl.Executor;
import ch.systemsx.cisd.openbis.uitest.request.AddEntitiesToMetaProject;
import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.Entity;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.Sample;

/**
 * @author anttil
 */
public class AddEntitiesToMetaProjectRmi extends Executor<AddEntitiesToMetaProject, Void>
{

    @Override
    public Void run(AddEntitiesToMetaProject request)
    {
        MetaprojectAssignmentsIds ids = new MetaprojectAssignmentsIds();

        for (Entity entity : request.getEntities())
        {
            if (entity instanceof Sample)
            {
                ids.addSample(new SampleIdentifierId(Identifiers.get((Sample) entity).toString()));
            } else if (entity instanceof Experiment)
            {
                ids.addExperiment(new ExperimentIdentifierId(Identifiers.get((Experiment) entity)
                        .toString()));
            } else if (entity instanceof DataSet)
            {
                ids.addDataSet(new DataSetCodeId(((DataSet) entity).getCode()));
            } else
            {
                throw new UnsupportedOperationException("not implemented yet for "
                        + entity.getClass());
            }
        }

        generalInformationChangingService.addToMetaproject(session, new MetaprojectIdentifierId(
                "/selenium/"
                        + request.getMetaProject().getName()), ids);
        return null;
    }
}
