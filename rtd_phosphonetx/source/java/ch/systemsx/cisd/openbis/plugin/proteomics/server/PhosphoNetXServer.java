/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.proteomics.server;

import java.util.List;

import javax.annotation.Resource;

import net.lemnik.eodsql.DataSet;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.translator.VocabularyTranslator;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.AccessionNumberBuilder;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.IAbundanceColumnDefinitionTable;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.IBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.IDataSetProteinTable;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.IProteinDetailsBO;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.IProteinInfoTable;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.IProteinRelatedSampleTable;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.IProteinSequenceTable;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.IProteinSummaryTable;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.ISampleProvider;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.DataSetProtein;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinByExperiment;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinDetails;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinRelatedSample;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinSequence;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinSummary;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto.ProteinReference;

/**
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.PROTEOMICS_PLUGIN_SERVER)
public class PhosphoNetXServer extends AbstractServer<IPhosphoNetXServer> implements
        IPhosphoNetXServer
{
    @Resource(name = ResourceNames.PROTEOMICS_DAO_FACTORY)
    private IPhosphoNetXDAOFactory specificDAOFactory;

    @Resource(name = ResourceNames.PROTEOMICS_BO_FACTORY)
    private IBusinessObjectFactory specificBOFactory;

    public PhosphoNetXServer()
    {
        super();
    }

    @Private
    PhosphoNetXServer(IOpenBisSessionManager sessionManager, IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager,
            IPhosphoNetXDAOFactory specificDAOFactory, IBusinessObjectFactory specificBOFactory,
            ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin)
    {
        super(sessionManager, daoFactory, propertiesBatchManager, sampleTypeSlaveServerPlugin,
                dataSetTypeSlaveServerPlugin);
        this.specificDAOFactory = specificDAOFactory;
        this.specificBOFactory = specificBOFactory;
    }

    @Override
    public IPhosphoNetXServer createLogger(IInvocationLoggerContext context)
    {
        return new PhosphoNetXServerLogger(getSessionManager(), context);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public Vocabulary getTreatmentTypeVocabulary(String sessionToken) throws UserFailureException
    {
        IVocabularyDAO vocabularyDAO = getDAOFactory().getVocabularyDAO();
        VocabularyPE vocabulary = vocabularyDAO.tryFindVocabularyByCode("TREATMENT_TYPE");
        return VocabularyTranslator.translate(vocabulary);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<AbundanceColumnDefinition> getAbundanceColumnDefinitionsForProteinByExperiment(
            String sessionToken, @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentID, String treatmentTypeOrNull)
            throws UserFailureException
    {
        Session session = getSession(sessionToken);
        ISampleProvider sampleProvider = specificBOFactory.createSampleProvider(session);
        sampleProvider.loadByExperimentID(experimentID);
        return getAbundanceColumnDefinitions(session, sampleProvider, experimentID,
                treatmentTypeOrNull);
    }

    private List<AbundanceColumnDefinition> getAbundanceColumnDefinitions(Session session,
            ISampleProvider sampleProvider, TechId experimentID, String treatmentTypeOrNull)
    {
        String experimentPermID = getExperimentPermIDFor(experimentID);
        IProteinQueryDAO dao = specificDAOFactory.getProteinQueryDAO(experimentID);
        DataSet<String> samplePermIDs =
                dao.listAbundanceRelatedSamplePermIDsByExperiment(experimentPermID);
        try
        {
            IAbundanceColumnDefinitionTable table =
                    specificBOFactory.createAbundanceColumnDefinitionTable(session);
            for (String samplePermID : samplePermIDs)
            {
                table.add(sampleProvider.getSample(samplePermID));
            }
            return table.getSortedAndAggregatedDefinitions(treatmentTypeOrNull);
        } finally
        {
            samplePermIDs.close();
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<ProteinInfo> listProteinsByExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId, double falseDiscoveryRate,
            AggregateFunction function,
            String treatmentTypeCode, boolean aggregateOnOriginal) throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        ISampleProvider sampleProvider = specificBOFactory.createSampleProvider(session);
        sampleProvider.loadByExperimentID(experimentId);
        List<AbundanceColumnDefinition> definitions =
                getAbundanceColumnDefinitions(session, sampleProvider, experimentId,
                        treatmentTypeCode);
        IProteinInfoTable table = specificBOFactory.createProteinInfoTable(session, sampleProvider);
        table.load(definitions, experimentId, falseDiscoveryRate, function, aggregateOnOriginal);
        return table.getProteinInfos();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<ProteinSummary> listProteinSummariesByExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId) throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        IProteinSummaryTable summaryTable = specificBOFactory.createProteinSummaryTable(session);
        summaryTable.load(experimentId);
        return summaryTable.getProteinSummaries();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public ProteinByExperiment getProteinByExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentID, TechId proteinReferenceID)
            throws UserFailureException
    {
        Session session = getSession(sessionToken);
        IProteinQueryDAO proteinQueryDAO = specificDAOFactory.getProteinQueryDAO(experimentID);
        ProteinByExperiment proteinByExperiment = new ProteinByExperiment();
        ProteinReference proteinReference =
                proteinQueryDAO.tryToGetProteinReference(proteinReferenceID.getId());
        if (proteinReference == null)
        {
            throw new UserFailureException("No protein reference found for ID: "
                    + proteinReferenceID);
        }
        proteinByExperiment.setId(proteinReferenceID);
        AccessionNumberBuilder builder =
                new AccessionNumberBuilder(proteinReference.getAccessionNumber());
        proteinByExperiment.setAccessionNumber(builder.getAccessionNumber());
        proteinByExperiment.setAccessionNumberType(builder.getTypeOrNull());
        proteinByExperiment.setDescription(proteinReference.getDescription());
        IProteinDetailsBO proteinDetailsBO = specificBOFactory.createProteinDetailsBO(session);
        proteinDetailsBO.loadByExperimentAndReference(experimentID, proteinReferenceID);
        proteinByExperiment.setDetails(proteinDetailsBO.getDetailsOrNull());
        return proteinByExperiment;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<ProteinSequence> listProteinSequencesByProteinReference(String sessionToken,
            TechId experimentID, TechId proteinReferenceID) throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        IProteinSequenceTable sequenceTable = specificBOFactory.createProteinSequenceTable(session);
        sequenceTable.loadByReference(experimentID, proteinReferenceID);
        return sequenceTable.getSequences();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<DataSetProtein> listProteinsByExperimentAndReference(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId, TechId proteinReferenceID)
            throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        IProteinSequenceTable sequenceTable = specificBOFactory.createProteinSequenceTable(session);
        sequenceTable.loadByReference(experimentId, proteinReferenceID);
        IDataSetProteinTable dataSetProteinTable =
                specificBOFactory.createDataSetProteinTable(session);
        dataSetProteinTable.load(getExperimentPermIDFor(experimentId), proteinReferenceID,
                sequenceTable);
        return dataSetProteinTable.getDataSetProteins();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<ProteinRelatedSample> listProteinRelatedSamplesByProtein(String sessionToken,
            TechId experimentID, TechId proteinReferenceID) throws UserFailureException
    {
        final Session session = getSession(sessionToken);

        IProteinDetailsBO proteinDetailsBO = specificBOFactory.createProteinDetailsBO(session);
        proteinDetailsBO.loadByExperimentAndReference(experimentID, proteinReferenceID);
        ProteinDetails detailsOrNull = proteinDetailsBO.getDetailsOrNull();
        String sequenceOrNull = detailsOrNull == null ? null : detailsOrNull.getSequence();
        IProteinRelatedSampleTable proteinRelatedSampleTable =
                specificBOFactory.createProteinRelatedSampleTable(session);
        proteinRelatedSampleTable.load(experimentID, proteinReferenceID, sequenceOrNull);
        return proteinRelatedSampleTable.getSamples();
    }

    private String getExperimentPermIDFor(TechId experimentId)
    {
        ExperimentPE experiment = getDAOFactory().getExperimentDAO().getByTechId(experimentId);
        return experiment.getPermId();
    }

}
