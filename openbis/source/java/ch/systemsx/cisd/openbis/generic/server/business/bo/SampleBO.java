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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertiesConverter;
import ch.systemsx.cisd.openbis.generic.shared.IEntityOperationChecker;
import ch.systemsx.cisd.openbis.generic.shared.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
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

    private boolean spaceUpdated;

    public SampleBO(final IDAOFactory daoFactory, final Session session,
            final IRelationshipService relationshipService,
            final IEntityOperationChecker entityOperationChecker)
    {
        super(daoFactory, session, relationshipService, entityOperationChecker);
    }

    SampleBO(final IDAOFactory daoFactory, final Session session,
            final IEntityPropertiesConverter entityPropertiesConverter,
            IRelationshipService relationshipService,
            final IEntityOperationChecker entityOperationChecker)
    {
        super(daoFactory, session, entityPropertiesConverter, relationshipService,
                entityOperationChecker);
    }

    //
    // ISampleBO
    //
    @Override
    public SamplePE tryToGetSample()
    {
        return sample;
    }

    @Override
    public void tryToLoadBySampleIdentifier(SampleIdentifier identifier)
    {
        assert identifier != null : "Unspecified identifier.";

        sample = tryToGetSampleByIdentifier(identifier);
    }

    public void tryToLoadBySamplePermId(String permId)
    {
        assert permId != null : "Unspecified perm id.";

        sample = getSampleDAO().tryToFindByPermID(permId);
    }

    public void tryToLoadBySampleTechId(final TechId sampleId)
    {
        assert sampleId != null : "Unspecified id.";

        sample = tryToGetSampleByTechId(sampleId);
    }

    @Override
    public final SamplePE getSample() throws IllegalStateException
    {
        if (sample == null)
        {
            throw new IllegalStateException("Unloaded sample.");
        }
        return sample;
    }

    @Override
    public void loadDataByTechId(TechId sampleId)
    {
        onlyNewSamples = false;
        tryToLoadBySampleTechId(sampleId);
        if (sample == null)
        {
            throw new UserFailureException(String.format("Sample with ID '%s' does not exist.",
                    sampleId));
        }
        dataChanged = spaceUpdated = false;
    }

    @Override
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

    @Override
    public final void loadBySamplePermId(final String permId) throws UserFailureException
    {
        tryToLoadBySamplePermId(permId);
        if (sample == null)
        {
            throw UserFailureException.fromTemplate(
                    "No sample could be found with given permId '%s'.", permId);
        }
    }

    @Override
    public final void define(final NewSample newSample)
    {
        assert newSample != null : "Unspecified new sample.";
        assertInstanceSampleCreationAllowed(Collections.singletonList(newSample));
        sample = createSample(newSample, null, null, null, null);
        dataChanged = spaceUpdated = true;
        onlyNewSamples = true;
    }

    @Override
    public final void save()
    {
        assert sample != null : "Sample not loaded.";
        if (dataChanged)
        {
            try
            {
                getSampleDAO().createOrUpdateSample(sample, findPerson());
            } catch (final DataAccessException ex)
            {
                throwException(ex, String.format("Sample '%s'", sample.getSampleIdentifier()));
            }
            try
            {
                checkAllBusinessRules(sample, getDataDAO(), null, spaceUpdated);
            } finally
            {
                onlyNewSamples = dataChanged = spaceUpdated = false;
            }
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
                    throwException(
                            e,
                            String.format("Filename '%s' for sample '%s'", fileName,
                                    sample.getSampleIdentifier()));
                }
            }
            attachments.clear();
        }
    }

    @Override
    public void setExperiment(ExperimentPE experiment)
    {
        assert sample != null : "Sample not loaded.";

        checkAvailable(sample);
        checkSampleInGroup(sample);
        checkSampleUnused(sample);
        checkSampleWithoutDatasets();

        this.relationshipService.assignSampleToExperiment(session, sample, experiment);
        try
        {
            getSampleDAO().updateSample(sample, findPerson());
        } catch (final DataAccessException ex)
        {
            throwException(ex,
                    String.format("Couldn't update sample '%s'", sample.getSampleIdentifier()));
        }

        dataChanged = spaceUpdated = false;
    }

    static private void checkAvailable(SamplePE sample)
    {
        if (sample.getDeletion() != null)
        {
            throw UserFailureException.fromTemplate("Given sample '%s' is in trash.",
                    sample.getSampleIdentifier());
        }
    }

    private final void checkSampleWithoutDatasets()
    {
        SampleUtils.checkSampleWithoutDatasets(getDataDAO(), sample);
    }

    private final static void checkSampleUnused(final SamplePE sample)
    {
        ExperimentPE experiment = sample.getExperiment();
        if (experiment != null && experiment.getDeletion() == null)
        {
            throw UserFailureException.fromTemplate(
                    "Given sample code '%s' already registered for experiment '%s'.",
                    sample.getSampleIdentifier(),
                    IdentifierHelper.createExperimentIdentifier(experiment));
        }
    }

    private final static void checkSampleInGroup(final SamplePE sample)
    {
        if (sample.getSpace() == null)
        {
            throw UserFailureException.fromTemplate(
                    "The sample '%s' is shared and cannot be assigned to any experiment.",
                    sample.getSampleIdentifier());
        }
    }

    @Override
    public void update(SampleUpdatesDTO updates)
    {
        loadDataByTechId(updates.getSampleIdOrNull());

        assertInstanceSampleUpdateAllowed(Collections.singletonList(sample));

        if (updates.getVersion().equals(sample.getModificationDate()) == false)
        {
            throwModifiedEntityException("Sample");
        }
        updateProperties(updates.getProperties());
        spaceUpdated = updateSpace(sample, updates.getSampleIdentifier(), null);
        if (updates.isUpdateExperimentLink())
        {
            updateExperiment(sample, updates.getExperimentIdentifierOrNull(), null);
        }
        setContainer(updates.getSampleIdentifier(), sample, updates.getContainerIdentifierOrNull(),
                null);
        for (NewAttachment attachment : updates.getAttachments())
        {
            addAttachment(AttachmentTranslator.translate(attachment));
        }
        updateParents(updates);
        dataChanged = true;
    }

    private void updateProperties(List<IEntityProperty> properties)
    {
        final Set<SamplePropertyPE> existingProperties = sample.getProperties();
        final SampleTypePE type = sample.getSampleType();
        sample.setProperties(convertProperties(type, existingProperties, properties));
    }

    private void updateParents(SampleUpdatesDTO updates)
    {
        String[] parentCodes = updates.getModifiedParentCodesOrNull();
        if (parentCodes != null)
        {
            attachParents(parentCodes);
        }
    }

    // attaches specified existing samples to the sample as parents
    private void attachParents(String[] parentCodes)
    {
        setParents(sample, parentCodes, null);
    }

    @Override
    public void addAttachment(AttachmentPE sampleAttachment)
    {
        assert sample != null : "no sample has been loaded";
        assertInstanceSampleUpdateAllowed(Collections.singletonList(sample));
        sampleAttachment.setRegistrator(findPerson());
        escapeFileName(sampleAttachment);
        attachments.add(sampleAttachment);
    }

    private void escapeFileName(final AttachmentPE attachment)
    {
        if (attachment != null)
        {
            attachment.setFileName(AttachmentHolderPE.escapeFileName(attachment.getFileName()));
        }
    }

    private void checkSampleLoaded()
    {
        if (sample == null)
        {
            throw new IllegalStateException("Unloaded sample.");
        }
    }

    @Override
    public AttachmentPE getSampleFileAttachment(final String filename, final Integer versionOrNull)
    {
        checkSampleLoaded();
        sample.ensureAttachmentsLoaded();
        AttachmentPE att =
                versionOrNull == null ? getAttachment(filename) : getAttachment(filename,
                        versionOrNull);
        if (att != null)
        {
            HibernateUtils.initialize(att.getAttachmentContent());
            return att;
        }

        throw new UserFailureException(
                "Attachment '"
                        + filename
                        + "' "
                        + (versionOrNull == null ? "(latest version)" : "(version '"
                                + versionOrNull + "')") + " not found in sample '"
                        + sample.getIdentifier() + "'.");
    }

    private AttachmentPE getAttachment(String filename, final int version)
    {
        final Set<AttachmentPE> attachmentsSet = sample.getAttachments();
        for (AttachmentPE att : attachmentsSet)
        {
            if (att.getFileName().equals(filename) && att.getVersion() == version)
            {
                return att;
            }
        }

        return null;
    }

    private AttachmentPE getAttachment(String filename)
    {
        AttachmentPE latest = null;
        final Set<AttachmentPE> attachmentsSet = sample.getAttachments();
        for (AttachmentPE att : attachmentsSet)
        {
            if (att.getFileName().equals(filename))
            {
                if (latest == null || latest.getVersion() < att.getVersion())
                {
                    latest = att;
                }
            }
        }

        return latest;
    }

    @Override
    public final void enrichWithAttachments()
    {
        if (sample != null)
        {
            sample.ensureAttachmentsLoaded();
        }
    }

    @Override
    public final void enrichWithPropertyTypes()
    {
        if (sample != null)
        {
            HibernateUtils.initialize(sample.getSampleType());
            HibernateUtils.initialize(sample.getSampleType().getSampleTypePropertyTypes());
        }
    }

    @Override
    public void enrichWithProperties()
    {
        if (sample != null)
        {
            HibernateUtils.initialize(sample.getProperties());
        }
    }

    @Override
    public void updateManagedProperty(IManagedProperty managedProperty)
    {
        assertInstanceSampleUpdateAllowed(Collections.singletonList(sample));
        final Set<SamplePropertyPE> existingProperties = sample.getProperties();
        final SampleTypePE type = sample.getSampleType();
        final PersonPE registrator = findPerson();
        sample.setProperties(entityPropertiesConverter.updateManagedProperty(existingProperties,
                type, managedProperty, registrator));

        dataChanged = true;
    }

}
