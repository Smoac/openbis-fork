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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.IASyncAction;
import ch.systemsx.cisd.openbis.generic.server.MaterialHelper;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdPredicate.DataSetTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdPredicate.ProjectTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewDataSetsWithTypePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewExperimentPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewExperimentsWithTypePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewSamplePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewSamplesWithTypePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.UpdatedExperimentsWithTypePredicate;
import ch.systemsx.cisd.openbis.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.batch.IBatchOperation;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.EntityCodeGenerator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IProjectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSetsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.translator.AttachmentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MetaprojectTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.ServerUtils;
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

    @Resource(name = ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER)
    protected ICommonServer commonServer;

    @Resource(name = ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames.GENERIC_PLUGIN_SERVER)
    private IGenericServer genericServer;

    public GenericServer()
    {
    }

    @Private
    GenericServer(final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager,
            final IGenericBusinessObjectFactory businessObjectFactory,
            final ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            final IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin)
    {
        super(sessionManager, daoFactory, propertiesBatchManager, sampleTypeSlaveServerPlugin,
                dataSetTypeSlaveServerPlugin);
        this.businessObjectFactory = businessObjectFactory;
    }

    //
    // IInvocationLoggerFactory
    //

    /**
     * Creates a logger used to log invocations of objects of this class.
     */
    @Override
    public IGenericServer createLogger(IInvocationLoggerContext context)
    {
        return new GenericServerLogger(getSessionManager(), context);
    }

    //
    // IGenericServer
    //

    public final SampleParentWithDerived getSampleInfo(final String sessionToken,
            final SampleIdentifier identifier)
    {
        assert sessionToken != null : "Unspecified session token.";
        assert identifier != null : "Unspecified sample identifier.";

        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadBySampleIdentifier(identifier);
        sampleBO.enrichWithAttachments();
        sampleBO.enrichWithPropertyTypes();
        final SamplePE sample = sampleBO.getSample();
        Collection<MetaprojectPE> metaprojectPEs =
                getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                        session.tryGetPerson(), sample);
        return SampleTranslator.translate(getSampleTypeSlaveServerPlugin(sample.getSampleType())
                .getSampleInfo(session, sample), session.getBaseIndexURL(), MetaprojectTranslator
                .translate(metaprojectPEs));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public SampleParentWithDerived getSampleInfo(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class)
            final TechId sampleId) throws UserFailureException
    {
        return commonServer.getSampleInfo(sessionToken, sampleId);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void registerSample(final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplePredicate.class)
            final NewSample newSample, final Collection<NewAttachment> attachments)
    {
        assert sessionToken != null : "Unspecified session token.";
        assert newSample != null : "Unspecified new sample.";

        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        newSample.setAttachments(new ArrayList<NewAttachment>(attachments));
        sampleBO.define(newSample);
        sampleBO.save();
    }

    public Experiment getExperimentInfo(final String sessionToken,
            final ExperimentIdentifier identifier)
    {
        final Session session = getSession(sessionToken);
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadByExperimentIdentifier(identifier);
        experimentBO.enrichWithProperties();
        experimentBO.enrichWithAttachments();
        final ExperimentPE experiment = experimentBO.getExperiment();
        Collection<MetaprojectPE> metaprojectPEs =
                getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                        session.tryGetPerson(), experiment);
        if (experiment == null)
        {
            throw UserFailureException.fromTemplate(
                    "No experiment could be found with given identifier '%s'.", identifier);
        }
        return ExperimentTranslator.translate(experiment, session.getBaseIndexURL(),
                MetaprojectTranslator.translate(metaprojectPEs),
                ExperimentTranslator.LoadableFields.PROPERTIES,
                ExperimentTranslator.LoadableFields.ATTACHMENTS);
    }

    public Experiment getExperimentInfo(final String sessionToken, final TechId experimentId)
    {
        final Session session = getSession(sessionToken);
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadDataByTechId(experimentId);
        experimentBO.enrichWithProperties();
        experimentBO.enrichWithAttachments();
        final ExperimentPE experiment = experimentBO.getExperiment();
        Collection<MetaprojectPE> metaprojectPEs =
                getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                        session.tryGetPerson(), experiment);
        return ExperimentTranslator.translate(experiment, session.getBaseIndexURL(),
                MetaprojectTranslator.translate(metaprojectPEs),
                ExperimentTranslator.LoadableFields.PROPERTIES,
                ExperimentTranslator.LoadableFields.ATTACHMENTS);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public ExternalData getDataSetInfo(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetTechIdPredicate.class)
            TechId datasetId)
    {
        return commonServer.getDataSetInfo(sessionToken, datasetId);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public AttachmentWithContent getExperimentFileAttachment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class)
            TechId experimentId, String filename, Integer versionOrNull)
            throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadDataByTechId(experimentId);
        return AttachmentTranslator.translateWithContent(experimentBO.getExperimentFileAttachment(
                filename, versionOrNull));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    @Capability("WRITE_SAMPLE")
    public void registerOrUpdateSamples(final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplesWithTypePredicate.class)
            final List<NewSamplesWithTypes> newSamplesWithType) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        for (NewSamplesWithTypes samples : newSamplesWithType)
        {
            if (samples.isAllowUpdateIfExist() == false)
            {
                registerSamples(session, samples, session.tryGetPerson());
            } else
            {
                SampleBatchRegisterOrUpdate sampleBatchOperation =
                        createSampleBatchOperation(session, samples);
                BatchOperationExecutor.executeInBatches(sampleBatchOperation);
            }
        }
    }

    private SampleBatchRegisterOrUpdate createSampleBatchOperation(final Session session,
            NewSamplesWithTypes samples)
    {
        ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        return new SampleBatchRegisterOrUpdate(sampleLister, samples.getNewEntities(),
                samples.getEntityType(), session);
    }

    private class SampleBatchRegisterOrUpdate implements IBatchOperation<NewSample>
    {

        private final List<NewSample> entities;

        private final SampleType sampleType;

        private final ISampleLister sampleLister;

        private final Session session;

        public SampleBatchRegisterOrUpdate(ISampleLister sampleLister, List<NewSample> entities,
                SampleType sampleType, Session session)
        {
            this.sampleLister = sampleLister;
            this.entities = entities;
            this.sampleType = sampleType;
            this.session = session;
        }

        @Override
        public void execute(List<NewSample> newSamples)
        {
            fillHomeSpace(newSamples, session.tryGetHomeGroupCode());
            List<Sample> existingSamples = fetchExistingSamples(newSamples);

            List<NewSample> samplesToUpdate =
                    SampleRegisterOrUpdateUtil.getSamplesToUpdate(newSamples, existingSamples);
            List<NewSample> samplesToRegister = new ArrayList<NewSample>(newSamples);
            samplesToRegister.removeAll(samplesToUpdate);
            registerSamples(session, new NewSamplesWithTypes(sampleType, samplesToRegister),
                    session.tryGetPerson());
            updateSamples(session, new NewSamplesWithTypes(sampleType, samplesToUpdate));
        }

        // TODO 2011-08-31, Tomasz Pylak: remove existing hacks
        // 1. replaces contained samples with their containers if the container is
        // specified in the contained sample identifier. This should be replaced by a flag not to
        // update contained samples when the siRNA library is uploaded.
        // 2. matches samples only by the code, so it can return match more samples if the sample
        // with the same code exists in a different space. It does not hurt later on (matching is
        // done by the identifier and additional samples are ignored), but is inefficient.
        private List<Sample> fetchExistingSamples(List<NewSample> newSamples)
        {
            List<Sample> existingSamples = new ArrayList<Sample>();

            // add non-contained samples codes
            List<String> codes = SampleRegisterOrUpdateUtil.extractNonContainedCodes(newSamples);
            // NOTE 2011-08-17, Tomasz Pylak: this code never updates contained samples!
            List<Sample> list =
                    sampleLister.list(SampleRegisterOrUpdateUtil
                            .createListSamplesByCodeCriteria(codes));
            existingSamples.addAll(list);

            // for contained samples add container samples codes
            codes = SampleRegisterOrUpdateUtil.extractContainerCodes(newSamples);
            ListOrSearchSampleCriteria criteria =
                    SampleRegisterOrUpdateUtil.createListContainerSamplesByCodeCriteria(codes);
            List<Sample> existingContainers = sampleLister.list(criteria);
            for (Sample s : existingContainers)
            {
                existingSamples.addAll(sampleLister.list(new ListOrSearchSampleCriteria(
                        ListSampleCriteria.createForContainer(new TechId(s.getId())))));
            }
            return existingSamples;
        }

        @Override
        public List<NewSample> getAllEntities()
        {
            return entities;
        }

        @Override
        public String getEntityName()
        {
            return "sample";
        }

        @Override
        public String getOperationName()
        {
            return "update/register preprocessing";
        }

    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    @Capability("WRITE_SAMPLE")
    public void registerSamples(final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplesWithTypePredicate.class)
            final List<NewSamplesWithTypes> newSamplesWithType) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        for (NewSamplesWithTypes samples : newSamplesWithType)
        {
            registerSamples(session, samples, session.tryGetPerson());
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    @Capability("WRITE_SAMPLE")
    public void updateSamples(final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplesWithTypePredicate.class)
            final List<NewSamplesWithTypes> newSamplesWithType) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        for (NewSamplesWithTypes samples : newSamplesWithType)
        {
            updateSamples(session, samples);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_POWER_USER)
    @Capability("WRITE_DATASET")
    public void updateDataSets(final String sessionToken,
            @AuthorizationGuard(guardClass = NewDataSetsWithTypePredicate.class)
            final NewDataSetsWithTypes dataSets) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        final List<NewDataSet> newDataSets = dataSets.getNewDataSets();
        // Does nothing if samples list is empty.
        if (newDataSets.size() == 0)
        {
            return;
        }
        ServerUtils.prevalidate(newDataSets, "data set");
        final DataSetTypePE dataSetType =
                getDAOFactory().getDataSetTypeDAO().tryToFindDataSetTypeByCode(
                        dataSets.getDataSetType().getCode());
        if (dataSetType == null)
        {
            throw UserFailureException.fromTemplate("Data set type with code '%s' does not exist.",
                    dataSets.getDataSetType());
        }
        getPropertiesBatchManager().manageProperties(dataSetType, newDataSets,
                session.tryGetPerson());
        getDataSetTypeSlaveServerPlugin(dataSetType).updateDataSets(session,
                convertDataSets(newDataSets));
    }

    private void updateSamples(final Session session,
            final NewSamplesWithTypes updatedSamplesWithType)
    {
        final SampleType sampleType = updatedSamplesWithType.getEntityType();
        final List<NewSample> updatedSamples = updatedSamplesWithType.getNewEntities();
        assert sampleType != null : "Unspecified sample type.";
        assert updatedSamples != null : "Unspecified new samples.";

        // Does nothing if samples list is empty.
        if (updatedSamples.size() == 0)
        {
            return;
        }
        fillHomeSpace(updatedSamples, session.tryGetHomeGroupCode());
        ServerUtils.prevalidate(updatedSamples, "sample");
        final String sampleTypeCode = sampleType.getCode();
        final SampleTypePE sampleTypePE =
                getDAOFactory().getSampleTypeDAO().tryFindSampleTypeByCode(sampleTypeCode);
        if (sampleTypePE == null)
        {
            throw UserFailureException.fromTemplate("Sample type with code '%s' does not exist.",
                    sampleTypeCode);
        }
        getPropertiesBatchManager().manageProperties(sampleTypePE, updatedSamples,
                session.tryGetPerson());
        getSampleTypeSlaveServerPlugin(sampleTypePE).updateSamples(session,
                convertSamples(updatedSamples));
    }

    private List<SampleBatchUpdatesDTO> convertSamples(final List<NewSample> updatedSamples)
    {
        List<SampleBatchUpdatesDTO> samples = new ArrayList<SampleBatchUpdatesDTO>();

        for (NewSample updatedSample : updatedSamples)
        {
            IdentifersExtractor extractor = new IdentifersExtractor(updatedSample);
            final List<IEntityProperty> properties = Arrays.asList(updatedSample.getProperties());
            final String[] parentsOrNull = updatedSample.getParentsOrNull();
            final SampleBatchUpdateDetails batchUpdateDetails =
                    createBatchUpdateDetails(updatedSample);

            samples.add(new SampleBatchUpdatesDTO(updatedSample.getDefaultSpaceIdentifier(),
                    extractor.getOldSampleIdentifier(), properties, extractor
                            .getExperimentIdentifierOrNull(), extractor.getNewSampleIdentifier(),
                    extractor.getContainerIdentifierOrNull(), parentsOrNull, batchUpdateDetails));
        }
        return samples;
    }

    private List<DataSetBatchUpdatesDTO> convertDataSets(final List<NewDataSet> updatedDataSets)
    {
        List<DataSetBatchUpdatesDTO> dataSets = new ArrayList<DataSetBatchUpdatesDTO>();

        for (NewDataSet updatedDataSet : updatedDataSets)
        {
            DataSetBatchUpdatesDTO dataSet = new DataSetBatchUpdatesDTO();
            DataSetIdentifiersExtractor extractor = new DataSetIdentifiersExtractor(updatedDataSet);

            dataSet.setDatasetCode(updatedDataSet.getCode());
            dataSet.setExperimentIdentifierOrNull(extractor.getExperimentIdentifierOrNull());
            dataSet.setSampleIdentifierOrNull(extractor.getSampleIdentifierOrNull());
            dataSet.setProperties(Arrays.asList(updatedDataSet.getProperties()));
            dataSet.setModifiedContainerDatasetCodeOrNull(updatedDataSet
                    .getContainerIdentifierOrNull());
            dataSet.setModifiedParentDatasetCodesOrNull(updatedDataSet
                    .getParentsIdentifiersOrNull());
            dataSet.setFileFormatTypeCode(updatedDataSet.getFileFormatOrNull());
            dataSet.setDetails(createBatchUpdateDetails(updatedDataSet));
            // it looks like this goes through GUI only, so for now no need to set the external
            // system information

            dataSets.add(dataSet);
        }
        return dataSets;
    }

    SampleBatchUpdateDetails createBatchUpdateDetails(NewSample sample)
    {
        if (sample instanceof UpdatedSample)
        {
            return ((UpdatedSample) sample).getBatchUpdateDetails();
        } else
        {
            IEntityProperty[] properties = sample.getProperties();
            Set<String> propertyCodes = new HashSet<String>();
            for (IEntityProperty p : properties)
            {
                propertyCodes.add(p.getPropertyType().getCode());
            }
            SampleBatchUpdateDetails result =
                    new SampleBatchUpdateDetails(false, false, false, propertyCodes);
            return result;
        }
    }

    DataSetBatchUpdateDetails createBatchUpdateDetails(NewDataSet dataSet)
    {
        if (dataSet instanceof UpdatedDataSet)
        {
            return ((UpdatedDataSet) dataSet).getBatchUpdateDetails();
        } else
        {
            IEntityProperty[] properties = dataSet.getProperties();
            Set<String> propertyCodes = new HashSet<String>();
            for (IEntityProperty p : properties)
            {
                propertyCodes.add(p.getPropertyType().getCode());
            }
            DataSetBatchUpdateDetails result = new DataSetBatchUpdateDetails();
            result.setPropertiesToUpdate(propertyCodes);
            return result;
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    @Capability("WRITE_EXPERIMENT_SAMPLE")
    public void registerExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = NewExperimentPredicate.class)
            final NewExperiment newExperiment, final Collection<NewAttachment> attachments)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert newExperiment != null : "Unspecified new experiment.";

        final Session session = getSession(sessionToken);

        List<NewSamplesWithTypes> newSamples = newExperiment.getNewSamples();
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        newExperiment.setAttachments(new ArrayList<NewAttachment>(attachments));
        experimentBO.define(newExperiment);
        experimentBO.save();
        ExperimentPE experiment = experimentBO.getExperiment();
        String experimentSpace = experiment.getProject().getSpace().getCode();
        if (newExperiment.isRegisterSamples())
        {
            for (NewSamplesWithTypes newSamplesWithType : newSamples)
            {
                List<NewSample> newEntities = newSamplesWithType.getNewEntities();
                for (NewSample newSample : newEntities)
                {
                    newSample.setDefaultSpaceIdentifier(new SpaceIdentifier(experimentSpace)
                            .toString());
                }
            }
            registerSamples(sessionToken, newSamples);
        }

        if (newExperiment.getSamples() != null && newExperiment.getSamples().length > 0)
        {
            List<SampleIdentifier> sampleIdentifiers = null;
            if (newSamples == null)
            {
                sampleIdentifiers =
                        IdentifierHelper.extractSampleIdentifiers(newExperiment.getSamples(), null);
            } else
            {
                sampleIdentifiers = IdentifierHelper.extractSampleIdentifiers(newExperiment);
            }
            for (SampleIdentifier si : sampleIdentifiers)
            {
                IdentifierHelper.fillAndCheckGroup(si, experimentSpace);
            }
            for (SampleIdentifier si : sampleIdentifiers)
            {
                ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
                sampleBO.loadBySampleIdentifier(si);
                sampleBO.setExperiment(experiment);
            }
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @Capability("WRITE_MATERIAL")
    public void registerMaterials(String sessionToken, List<NewMaterialsWithTypes> newMaterials)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        Session session = getSession(sessionToken);
        MaterialHelper materialHelper = getMaterialHelper(session);

        for (NewMaterialsWithTypes m : newMaterials)
        {
            registerMaterials(materialHelper, m);
        }
    }

    private void registerMaterials(MaterialHelper materialHelper, NewMaterialsWithTypes materials)
    {
        materialHelper.registerMaterials(materials.getEntityType().getCode(),
                materials.getNewEntities());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @Capability("WRITE_MATERIAL")
    public int updateMaterials(String sessionToken, List<NewMaterialsWithTypes> newMaterials,
            boolean ignoreUnregisteredMaterials) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        Session session = getSession(sessionToken);

        int count = 0;
        for (NewMaterialsWithTypes m : newMaterials)
        {
            count +=
                    getMaterialHelper(session).updateMaterials(m.getEntityType().getCode(),
                            m.getNewEntities(), ignoreUnregisteredMaterials);
        }
        return count;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public AttachmentWithContent getProjectFileAttachment(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectTechIdPredicate.class)
            TechId projectId, String fileName, Integer versionOrNull)
    {
        final Session session = getSession(sessionToken);
        final IProjectBO bo = businessObjectFactory.createProjectBO(session);
        bo.loadDataByTechId(projectId);
        return AttachmentTranslator.translateWithContent(bo.getProjectFileAttachment(fileName,
                versionOrNull));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public AttachmentWithContent getSampleFileAttachment(String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class)
            TechId sampleId, String fileName, Integer versionOrNull)
    {
        final Session session = getSession(sessionToken);
        final ISampleBO bo = businessObjectFactory.createSampleBO(session);
        bo.loadDataByTechId(sampleId);
        return AttachmentTranslator.translateWithContent(bo.getSampleFileAttachment(fileName,
                versionOrNull));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<String> generateCodes(String sessionToken, String prefix,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind entityKind, int number)
    {
        checkSession(sessionToken);
        return new EntityCodeGenerator(getDAOFactory()).generateCodes(prefix, entityKind, number);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    @Capability("WRITE_EXPERIMENT_SAMPLE")
    public ExperimentUpdateResult updateExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentUpdatesPredicate.class)
            ExperimentUpdatesDTO updates)
    {
        final Session session = getSession(sessionToken);
        if (updates.isRegisterSamples())
        {
            registerSamples(sessionToken, updates.getNewSamples());
        }
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.update(updates);
        experimentBO.save();
        ExperimentUpdateResult result = new ExperimentUpdateResult();
        ExperimentPE experiment = experimentBO.getExperiment();
        result.setVersion(experiment.getVersion());
        result.setSamples(getDAOFactory().getExperimentDAO().getSampleCodes(experiment));
        return result;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @Capability("WRITE_MATERIAL")
    public Date updateMaterial(String sessionToken, TechId materialId,
            List<IEntityProperty> properties, String[] metaprojects, Date version)
    {
        return commonServer.updateMaterial(sessionToken, materialId, properties, metaprojects,
                version);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public SampleUpdateResult updateSample(String sessionToken,
            @AuthorizationGuard(guardClass = SampleUpdatesPredicate.class)
            SampleUpdatesDTO updates)
    {
        return commonServer.updateSample(sessionToken, updates);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_POWER_USER)
    @Capability("WRITE_DATASET")
    public DataSetUpdateResult updateDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetUpdatesPredicate.class)
            DataSetUpdatesDTO updates)
    {
        return commonServer.updateDataSet(sessionToken, updates);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @Capability("WRITE_MATERIAL")
    public void registerOrUpdateMaterials(String sessionToken, List<NewMaterialsWithTypes> materials)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        MaterialHelper materialHelper = getMaterialHelper(session);
        for (NewMaterialsWithTypes materialsWithTypes : materials)
        {
            String materialTypeCode = materialsWithTypes.getEntityType().getCode();
            if (materialsWithTypes.isAllowUpdateIfExist())
            {
                materialHelper.registerOrUpdateMaterials(materialTypeCode,
                        materialsWithTypes.getNewEntities());
            } else
            {
                registerMaterials(materialHelper, materialsWithTypes);
            }
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    @Capability("WRITE_EXPERIMENT_SAMPLE")
    public void registerExperiments(String sessionToken,
            @AuthorizationGuard(guardClass = NewExperimentsWithTypePredicate.class)
            final NewExperimentsWithType experiments) throws UserFailureException
    {
        assert experiments != null : "Unspecified experiments.";
        assert sessionToken != null : "Unspecified session token.";
        assert experiments.getExperimentTypeCode() != null : "Experiments type not specified";
        assert experiments.getNewExperiments() != null : "Experiments collection not specified";

        final Session session = getSession(sessionToken);
        final List<NewBasicExperiment> newExperiments = experiments.getNewExperiments();
        if (newExperiments.size() == 0)
        {
            return;
        }
        ServerUtils.prevalidate(newExperiments, "experiment");
        final ExperimentTypePE experimentTypePE =
                (ExperimentTypePE) getDAOFactory().getEntityTypeDAO(EntityKind.EXPERIMENT)
                        .tryToFindEntityTypeByCode(experiments.getExperimentTypeCode());
        if (experimentTypePE == null)
        {
            throw UserFailureException.fromTemplate(
                    "Experiment type with code '%s' does not exist.", experimentTypePE);
        }
        getPropertiesBatchManager().manageProperties(experimentTypePE, newExperiments,
                session.tryGetPerson());
        BatchOperationExecutor.executeInBatches(new ExperimentBatchRegistration(
                businessObjectFactory.createExperimentTable(session), newExperiments,
                experimentTypePE));
    }

    /**
     * @param sessionToken The session token for the request
     * @param experiments Should be a NewExperimentsWithType where the newExperiments contains a
     *            collection of {@link UpdatedBasicExperiment} objects.
     */
    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    @Capability("WRITE_EXPERIMENT_SAMPLE")
    public void updateExperiments(String sessionToken,
            @AuthorizationGuard(guardClass = UpdatedExperimentsWithTypePredicate.class)
            final UpdatedExperimentsWithType experiments) throws UserFailureException
    {
        assert experiments != null : "Unspecified experiments.";
        assert sessionToken != null : "Unspecified session token.";
        assert experiments.getExperimentType() != null : "Experiments type not specified";
        assert experiments.getUpdatedExperiments() != null : "Experiments collection not specified";

        final Session session = getSession(sessionToken);
        final List<UpdatedBasicExperiment> newExperiments = experiments.getUpdatedExperiments();
        if (newExperiments.size() == 0)
        {
            return;
        }
        ServerUtils.prevalidate(newExperiments, "experiment");
        final ExperimentTypePE experimentTypePE =
                (ExperimentTypePE) getDAOFactory().getEntityTypeDAO(EntityKind.EXPERIMENT)
                        .tryToFindEntityTypeByCode(experiments.getExperimentType().getCode());
        if (experimentTypePE == null)
        {
            throw UserFailureException.fromTemplate(
                    "Experiment type with code '%s' does not exist.", experimentTypePE);
        }
        getPropertiesBatchManager().manageProperties(experimentTypePE, newExperiments,
                session.tryGetPerson());
        BatchOperationExecutor.executeInBatches(new ExperimentBatchUpdate(businessObjectFactory
                .createExperimentTable(session), convertExperiments(newExperiments),
                experimentTypePE));
    }

    /**
     * @param updatedExperiments The experiments should actually be instances of
     *            UpdatedBasicExperiment.
     */
    private List<ExperimentBatchUpdatesDTO> convertExperiments(
            final List<UpdatedBasicExperiment> updatedExperiments)
    {
        List<ExperimentBatchUpdatesDTO> experiments = new ArrayList<ExperimentBatchUpdatesDTO>();

        for (NewBasicExperiment experiment : updatedExperiments)
        {
            assert experiment instanceof UpdatedBasicExperiment;
            UpdatedBasicExperiment updatedExperiment = (UpdatedBasicExperiment) experiment;

            final ExperimentIdentifier oldExperimentIdentifier =
                    new ExperimentIdentifierFactory(updatedExperiment.getIdentifier().toUpperCase())
                            .createIdentifier();
            final List<IEntityProperty> properties =
                    Arrays.asList(updatedExperiment.getProperties());
            // If we allow changing projects, we will have to take new/old experiment identifiers
            // into account, but since this is currently not possible, there is nothing to do.
            final ExperimentIdentifier newExperimentIdentifier;
            final boolean isProjectUpdateRequested;
            String projectIdentifierOrNull = updatedExperiment.getNewProjectIdentifierOrNull();
            projectIdentifierOrNull =
                    (null != projectIdentifierOrNull) ? projectIdentifierOrNull.trim() : null;
            if (null != projectIdentifierOrNull && projectIdentifierOrNull.length() > 0)
            {
                // the new experiment identifier is derived form the project id
                ProjectIdentifier projectIdentifier =
                        new ProjectIdentifierFactory(projectIdentifierOrNull.toUpperCase())
                                .createIdentifier();
                newExperimentIdentifier =
                        new ExperimentIdentifier(projectIdentifier,
                                oldExperimentIdentifier.getExperimentCode());
                isProjectUpdateRequested = true;
            } else
            {
                newExperimentIdentifier = oldExperimentIdentifier;
                isProjectUpdateRequested = false;
            }

            experiments.add(new ExperimentBatchUpdatesDTO(oldExperimentIdentifier, properties,
                    newExperimentIdentifier, updatedExperiment.getBatchUpdateDetails(),
                    isProjectUpdateRequested));
        }
        return experiments;
    }

    private MaterialHelper getMaterialHelper(final Session session)
    {
        final MaterialHelper materialHelper =
                new MaterialHelper(session, businessObjectFactory, getDAOFactory(),
                        getPropertiesBatchManager());
        return materialHelper;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @Capability("WRITE_EXPERIMENT_SAMPLE_MATERIAL")
    public void registerOrUpdateSamplesAndMaterials(final String sessionToken,
            final List<NewSamplesWithTypes> newSamplesWithType,
            List<NewMaterialsWithTypes> newMaterialsWithType) throws UserFailureException
    {
        EntityExistenceChecker entityExistenceChecker = new EntityExistenceChecker(getDAOFactory());
        entityExistenceChecker.checkNewMaterials(newMaterialsWithType);
        entityExistenceChecker.checkNewSamples(newSamplesWithType);
        registerOrUpdateMaterials(sessionToken, newMaterialsWithType);
        registerOrUpdateSamples(sessionToken, newSamplesWithType);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @Capability("WRITE_EXPERIMENT_SAMPLE_MATERIAL")
    public void registerOrUpdateSamplesAndMaterialsAsync(final String sessionToken,
            final List<NewSamplesWithTypes> newSamplesWithType,
            final List<NewMaterialsWithTypes> newMaterialsWithType, String userEmail)
            throws UserFailureException
    {
        executeASync(userEmail, new IASyncAction()
            {
                @Override
                public String getName()
                {
                    return "General Batch Import";
                }

                @Override
                public boolean doAction(Writer messageWriter)
                {
                    try
                    {
                        genericServer.registerOrUpdateSamplesAndMaterials(sessionToken,
                                newSamplesWithType, newMaterialsWithType);
                    } catch (RuntimeException ex)
                    {
                        try
                        {
                            messageWriter.write(getName()
                                    + " has failed with a following exception: ");
                            messageWriter.write(ex.getMessage());
                            messageWriter
                                    .write("\n\nPlease correct the error or contact your administrator.");
                        } catch (IOException writingEx)
                        {
                            throw new UserFailureException(writingEx.getMessage()
                                    + " when trying to throw exception: " + ex.getMessage(), ex);
                        }
                        throw ex;
                    }
                    return true;
                }
            });
    }

}
