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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import net.lemnik.eodsql.DataSet;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.translator.EntityPropertyTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.VocabularyTranslator;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.AccessionNumberBuilder;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.IAbundanceColumnDefinitionTable;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.IBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.IDataSetProteinTable;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.IProteinDetailsBO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.IProteinInfoTable;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.IProteinSequenceTable;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.IProteinSummaryTable;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.ISampleProvider;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.SampleIDProvider;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.DataSetProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.Occurrence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.OccurrenceUtil;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinByExperiment;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinDetails;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinRelatedSample;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSequence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSummary;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.AbstractSample;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReference;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.SampleAbundance;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.SamplePeptideModification;

/**
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.PHOSPHONETX_PLUGIN_SERVER)
public class PhosphoNetXServer extends AbstractServer<IPhosphoNetXServer> implements
        IPhosphoNetXServer
{
    @Resource(name = ResourceNames.PHOSPHONETX_DAO_FACTORY)
    private IPhosphoNetXDAOFactory specificDAOFactory;

    @Resource(name = ResourceNames.PHOSPHONETX_BO_FACTORY)
    private IBusinessObjectFactory specificBOFactory;
    
    public PhosphoNetXServer()
    {
        super();
    }

    @Private
    PhosphoNetXServer(ISessionManager<Session> sessionManager, IDAOFactory daoFactory,
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

    public IPhosphoNetXServer createLogger(IInvocationLoggerContext context)
    {
        return new PhosphoNetXServerLogger(getSessionManager(), context);
    }

    public Vocabulary getTreatmentTypeVocabulary(String sessionToken) throws UserFailureException
    {
        IVocabularyDAO vocabularyDAO = getDAOFactory().getVocabularyDAO();
        VocabularyPE vocabulary = vocabularyDAO.tryFindVocabularyByCode("TREATMENT_TYPE");
        return VocabularyTranslator.translate(vocabulary);
    }

    public List<AbundanceColumnDefinition> getAbundanceColumnDefinitionsForProteinByExperiment(
            String sessionToken, TechId experimentID, String treatmentTypeOrNull)
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
        IProteinQueryDAO dao = specificDAOFactory.getProteinQueryDAO();
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

    public List<ProteinInfo> listProteinsByExperiment(String sessionToken, TechId experimentId,
            double falseDiscoveryRate, AggregateFunction function, String treatmentTypeCode,
            boolean aggregateOnOriginal) throws UserFailureException
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

    public List<ProteinSummary> listProteinSummariesByExperiment(String sessionToken,
            TechId experimentId) throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        IProteinSummaryTable summaryTable = specificBOFactory.createProteinSummaryTable(session);
        summaryTable.load(experimentId);
        return summaryTable.getProteinSummaries();
    }

    public ProteinByExperiment getProteinByExperiment(String sessionToken, TechId experimentID,
            TechId proteinReferenceID) throws UserFailureException
    {
        Session session = getSession(sessionToken);
        IProteinQueryDAO proteinQueryDAO = specificDAOFactory.getProteinQueryDAO();
        ProteinByExperiment proteinByExperiment = new ProteinByExperiment();
        ProteinReference proteinReference =
                proteinQueryDAO.tryToGetProteinReference(proteinReferenceID.getId());
        if (proteinReference == null)
        {
            throw new UserFailureException("No protein reference found for ID: " + proteinReferenceID);
        }
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

    public List<ProteinSequence> listProteinSequencesByProteinReference(String sessionToken,
            TechId proteinReferenceID) throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        IProteinSequenceTable sequenceTable = specificBOFactory.createProteinSequenceTable(session);
        sequenceTable.loadByReference(proteinReferenceID);
        return sequenceTable.getSequences();
    }

    public List<DataSetProtein> listProteinsByExperimentAndReference(String sessionToken,
            TechId experimentId, TechId proteinReferenceID) throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        IProteinSequenceTable sequenceTable = specificBOFactory.createProteinSequenceTable(session);
        sequenceTable.loadByReference(proteinReferenceID);
        IDataSetProteinTable dataSetProteinTable = specificBOFactory.createDataSetProteinTable(session);
        dataSetProteinTable.load(getExperimentPermIDFor(experimentId), proteinReferenceID, sequenceTable);
        return dataSetProteinTable.getDataSetProteins();
    }

    public List<ProteinRelatedSample> listProteinRelatedSamplesByProtein(
            String sessionToken, TechId experimentID, TechId proteinReferenceID)
            throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        
        IProteinDetailsBO proteinDetailsBO = specificBOFactory.createProteinDetailsBO(session);
        proteinDetailsBO.loadByExperimentAndReference(experimentID, proteinReferenceID);
        ProteinDetails detailsOrNull = proteinDetailsBO.getDetailsOrNull();
        String sequenceOrNull = detailsOrNull == null ? null : detailsOrNull.getSequence();
        IProteinQueryDAO proteinQueryDAO = specificDAOFactory.getProteinQueryDAO();
        proteinQueryDAO.listProteinSequencesByProteinReference(proteinReferenceID.getId());
        IDAOFactory daoFactory = getDAOFactory();
        String experimentPermID =
                daoFactory.getExperimentDAO().getByTechId(experimentID).getPermId();
        Map<String, List<SampleAbundance>> sampleAbundanceMap =
                createSampleMap(proteinQueryDAO.listSampleAbundanceByProtein(experimentPermID,
                        proteinReferenceID.getId()));
        Map<String, List<SamplePeptideModification>> samplePeptideModificationMap =
                createSampleMap(proteinQueryDAO.listSamplePeptideModificatioByProtein(
                        experimentPermID, proteinReferenceID.getId()));
        List<ProteinRelatedSample> result = new ArrayList<ProteinRelatedSample>();
        SampleIDProvider sampleIDProvider = new SampleIDProvider(daoFactory.getSampleDAO());
        Map<PropertyTypePE, PropertyType> cache = new HashMap<PropertyTypePE, PropertyType>();
        for (Entry<String, List<SampleAbundance>> entry : sampleAbundanceMap.entrySet())
        {
            String key = entry.getKey();
            SamplePE sample = sampleIDProvider.getSampleOrParentSample(key);
            List<SampleAbundance> sampleAbundances = entry.getValue();
            List<SamplePeptideModification> samplePeptideModifications =
                    samplePeptideModificationMap.get(key);
            if (samplePeptideModifications == null)
            {
                for (SampleAbundance sampleAbundance : sampleAbundances)
                {
                    ProteinRelatedSample s = createFrom(sample, cache);
                    s.setAbundance(sampleAbundance.getAbundance());
                    result.add(s);
                }
            } else
            {
                for (SampleAbundance sampleAbundance : sampleAbundances)
                {
                    Double abundance = sampleAbundance.getAbundance();
                    result.addAll(createSamplesForPeptideModifications(samplePeptideModifications,
                            sample, abundance, sequenceOrNull, cache));
                }
            }
        }
        for (Entry<String, List<SamplePeptideModification>> entry : samplePeptideModificationMap
                .entrySet())
        {
            String key = entry.getKey();
            if (sampleAbundanceMap.containsKey(key) == false)
            {
                SamplePE sample = sampleIDProvider.getSampleOrParentSample(key);
                List<SamplePeptideModification> samplePeptideModifications = entry.getValue();
                result.addAll(createSamplesForPeptideModifications(samplePeptideModifications,
                        sample, null, sequenceOrNull, cache));
            }
        }
        return result;
    }

    private List<ProteinRelatedSample> createSamplesForPeptideModifications(
            List<SamplePeptideModification> samplePeptideModifications, SamplePE sample,
            Double abundanceOrNull, String sequenceOrNull, Map<PropertyTypePE, PropertyType> cache)
    {
        List<ProteinRelatedSample> samples = new ArrayList<ProteinRelatedSample>();
        for (SamplePeptideModification samplePeptideModification : samplePeptideModifications)
        {
            int position = samplePeptideModification.getPosition();
            if (sequenceOrNull != null)
            {
                List<Occurrence> occurances =
                        OccurrenceUtil.findAllOccurrences(sequenceOrNull,
                                samplePeptideModification.getSequence());
                for (Occurrence occurrence : occurances)
                {
                    samples.add(createProteinRelatedSample(samplePeptideModification, sample,
                            abundanceOrNull, position + occurrence.getStartIndex(), cache));
                }
            } else
            {
                samples.add(createProteinRelatedSample(samplePeptideModification, sample,
                        abundanceOrNull, position, cache));
            }
        }
        return samples;
    }

    private ProteinRelatedSample createProteinRelatedSample(
            SamplePeptideModification samplePeptideModification, SamplePE sample,
            Double abundanceOrNull, int position, Map<PropertyTypePE, PropertyType> cache)
    {
        ProteinRelatedSample s = createFrom(sample, cache);
        s.setAbundance(abundanceOrNull);
        int index = samplePeptideModification.getPosition() - 1;
        String sequence = samplePeptideModification.getSequence();
        if (index >= 0 && index < sequence.length())
        {
            s.setModifiedAminoAcid(sequence.charAt(index));
        }
        s.setModificationFraction(samplePeptideModification.getFraction());
        s.setModificationMass(samplePeptideModification.getMass());
        s.setModificationPosition((long) position);
        return s;
    }
    
    private ProteinRelatedSample createFrom(SamplePE sample, Map<PropertyTypePE, PropertyType> cache)
    {
        ProteinRelatedSample s = new ProteinRelatedSample();
        s.setCode(sample.getCode());
        s.setEntityType(SampleTypeTranslator.translate(sample.getSampleType(), cache));
        s.setId(sample.getId());
        s.setIdentifier(sample.getIdentifier());
        s.setPermId(sample.getPermId());
        s.setProperties(EntityPropertyTranslator.translate(sample.getProperties(), cache));
        return s;
    }

    private <T extends AbstractSample> Map<String, List<T>> createSampleMap(DataSet<T> items)
    {
        Map<String, List<T>> map = new HashMap<String, List<T>>();
        try
        {
            for (T item : items)
            {
                String samplePermID = item.getSamplePermID();
                List<T> list = map.get(samplePermID);
                if (list == null)
                {
                    list = new ArrayList<T>();
                    map.put(samplePermID, list);
                }
                list.add(item);
            }
        } finally
        {
            items.close();
        }
        return map;
    }

    private String getExperimentPermIDFor(TechId experimentId)
    {
        ExperimentPE experiment = getDAOFactory().getExperimentDAO().getByTechId(experimentId);
        return experiment.getPermId();
    }

}
