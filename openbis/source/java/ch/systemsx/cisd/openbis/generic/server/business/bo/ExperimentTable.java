/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * The only productive implementation of {@link IExperimentTable}.
 * 
 * @author Izabela Adamczyk
 */
public final class ExperimentTable extends AbstractBusinessObject implements IExperimentTable
{
    private List<ExperimentPE> experiments;

    public ExperimentTable(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    //
    // IExperimentTable
    //

    public final void load(final String experimentTypeCode,
            final ProjectIdentifier projectIdentifier)
    {
        checkNotNull(experimentTypeCode, projectIdentifier);
        fillGroupIdentifier(projectIdentifier);
        final ProjectPE project =
                getProjectDAO().tryFindProject(projectIdentifier.getDatabaseInstanceCode(),
                        projectIdentifier.getSpaceCode(), projectIdentifier.getProjectCode());
        checkNotNull(projectIdentifier, project);
        if (EntityType.isAllTypesCode(experimentTypeCode))
        {
            experiments = getExperimentDAO().listExperimentsWithProperties(project);
        } else
        {
            final EntityTypePE entityType =
                    getEntityTypeDAO(EntityKind.EXPERIMENT).tryToFindEntityTypeByCode(
                            experimentTypeCode);
            checkNotNull(experimentTypeCode, entityType);
            experiments =
                    getExperimentDAO().listExperimentsWithProperties((ExperimentTypePE) entityType,
                            project);
        }
    }

    private void checkNotNull(final ProjectIdentifier projectIdentifier, final ProjectPE project)
    {
        if (project == null)
        {
            throw new UserFailureException("Project '" + projectIdentifier + "' unknown.");
        }
    }

    private void checkNotNull(final String experimentTypeCode, final EntityTypePE entityType)
    {
        if (entityType == null)
        {
            throw new UserFailureException("Unknown experiment type '" + experimentTypeCode + "'.");
        }
    }

    private void checkNotNull(final String experimentTypeCode,
            final ProjectIdentifier projectIdentifier)
    {
        if (experimentTypeCode == null)
        {
            throw new UserFailureException("Experiment type not specified.");
        }
        if (projectIdentifier == null)
        {
            throw new UserFailureException("Project not specified.");
        }
    }

    public final List<ExperimentPE> getExperiments()
    {
        assert experiments != null : "Experiments have not been loaded.";
        return experiments;
    }

}
