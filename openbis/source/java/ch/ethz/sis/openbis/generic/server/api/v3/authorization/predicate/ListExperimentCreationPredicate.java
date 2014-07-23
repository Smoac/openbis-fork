package ch.ethz.sis.openbis.generic.server.api.v3.authorization.predicate;

/*
 * Copyright 2014 ETH Zuerich, CISD
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

import java.util.List;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.project.TryGetProjectByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectPEPredicate;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author Jakub Straszewski
 * @author Franz-Josef Elmer
 */
@ShouldFlattenCollections(value = false)
public class ListExperimentCreationPredicate extends AbstractPredicate<List<ExperimentCreation>>
{
    private IAuthorizationDataProvider provider;

    private ProjectPEPredicate projectPredicate;

    public ListExperimentCreationPredicate()
    {
        projectPredicate = new ProjectPEPredicate();
    }

    @Override
    public void init(IAuthorizationDataProvider dataProvider)
    {
        this.provider = dataProvider;
        projectPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "experiment creation";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            List<ExperimentCreation> experimentCreations)
    {

        for (ExperimentCreation experimentCreation : experimentCreations)
        {
            IProjectId projectId = experimentCreation.getProjectId();
            if (projectId == null)
            {
                throw new UserFailureException(
                        "Unspecified project for experiment to registered with code '"
                                + experimentCreation.getCode() + "'.");
            }
            ProjectPE project =
                    new TryGetProjectByIdExecutor(provider.getDaoFactory().getProjectDAO()).tryGet(new OperationContext(null), projectId);
            Status status = projectPredicate.evaluate(person, allowedRoles, project);
            if (status.isError())
            {
                return status;
            }
        }
        return Status.OK;
    }

}
