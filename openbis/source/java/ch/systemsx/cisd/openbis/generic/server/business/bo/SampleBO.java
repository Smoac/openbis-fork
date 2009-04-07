/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * The unique {@link ISampleBO} implementation.
 * 
 * @author Christian Ribeaud
 */
public final class SampleBO extends AbstractSampleBusinessObject implements ISampleBO
{
    private SamplePE sample;

    private boolean dataChanged;

    public SampleBO(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    SampleBO(final IDAOFactory daoFactory, final Session session,
            final IEntityPropertiesConverter entityPropertiesConverter)
    {
        super(daoFactory, session, entityPropertiesConverter);
    }

    //
    // ISampleBO
    //

    public SamplePE tryToGetSample()
    {
        return sample;
    }

    public void tryToLoadBySampleIdentifier(SampleIdentifier identifier)
    {
        assert identifier != null : "Unspecified identifier.";

        sample = tryToGetSampleByIdentifier(identifier);
    }

    public final SamplePE getSample() throws IllegalStateException
    {
        if (sample == null)
        {
            throw new IllegalStateException("Unloaded sample.");
        }
        return sample;
    }

    public final void loadBySampleIdentifier(final SampleIdentifier identifier)
            throws UserFailureException
    {
        tryToLoadBySampleIdentifier(identifier);
        if (sample == null)
        {
            throw UserFailureException.fromTemplate(
                    "No sample could be found with given identifier '%s'.", identifier);
        }
    }

    public final void define(final NewSample newSample)
    {
        assert newSample != null : "Unspecified new sample.";

        sample = createSample(newSample);
        dataChanged = true;
    }

    public final void save()
    {
        assert sample != null : "Sample not loaded.";
        assert dataChanged : "Data have not been changed.";

        try
        {
            getSampleDAO().createSample(sample);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Sample '%s'", sample.getSampleIdentifier()));
        }
        dataChanged = false;
    }

    public boolean hasDatasets()
    {
        assert sample != null;

        final IExternalDataDAO externalDataDAO = getExternalDataDAO();
        long count = 0;
        for (final SourceType dataSourceType : SourceType.values())
        {
            final List<ExternalDataPE> list =
                    externalDataDAO.listExternalData(sample, dataSourceType);
            count += list.size();
        }
        return count > 0;
    }

    public void setExperiment(ExperimentPE experiment)
    {
        assert sample != null : "Sample not loaded.";

        checkValid(sample);
        checkSampleInGroup(sample);
        checkSampleUnused(sample);
        checkSampleWithoutDatasets();
        sample.setExperiment(experiment);
        getSampleDAO().updateSample(sample);
        dataChanged = false;
    }

    static private void checkValid(SamplePE sample)
    {
        if (sample.getInvalidation() != null)
        {
            throw UserFailureException.fromTemplate("Given sample '%s' is invalid.", sample
                    .getSampleIdentifier());
        }
    }

    private final void checkSampleWithoutDatasets()
    {
        if (hasDatasets())
        {
            throw UserFailureException
                    .fromTemplate(
                            "Cannot use sample '%s' in the experiment, because in the previous "
                                    + "invalidated experiment there were some data acquired for this sample.",
                            getSample().getSampleIdentifier());
        }
    }

    private final static void checkSampleUnused(final SamplePE sample)
    {
        ExperimentPE experiment = sample.getExperiment();
        if (experiment != null && experiment.getInvalidation() == null)
        {
            throw UserFailureException.fromTemplate(
                    "Given sample code '%s' already registered for experiment '%s'.", sample
                            .getSampleIdentifier(), IdentifierHelper
                            .createExperimentIdentifier(experiment));
        }
    }

    private final static void checkSampleInGroup(final SamplePE sample)
    {
        if (sample.getGroup() == null)
        {
            throw UserFailureException.fromTemplate(
                    "The sample '%s' has to belong to a group to be registered in the experiment.",
                    sample.getSampleIdentifier());
        }
    }

    public void edit(SampleIdentifier identifier, List<SampleProperty> properties,
            ExperimentIdentifier experimentIdentifierOrNull, Date version)
    {
        loadBySampleIdentifier(identifier);
        if (sample.getModificationDate().equals(version) == false)
        {
            throw new UserFailureException("Sample has been modified in the meantime.");
        }
        updateProperties(properties);
        updateExperiment(experimentIdentifierOrNull);
        dataChanged = true;
    }

    private void updateExperiment(ExperimentIdentifier identifierOrNull)
    {
        ExperimentPE experimentOrNull = null;
        if (identifierOrNull != null)
        {
            experimentOrNull = findExperiment(identifierOrNull);
        }
        if (isExperimentChangeUnnecessary(experimentOrNull, sample.getExperiment()))
        {
            return;
        }
        ensureExperimentIsValid(identifierOrNull, experimentOrNull);
        ensureNoDatasetsBeforeExperimentChange(identifierOrNull);
        sample.setExperiment(experimentOrNull);
    }

    private void ensureExperimentIsValid(ExperimentIdentifier identOrNull,
            ExperimentPE experimentOrNull)
    {
        if (experimentOrNull != null && experimentOrNull.getInvalidation() != null)
        {
            throw UserFailureException
                    .fromTemplate(
                            "The sample '%s' cannot be assigned to the experiment '%s' " +
                            "because the experiment has been invalidated.",
                            sample.getSampleIdentifier(), identOrNull);
        }
    }

    private void ensureNoDatasetsBeforeExperimentChange(ExperimentIdentifier identOrNull)
    {
        if (hasDatasets())
        {
            String actionDesc;
            if (identOrNull != null)
            {
                actionDesc = "assigned to the new experiment '" + identOrNull + "'";
            } else
            {
                actionDesc = "removed from the experiment";
            }
            throw UserFailureException
                    .fromTemplate(
                            "The sample '%s' cannot be %s because there are already datasets registered for this sample.",
                            sample.getSampleIdentifier(), actionDesc);
        }
    }

    private boolean isExperimentChangeUnnecessary(ExperimentPE newExperimentOrNull,
            ExperimentPE experimentOrNull)
    {
        return experimentOrNull == null ? newExperimentOrNull == null 
                                        : experimentOrNull.equals(newExperimentOrNull);
    }

    private ExperimentPE findExperiment(ExperimentIdentifier identifierOrNull)
    {
        String groupCode = identifierOrNull.getGroupCode();
        String projectCode = identifierOrNull.getProjectCode();
        String databaseInstanceCode = identifierOrNull.getDatabaseInstanceCode();
        ProjectPE project =
                getProjectDAO().tryFindProject(databaseInstanceCode, groupCode, projectCode);
        if (project == null)
        {
            throw UserFailureException.fromTemplate(
                    "No project '%s' could be found in the '%s' group!", projectCode, groupCode);
        }
        String experimentCode = identifierOrNull.getExperimentCode();
        ExperimentPE experiment =
                getExperimentDAO().tryFindByCodeAndProject(project, experimentCode);
        if (experiment == null)
        {
            throw UserFailureException.fromTemplate(
                    "No experiment '%s' could be found in the '%s/%s' project!", experimentCode,
                    groupCode, projectCode);
        }
        return experiment;
    }

    private void updateProperties(List<SampleProperty> properties)
    {
        final Set<SamplePropertyPE> existingProperties = sample.getProperties();
        final EntityTypePE type = sample.getSampleType();
        final PersonPE registrator = findRegistrator();
        sample.setProperties(entityPropertiesConverter.updateProperties(existingProperties, type,
                properties, registrator));
    }
}
