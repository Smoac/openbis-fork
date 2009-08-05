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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IProjectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;

/**
 * Implementation of client-server interface.
 * 
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.GENERIC_PLUGIN_SERVER)
public final class GenericServer extends AbstractServer<IGenericServer> implements
        ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer
{
    @Resource(name = ResourceNames.GENERIC_BUSINESS_OBJECT_FACTORY)
    private IGenericBusinessObjectFactory businessObjectFactory;

    public GenericServer()
    {
    }

    @Private
    GenericServer(final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory,
            final IGenericBusinessObjectFactory businessObjectFactory,
            final ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            final IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin)
    {
        super(sessionManager, daoFactory, sampleTypeSlaveServerPlugin, dataSetTypeSlaveServerPlugin);
        this.businessObjectFactory = businessObjectFactory;
    }

    //
    // IInvocationLoggerFactory
    //

    /**
     * Creates a logger used to log invocations of objects of this class.
     */
    public IGenericServer createLogger(final boolean invocationSuccessful, final long elapsedTime)
    {
        return new GenericServerLogger(getSessionManager(), invocationSuccessful, elapsedTime);
    }

    //
    // IGenericServer
    //

    public final SampleGenerationDTO getSampleInfo(final String sessionToken,
            final SampleIdentifier identifier)
    {
        assert sessionToken != null : "Unspecified session token.";
        assert identifier != null : "Unspecified sample identifier.";

        final Session session = getSessionManager().getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadBySampleIdentifier(identifier);
        sampleBO.enrichWithAttachments();
        final SamplePE sample = sampleBO.getSample();
        return getSampleTypeSlaveServerPlugin(sample.getSampleType())
                .getSampleInfo(session, sample);
    }

    public final SampleGenerationDTO getSampleInfo(final String sessionToken, final TechId sampleId)
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleId != null : "Unspecified sample techId.";

        final Session session = getSessionManager().getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(sampleId);
        sampleBO.enrichWithAttachments();
        final SamplePE sample = sampleBO.getSample();
        return getSampleTypeSlaveServerPlugin(sample.getSampleType())
                .getSampleInfo(session, sample);
    }

    public final void registerSample(final String sessionToken, final NewSample newSample,
            List<AttachmentPE> attachments)
    {
        assert sessionToken != null : "Unspecified session token.";
        assert newSample != null : "Unspecified new sample.";

        final Session session = getSessionManager().getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.define(newSample);
        sampleBO.save();
        for (AttachmentPE att : attachments)
        {
            sampleBO.addAttachment(att);
        }
        sampleBO.save();
    }

    public ExperimentPE getExperimentInfo(final String sessionToken,
            final ExperimentIdentifier identifier)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadByExperimentIdentifier(identifier);
        experimentBO.enrichWithProperties();
        experimentBO.enrichWithAttachments();
        final ExperimentPE experiment = experimentBO.getExperiment();
        return experiment;
    }

    public ExperimentPE getExperimentInfo(final String sessionToken, final TechId experimentId)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadDataByTechId(experimentId);
        experimentBO.enrichWithProperties();
        experimentBO.enrichWithAttachments();
        final ExperimentPE experiment = experimentBO.getExperiment();
        return experiment;
    }

    public MaterialPE getMaterialInfo(final String sessionToken, final TechId materialId)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IMaterialBO materialBO = businessObjectFactory.createMaterialBO(session);
        materialBO.loadDataByTechId(materialId);
        materialBO.enrichWithProperties();
        final MaterialPE material = materialBO.getMaterial();
        return material;
    }

    public ExternalDataPE getDataSetInfo(final String sessionToken, final TechId datasetId)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IExternalDataBO datasetBO = businessObjectFactory.createExternalDataBO(session);
        datasetBO.loadDataByTechId(datasetId);
        datasetBO.enrichWithParentsAndExperiment();
        datasetBO.enrichWithChildren();
        datasetBO.enrichWithProperties();
        final ExternalDataPE dataset = datasetBO.getExternalData();
        return dataset;
    }

    public AttachmentPE getExperimentFileAttachment(final String sessionToken,
            final TechId experimentId, final String filename, final int version)
            throws UserFailureException
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadDataByTechId(experimentId);
        return experimentBO.getExperimentFileAttachment(filename, version);
    }

    public final void registerSamples(final String sessionToken, final SampleType sampleType,
            final List<NewSample> newSamples) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSessionManager().getSession(sessionToken);
        registerSamples(session, new NewSamplesWithTypes(sampleType, newSamples));
    }

    public final void registerSamples(final String sessionToken,
            final List<NewSamplesWithTypes> newSamplesWithType) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSessionManager().getSession(sessionToken);
        for (NewSamplesWithTypes samples : newSamplesWithType)
        {
            registerSamples(session, samples);
        }
    }

    private void registerSamples(final Session session, final NewSamplesWithTypes newSamplesWithType)
    {
        final SampleType sampleType = newSamplesWithType.getSampleType();
        final List<NewSample> newSamples = newSamplesWithType.getNewSamples();
        assert sampleType != null : "Unspecified sample type.";
        assert newSamples != null : "Unspecified new samples.";

        // Does nothing if samples list is empty.
        if (newSamples.size() == 0)
        {
            return;
        }
        // Check uniqueness of given list based on sample identifier.
        final HashSet<NewSample> sampleSet = new HashSet<NewSample>(newSamples);
        if (sampleSet.size() != newSamples.size())
        {
            for (NewSample s : sampleSet)
            {
                newSamples.remove(s);
            }
            throw UserFailureException.fromTemplate("Following samples '%s' are duplicated.",
                    CollectionUtils.abbreviate(newSamples, 20));
        }
        final String sampleTypeCode = sampleType.getCode();
        final SampleTypePE sampleTypePE =
                getDAOFactory().getSampleTypeDAO().tryFindSampleTypeByCode(sampleTypeCode);
        if (sampleTypePE == null)
        {
            throw UserFailureException.fromTemplate("Sample type with code '%s' does not exist.",
                    sampleTypeCode);
        }
        getSampleTypeSlaveServerPlugin(sampleTypePE).registerSamples(session, newSamples);
    }

    public void registerExperiment(String sessionToken, NewExperiment newExperiment,
            List<AttachmentPE> attachments)
    {
        assert sessionToken != null : "Unspecified session token.";
        assert newExperiment != null : "Unspecified new experiment.";

        final Session session = getSessionManager().getSession(sessionToken);

        if (newExperiment.isRegisterSamples())
        {
            registerSamples(sessionToken, newExperiment.getNewSamples());
        }
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.define(newExperiment);
        experimentBO.save();
        for (AttachmentPE att : attachments)
        {
            experimentBO.addAttachment(att);
        }
        experimentBO.save();

        if (newExperiment.getSamples() != null && newExperiment.getSamples().length > 0)
        {
            ExperimentPE experiment = experimentBO.getExperiment();
            List<SampleIdentifier> sampleIdentifiers =
                    IdentifierHelper.extractSampleIdentifiers(newExperiment.getSamples());
            for (SampleIdentifier si : sampleIdentifiers)
            {
                IdentifierHelper
                        .fillAndCheckGroup(si, experiment.getProject().getGroup().getCode());
            }
            for (SampleIdentifier si : sampleIdentifiers)
            {
                ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
                sampleBO.loadBySampleIdentifier(si);
                sampleBO.setExperiment(experiment);
            }
        }
    }

    public void registerMaterials(String sessionToken, String materialTypeCode,
            List<NewMaterial> newMaterials)
    {
        assert sessionToken != null : "Unspecified session token.";
        assert materialTypeCode != null : "Unspecified material type.";
        assert newMaterials != null : "Unspecified new materials.";

        final Session session = getSessionManager().getSession(sessionToken);
        // Does nothing if material list is empty.
        if (newMaterials.size() == 0)
        {
            return;
        }
        // Check uniqueness of given list based on material code.
        final HashSet<NewMaterial> materialSet = new HashSet<NewMaterial>(newMaterials);
        if (materialSet.size() != newMaterials.size())
        {
            newMaterials.removeAll(materialSet);
            throw UserFailureException.fromTemplate("Following materials '%s' are duplicated.",
                    CollectionUtils.abbreviate(newMaterials, 20));
        }
        final MaterialTypePE materialTypePE =
                (MaterialTypePE) getDAOFactory().getEntityTypeDAO(EntityKind.MATERIAL)
                        .tryToFindEntityTypeByCode(materialTypeCode);
        if (materialTypePE == null)
        {
            throw UserFailureException.fromTemplate("Material type with code '%s' does not exist.",
                    materialTypeCode);
        }
        final IMaterialTable materialTable = businessObjectFactory.createMaterialTable(session);
        materialTable.add(newMaterials, materialTypePE);
        materialTable.save();
    }

    public AttachmentPE getProjectFileAttachment(String sessionToken, TechId projectId,
            String fileName, int version)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IProjectBO bo = businessObjectFactory.createProjectBO(session);
        bo.loadDataByTechId(projectId);
        return bo.getProjectFileAttachment(fileName, version);
    }

    public AttachmentPE getSampleFileAttachment(String sessionToken, TechId sampleId,
            String fileName, int version)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final ISampleBO bo = businessObjectFactory.createSampleBO(session);
        bo.loadDataByTechId(sampleId);
        return bo.getSampleFileAttachment(fileName, version);
    }

    public List<String> generateCodes(String sessionToken, String prefix, int number)
    {
        getSessionManager().getSession(sessionToken);
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < number; i++)
        {
            result.add(prefix + getDAOFactory().getCodeSequenceDAO().getNextCodeSequenceId());
        }
        return result;
    }

    public ExperimentUpdateResult updateExperiment(String sessionToken, ExperimentUpdatesDTO updates)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        if (updates.isRegisterSamples())
        {
            registerSamples(sessionToken, updates.getNewSamples());
        }
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.update(updates);
        experimentBO.save();
        ExperimentUpdateResult result = new ExperimentUpdateResult();
        ExperimentPE experiment = experimentBO.getExperiment();
        result.setModificationDate(experiment.getModificationDate());
        result.setSamples(extractCodes(experiment.getSamples()));
        return result;
    }

    @Private
    static final String[] extractCodes(List<SamplePE> samples)
    {
        String[] codes = new String[samples.size()];
        int i = 0;
        for (SamplePE samplePE : samples)
        {
            codes[i] = IdentifierHelper.extractCode(samplePE);
            i++;
        }
        return codes;
    }

    public Date updateMaterial(String sessionToken, TechId materialId,
            List<IEntityProperty> properties, Date version)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IMaterialBO materialBO = businessObjectFactory.createMaterialBO(session);
        materialBO.update(materialId, properties, version);
        materialBO.save();
        return materialBO.getMaterial().getModificationDate();
    }

    public Date updateSample(String sessionToken, SampleUpdatesDTO updates)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.update(updates);
        sampleBO.save();
        return sampleBO.getSample().getModificationDate();
    }

    public Date updateDataSet(String sessionToken, DataSetUpdatesDTO updates)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IExternalDataBO dataSetBO = businessObjectFactory.createExternalDataBO(session);
        dataSetBO.update(updates);
        return dataSetBO.getExternalData().getModificationDate();
    }

}
