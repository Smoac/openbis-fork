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

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.GroupIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

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
        final EntityTypePE entityType =
                getEntityTypeDAO(EntityKind.EXPERIMENT).tryToFindEntityTypeByCode(
                        experimentTypeCode);
        checkNotNull(experimentTypeCode, entityType);
        if (org.apache.commons.lang.StringUtils.isBlank(projectIdentifier.getGroupCode()))
        {
            final GroupPE group =
                    GroupIdentifierHelper.tryGetGroup(projectIdentifier, findRegistrator(), this);
            checkNotNull(projectIdentifier, group);
            projectIdentifier.setDatabaseInstanceCode(group.getDatabaseInstance().getCode());
            projectIdentifier.setGroupCode(group.getCode());
        }
        final ProjectPE project =
                getProjectDAO().tryFindProject(projectIdentifier.getDatabaseInstanceCode(),
                        projectIdentifier.getGroupCode(), projectIdentifier.getProjectCode());
        checkNotNull(projectIdentifier, project);
        experiments = getExperimentDAO().listExperiments((ExperimentTypePE) entityType, project);
    }

    private void checkNotNull(final ProjectIdentifier projectIdentifier, final ProjectPE project)
    {
        if (project == null)
        {
            throw new UserFailureException("Project '" + projectIdentifier + "' unknown.");
        }
    }

    private void checkNotNull(final ProjectIdentifier projectIdentifier, final GroupPE group)
    {
        if (group == null)
        {
            throw new UserFailureException("Unknown project '" + projectIdentifier + "'.");
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

    public final void save()
    {
        throw new UnsupportedOperationException("Experiment table can not be saved.");
    }

    public final Iterator<ExperimentPE> iterator()
    {
        return experiments == null ? Collections.<ExperimentPE> emptyList().iterator()
                : experiments.iterator();
    }

    public final void enrichWithProperties()
    {
        if (experiments != null)
        {
            for (final ExperimentPE experiment : experiments)
            {
                HibernateUtils.initialize(experiment.getProperties());
            }
        }
    }

    public final List<ExperimentPE> getExperiments()
    {
        assert experiments != null : "Experiments have not been loaded.";
        return experiments;
    }

    public void enrichWithLastDataSetDates()
    {
        if (experiments == null)
        {
            return;
        }

        for (final ExperimentPE e : experiments)
        {
            setLastDatasetDate(e);
        }

    }

    /**
     * Helper method setting the last data set date for given experiment.
     */
    @Private
    final void setLastDatasetDate(final ExperimentPE experiment)
    {
        final List<ProcedurePE> procedures = experiment.getProcedures();
        if (procedures.isEmpty())
        {
            return;
        }
        Date soFarNewestDataSetDateFound = null;
        for (final ProcedurePE procedure : procedures)
        {
            final Set<DataPE> dataPEs = procedure.getData();
            if (dataPEs.isEmpty())
            {
                continue;
            }
            for (final DataPE dataPE : dataPEs)
            {
                final Date registrationDate = dataPE.getRegistrationDate();
                if (isAttachedToValidSample(dataPE)
                        && newerDataSetDateFound(soFarNewestDataSetDateFound, registrationDate))
                {
                    soFarNewestDataSetDateFound = registrationDate;
                }
            }
        }
        experiment.setLastDataSetDate(soFarNewestDataSetDateFound);

    }

    @Private
    final boolean newerDataSetDateFound(final Date newestDataSetDateFound,
            final Date registrationDate)
    {
        return newestDataSetDateFound == null || newestDataSetDateFound.before(registrationDate);
    }

    /**
     * Helper method checking if given data set is attached to valid sample.
     */
    @Private
    final boolean isAttachedToValidSample(final DataPE data)
    {
        boolean attachedSampleIsValid = false;
        final SamplePE sampleAcquired = data.getSampleAcquiredFrom();
        final SamplePE sampleDerived = data.getSampleDerivedFrom();
        if (sampleAcquired != null && sampleAcquired.getInvalidation() == null
                || sampleDerived != null && sampleDerived.getInvalidation() == null)
        {
            attachedSampleIsValid = true;
        }
        return attachedSampleIsValid;
    }

}
