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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * The unique {@link ISampleBO} implementation.
 * 
 * @author Christian Ribeaud
 */
public final class SampleBO extends AbstractSampleBusinessObject implements ISampleBO
{
    private SamplePE sample;

    private final List<AttachmentPE> attachments = new ArrayList<AttachmentPE>();

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

    private static final String PROPERTY_TYPES = "sampleType.sampleTypePropertyTypesInternal";

    private static final String VOCABULARY_TERMS =
            PROPERTY_TYPES + ".propertyTypeInternal.vocabulary.vocabularyTerms";

    public void loadDataByTechId(TechId sampleId)
    {
        String[] connections =
            { PROPERTY_TYPES, VOCABULARY_TERMS };
        sample = getSampleDAO().tryGetByTechId(sampleId, connections);
        if (sampleId == null)
        {
            throw new UserFailureException(String.format("Sample with ID '%s' does not exist.",
                    sampleId));
        }
        dataChanged = false;
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
        if (dataChanged)
        {
            try
            {
                getSampleDAO().createSample(sample);
            } catch (final DataAccessException ex)
            {
                throwException(ex, String.format("Sample '%s'", sample.getSampleIdentifier()));
            }
            dataChanged = false;
        }
        if (attachments.isEmpty() == false)
        {
            final IAttachmentDAO dao = getAttachmentDAO();
            for (final AttachmentPE property : attachments)
            {
                try
                {
                    dao.createAttachment(property, sample);
                } catch (final DataAccessException e)
                {
                    final String fileName = property.getFileName();
                    throwException(e, String.format("Filename '%s' for sample '%s'", fileName,
                            sample.getSampleIdentifier()));
                }
            }
            attachments.clear();
        }
        checkBusinessRules();
    }

    private void checkBusinessRules()
    {
        entityPropertiesConverter.checkMandatoryProperties(sample.getProperties(), sample
                .getSampleType());
    }

    public void setExperiment(ExperimentPE experiment)
    {
        assert sample != null : "Sample not loaded.";

        checkValid(sample);
        checkSampleInGroup(sample);
        checkSampleUnused(sample);
        checkSampleWithoutDatasets();
        sample.setExperiment(experiment);
        try
        {
            getSampleDAO().updateSample(sample);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Couldn't update sample '%s'", sample
                    .getSampleIdentifier()));
        }
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
        SampleUtils.checkSampleWithoutDatasets(getExternalDataDAO(), sample);
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
                    "The sample '%s' is shared and cannot be assigned to any experiment.", sample
                            .getSampleIdentifier());
        }
    }

    public void update(SampleIdentifier identifier, List<SampleProperty> properties,
            ExperimentIdentifier experimentIdentifierOrNull, List<AttachmentPE> newAttachments,
            Date version)
    {
        loadBySampleIdentifier(identifier);
        if (version.equals(sample.getModificationDate()) == false)
        {
            throwModifiedEntityException("Sample");
        }
        updateProperties(properties);
        updateExperiment(experimentIdentifierOrNull);
        for (AttachmentPE a : newAttachments)
        {
            addAttachment(a);
        }
        dataChanged = true;
    }

    private void updateExperiment(ExperimentIdentifier identifierOrNull)
    {
        if (identifierOrNull == null)
        {
            removeFromExperiment();
        } else
        {
            fillGroupIdentifier(identifierOrNull);
            changeExperiment(identifierOrNull);
        }

    }

    private void removeFromExperiment()
    {
        if (hasDatasets())
        {
            throw UserFailureException.fromTemplate(
                    "Cannot detach the sample '%s' from the experiment "
                            + "because there are already datasets attached to the sample.", sample
                            .getIdentifier());
        }
        sample.setExperiment(null);
    }

    private void changeExperiment(ExperimentIdentifier identifier)
    {
        ExperimentPE newExperiment = findExperiment(identifier);
        if (isExperimentUnchanged(newExperiment, sample.getExperiment()))
        {
            return;
        }
        ensureExperimentIsValid(identifier, newExperiment);
        ensureSampleAttachableToExperiment();

        GroupPE experimentGroup = newExperiment.getProject().getGroup();
        changeDatasetsExperiment(sample.getAcquiredDatasets(), newExperiment);
        changeDatasetsExperiment(sample.getDerivedDatasets(), newExperiment);
        sample.setGroup(experimentGroup);
        sample.setExperiment(newExperiment);
    }

    private void changeDatasetsExperiment(Set<DataPE> datasets, ExperimentPE experiment)
    {
        for (DataPE dataset : datasets)
        {
            dataset.setExperiment(experiment);
        }
    }

    private void ensureSampleAttachableToExperiment()
    {
        if (sample.getGroup() == null)
        {
            throw UserFailureException.fromTemplate(
                    "It is not allowed to connect a shared sample '%s' to the experiment.", sample
                            .getIdentifier());
        }
    }

    private void ensureExperimentIsValid(ExperimentIdentifier identOrNull,
            ExperimentPE experimentOrNull)
    {
        if (experimentOrNull != null && experimentOrNull.getInvalidation() != null)
        {
            throw UserFailureException.fromTemplate(
                    "The sample '%s' cannot be assigned to the experiment '%s' "
                            + "because the experiment has been invalidated.", sample
                            .getSampleIdentifier(), identOrNull);
        }
    }

    private boolean hasDatasets()
    {
        return SampleUtils.hasDatasets(getExternalDataDAO(), sample);
    }

    private boolean isExperimentUnchanged(ExperimentPE newExperimentOrNull,
            ExperimentPE experimentOrNull)
    {
        return experimentOrNull == null ? newExperimentOrNull == null : experimentOrNull
                .equals(newExperimentOrNull);
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

    public void setGeneratedCode()
    {
        final String code = createCode(EntityKind.SAMPLE);
        sample.setCode(code);
    }

    public void addAttachment(AttachmentPE sampleAttachment)
    {
        assert sample != null : "no sample has been loaded";
        sampleAttachment.setRegistrator(findRegistrator());
        escapeFileName(sampleAttachment);
        attachments.add(sampleAttachment);
    }

    private void escapeFileName(final AttachmentPE attachment)
    {
        if (attachment != null)
        {
            attachment.setFileName(SamplePE.escapeFileName(attachment.getFileName()));
        }
    }

    private void checkSampleLoaded()
    {
        if (sample == null)
        {
            throw new IllegalStateException("Unloaded sample.");
        }
    }

    public AttachmentPE getSampleFileAttachment(final String filename, final int version)
    {
        checkSampleLoaded();
        sample.ensureAttachmentsLoaded();
        final Set<AttachmentPE> attachmentsSet = sample.getAttachments();
        for (AttachmentPE att : attachmentsSet)
        {
            if (att.getFileName().equals(filename) && att.getVersion() == version)
            {
                HibernateUtils.initialize(att.getAttachmentContent());
                return att;
            }
        }
        throw new UserFailureException("Attachment '" + filename + "' (version '" + version
                + "') not found in sample '" + sample.getIdentifier() + "'.");
    }

    public final void enrichWithAttachments()
    {
        if (sample != null)
        {
            sample.ensureAttachmentsLoaded();
        }
    }
}
