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
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.translator.AttachmentTranslator;
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

    public void tryToLoadBySampleTechId(final TechId sampleId)
    {
        assert sampleId != null : "Unspecified id.";

        sample = tryToGetSampleByTechId(sampleId);
    }

    public final SamplePE getSample() throws IllegalStateException
    {
        if (sample == null)
        {
            throw new IllegalStateException("Unloaded sample.");
        }
        return sample;
    }

    public void loadDataByTechId(TechId sampleId)
    {
        onlyNewSamples = false;
        tryToLoadBySampleTechId(sampleId);
        if (sample == null)
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

    public void deleteByTechId(TechId sampleId, String reason) throws UserFailureException
    {
        loadDataByTechId(sampleId);
        try
        {
            getSampleDAO().delete(sample);
            getEventDAO().persist(createDeletionEvent(sample, session.tryGetPerson(), reason));
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Sample '%s'", sample.getCode()), EntityKind.SAMPLE);
        }
    }

    public static EventPE createDeletionEvent(SamplePE sample, PersonPE registrator, String reason)
    {
        EventPE event = new EventPE();
        event.setEventType(EventType.DELETION);
        event.setEntityType(EntityType.SAMPLE);
        event.setIdentifier(sample.getPermId());
        event.setDescription(getDeletionDescription(sample));
        event.setReason(reason);
        event.setRegistrator(registrator);

        return event;
    }

    private static String getDeletionDescription(SamplePE sample)
    {
        return String.format("%s [%s]", sample.getIdentifier(), sample.getPermId());
    }

    public final void define(final NewSample newSample)
    {
        assert newSample != null : "Unspecified new sample.";

        sample = createSample(newSample, null, null, null);
        dataChanged = true;
        onlyNewSamples = true;
    }

    public final void save()
    {
        assert sample != null : "Sample not loaded.";
        if (dataChanged)
        {
            try
            {
                getSampleDAO().createSample(sample);
            } catch (final DataIntegrityViolationException ex)
            {
                // needed because we throw an exception in DAO instead of relying on DB constraint
                throw UserFailureException.fromTemplate(ex.getMessage());
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
        checkBusinessRules(sample, getExternalDataDAO(), null);
        onlyNewSamples = false;
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

    public void update(SampleUpdatesDTO updates)
    {
        loadDataByTechId(updates.getSampleId());
        if (updates.getVersion().equals(sample.getModificationDate()) == false)
        {
            throwModifiedEntityException("Sample");
        }
        updateProperties(updates.getProperties());
        updateGroup(updates.getSampleIdentifier());
        updateExperiment(updates.getExperimentIdentifierOrNull());
        setGeneratedFrom(updates.getSampleIdentifier(), sample, updates.getParentIdentifierOrNull());
        setContainer(updates.getSampleIdentifier(), sample, updates.getContainerIdentifierOrNull());
        for (NewAttachment attachment : updates.getAttachments())
        {
            addAttachment(AttachmentTranslator.translate(attachment));
        }
        dataChanged = true;
    }

    private void updateGroup(SampleOwnerIdentifier sampleOwnerIdentifier)
    {
        if (sampleOwnerIdentifier != null)
        {
            final SampleOwner sampleOwner =
                    getSampleOwnerFinder().figureSampleOwner(sampleOwnerIdentifier);
            GroupPE group = sampleOwner.tryGetGroup();
            sample.setDatabaseInstance(sampleOwner.tryGetDatabaseInstance());
            sample.setGroup(group);
        }
    }

    private void updateExperiment(ExperimentIdentifier expIdentifierOrNull)
    {
        if (expIdentifierOrNull != null)
        {
            fillGroupIdentifier(expIdentifierOrNull);
            changeExperiment(expIdentifierOrNull);
        } else
        {
            removeFromExperiment();
        }
    }

    private void removeFromExperiment()
    {
        if (hasDatasets(getExternalDataDAO(), sample))
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

        changeDatasetsExperiment(sample.getDatasets(), newExperiment);
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

    private boolean isExperimentUnchanged(ExperimentPE newExperimentOrNull,
            ExperimentPE experimentOrNull)
    {
        return experimentOrNull == null ? newExperimentOrNull == null : experimentOrNull
                .equals(newExperimentOrNull);
    }

    private void updateProperties(List<IEntityProperty> properties)
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

    public final void enrichWithPropertyTypes()
    {
        if (sample != null)
        {
            HibernateUtils.initialize(sample.getSampleType());
            HibernateUtils.initialize(sample.getSampleType().getSampleTypePropertyTypes());
        }
    }
}
