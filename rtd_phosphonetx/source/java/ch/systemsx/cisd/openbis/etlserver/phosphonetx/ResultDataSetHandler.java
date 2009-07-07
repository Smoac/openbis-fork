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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.AminoAcidMass;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.AnnotatedProtein;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.DataSet;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Database;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Experiment;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Parameter;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Peptide;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.PeptideModification;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Protein;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinAnnotation;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinGroup;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinProphetDetails;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinReference;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinSummary;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinSummaryDataFilter;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Sample;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Sequence;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ResultDataSetHandler extends AbstractHandler implements IDataSetHandler
{
    private static final String PARAMETER_TYPE_ABUNDANCE = "abundance";
    private static final String DATABASE_ENGINE = "database.engine";
    private static final String DATABASE_URL_HOST_PART = "database.url-host-part";
    private static final String DATABASE_BASIC_NAME = "database.basic-name";
    private static final String DATABASE_KIND = "database.kind";
    private static final String DATABASE_OWNER = "database.owner";
    private static final String DATABASE_PASSWORD = "database.password";
    
    private static final class ProteinDescription
    {
        private final String uniprotID;
        private final String description;
        private final String sequence;

        public ProteinDescription(String proteinDescription)
        {
            String[] items = proteinDescription.split("\\\\");
            uniprotID = tryToGetUniprotID(items);
            description = tryToGetValue(items, "DE");
            sequence = tryToGetValue(items, "SEQ");
        }
        
        public final String getUniprotID()
        {
            return uniprotID;
        }

        public final String getDescription()
        {
            return description;
        }

        public final String getSequence()
        {
            return sequence;
        }

        private String tryToGetUniprotID(String[] items)
        {
            return items == null || items.length == 0 ? null : items[0].trim(); 
        }
        
        private String tryToGetValue(String[] items, String key)
        {
            for (String item : items)
            {
                int indexOfEqualSign = item.indexOf('=');
                if (indexOfEqualSign > 0
                        && item.substring(0, indexOfEqualSign).trim().equalsIgnoreCase(key))
                {
                    return item.substring(indexOfEqualSign + 1).trim();
                }
            }
            return null;
        }
    }
    
    private static Connection createDatabaseConnection(Properties properties)
    {
        DatabaseConfigurationContext context = new DatabaseConfigurationContext();
        context.setDatabaseEngineCode(properties.getProperty(DATABASE_ENGINE, "postgresql"));
        context.setUrlHostPart(properties.getProperty(DATABASE_URL_HOST_PART, ""));
        context.setBasicDatabaseName(properties.getProperty(DATABASE_BASIC_NAME, "phosphonetx"));
        context.setDatabaseKind(PropertyUtils.getMandatoryProperty(properties, DATABASE_KIND));
        context.setOwner(properties.getProperty(DATABASE_OWNER, ""));
        context.setPassword(properties.getProperty(DATABASE_PASSWORD, ""));
        try
        {
            Connection connection = context.getDataSource().getConnection();
            return connection;
        } catch (SQLException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }
    
    private final IDataSetHandler delegator;
    private final Unmarshaller unmarshaller;
    private final IEncapsulatedOpenBISService openbisService;

    public ResultDataSetHandler(Properties properties, IDataSetHandler delegator,
            IEncapsulatedOpenBISService openbisService)
    {
        super(QueryTool.getQuery(createDatabaseConnection(ExtendedProperties.getSubset(properties,
                IDataSetHandler.DATASET_HANDLER_KEY + '.', true)), IProtDAO.class));
        this.delegator = delegator;
        this.openbisService = openbisService;
        try
        {
            JAXBContext context =
                    JAXBContext.newInstance(ProteinSummary.class, ProteinProphetDetails.class);
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public List<DataSetInformation> handleDataSet(File dataSet)
    {
        ProteinSummary summary = readProtXML(dataSet);
        List<DataSetInformation> dataSets = delegator.handleDataSet(dataSet);
        if (dataSets.isEmpty())
        {
            throw new ConfigurationFailureException(
                    "Data set not registered due to some error. See error folder in data store.");
        }
        if (dataSets.size() != 1)
        {
            throw new ConfigurationFailureException(
                    dataSets.size() + " data set registered: " +
                    "Only data set handlers (like the primary one) " +
                    "registering exactly one data set are allowed.");
        }
        DataSetInformation dataSetInfo = dataSets.get(0);
        Experiment experiment = getOrCreateExperiment(dataSetInfo.getExperiment().getPermId());
        Sample sample = getOrCreateSample(experiment, dataSetInfo.getSample().getPermId());
        String referenceDatabase = summary.getSummaryHeader().getReferenceDatabase();
        Database database = getOrGreateDatabase(referenceDatabase);
        DataSet ds = getOrCreateDataSet(experiment, sample, database, dataSetInfo.getDataSetCode());
        addToDatabase(ds, experiment, dataSetInfo.getSample().getGroup(), summary);
        return dataSets;
    }

    private Database getOrGreateDatabase(String databaseNameAndVersion)
    {
        int indexOfLastSlash = databaseNameAndVersion.lastIndexOf('/');
        String nameOrVersion;
        if (indexOfLastSlash < 0)
        {
            nameOrVersion = databaseNameAndVersion;
        } else
        {
            nameOrVersion = databaseNameAndVersion.substring(indexOfLastSlash + 1);
        }
        Database database = dao.tryToGetDatabaseByName(nameOrVersion);
        if (database == null)
        {
            database = new Database();
            database.setNameAndVersion(nameOrVersion);
            database.setId(dao.createDatabase(database.getNameAndVersion()));
        }
        return database;
    }

    private DataSet getOrCreateDataSet(Experiment experiment, Sample sample, Database database,
            String dataSetPermID)
    {
        DataSet dataSet = dao.tryToGetDataSetByPermID(dataSetPermID);
        if (dataSet == null)
        {
            dataSet = new DataSet();
            dataSet.setPermID(dataSetPermID);
            long experimentID = experiment.getId();
            dataSet.setExperimentID(experimentID);
            long sampleID = sample.getId();
            dataSet.setSampleID(sampleID);
            long databaseID = database.getId();
            dataSet.setDatabaseID(databaseID);
            dataSet.setId(dao.createDataSet(experimentID, sampleID, dataSetPermID, databaseID));
        }
        return dataSet;
    }

    private Experiment getOrCreateExperiment(String experimentPermID)
    {
        Experiment experiment = dao.tryToGetExperimentByPermID(experimentPermID);
        if (experiment == null)
        {
            experiment = new Experiment();
            experiment.setPermID(experimentPermID);
            experiment.setId(dao.createExperiment(experimentPermID));
        }
        return experiment;
    }

    private ProteinSummary readProtXML(File dataSet)
    {
        try
        {
            Object object = unmarshaller.unmarshal(dataSet);
            if (object instanceof ProteinSummary == false)
            {
                throw new IllegalArgumentException("Wrong type: " + object);
            }
            ProteinSummary summary = (ProteinSummary) object;
            return summary;
        } catch (JAXBException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private void addToDatabase(DataSet dataSet, Experiment experiment, GroupPE group,
            ProteinSummary summary)
    {
        long dataSetID = dataSet.getId();
        Long databaseID = dataSet.getDatabaseID();
        GroupIdentifier groupIdentifier =
                new GroupIdentifier(group.getDatabaseInstance().getCode(), group.getCode());
        AbundanceHandler abundanceHandler =
                new AbundanceHandler(openbisService, dao, groupIdentifier, experiment);
        ModificationHandler modificationHandler = new ModificationHandler(dao);
        createProbabilityToFDRMapping(dataSetID, summary);
        List<ProteinGroup> proteinGroups = summary.getProteinGroups();
        for (ProteinGroup proteinGroup : proteinGroups)
        {
            double probability = proteinGroup.getProbability();
            List<Protein> proteins = proteinGroup.getProteins();
            if (proteins.isEmpty() == false)
            {
                // Only the first protein of a ProteinGroup is valid
                addProtein(proteins.get(0), probability, dataSetID, databaseID, abundanceHandler,
                        modificationHandler);
            }
        }
    }

    private void addProtein(Protein protein, double probability, long dataSetID, Long databaseID,
            AbundanceHandler abundanceHandler, ModificationHandler modificationHandler)
    {
        long proteinID = dao.createProtein(dataSetID, probability);
        for (Parameter parameter : protein.getParameters())
        {
            if (PARAMETER_TYPE_ABUNDANCE.equals(parameter.getType()))
            {
                abundanceHandler.addAbundancesToDatabase(parameter, proteinID, protein.getName());
            }
        }
        createIdentifiedProtein(proteinID, databaseID, protein.getAnnotation());
        for (AnnotatedProtein annotatedProtein : protein.getIndistinguishableProteins())
        {
            createIdentifiedProtein(proteinID, databaseID, annotatedProtein.getAnnotation());
        }
        List<Peptide> peptides = protein.getPeptides();
        for (Peptide peptide : peptides)
        {
            String peptideSequence = peptide.getSequence();
            int charge = peptide.getCharge();
            long peptideID = dao.createPeptide(proteinID, peptideSequence, charge);
            List<PeptideModification> modifications = peptide.getModifications();
            for (PeptideModification modification : modifications)
            {
                List<AminoAcidMass> aminoAcidMasses = modification.getAminoAcidMasses();
                for (AminoAcidMass aminoAcidMass : aminoAcidMasses)
                {
                    modificationHandler.createModification(peptideID, peptideSequence, aminoAcidMass);
                }
            }
        }
    }

    private void createIdentifiedProtein(long proteinID, Long databaseID,
            ProteinAnnotation annotation)
    {
        ProteinDescription protDesc = new ProteinDescription(annotation.getDescription());
        String uniprotID = protDesc.getUniprotID();
        String description = protDesc.getDescription();
        ProteinReference proteinReference = dao.tryToGetProteinReference(uniprotID);
        if (proteinReference == null)
        {
            proteinReference = new ProteinReference();
            proteinReference.setUniprotID(uniprotID);
            proteinReference.setDescription(description);
            proteinReference.setId(dao.createProteinReference(uniprotID, description));
        } else if (description.equals(proteinReference.getDescription()) == false)
        {
            dao.updateProteinReferenceDescription(proteinReference.getId(), description);
        }
        Sequence sequence =
                dao.tryToGetSequenceByReferenceAndDatabase(proteinReference.getId(), databaseID);
        if (sequence == null || protDesc.getSequence().equals(sequence.getSequence()) == false)
        {
            sequence = new Sequence(protDesc.getSequence());
            sequence.setDatabaseID(databaseID);
            sequence.setProteinReferenceID(proteinReference.getId());
            sequence.setId(dao.createSequence(sequence));
        }
        dao.createIdentifiedProtein(proteinID, sequence.getId());
    }

    private void createProbabilityToFDRMapping(long dataSetID, ProteinSummary summary)
    {
        Object[] s = summary.getSummaryHeader().getProgramDetails().getSummary();
        if (s != null)
        {
            for (Object object : s)
            {
                if (object instanceof ProteinProphetDetails)
                {
                    ProteinProphetDetails details = (ProteinProphetDetails) object;
                    List<ProteinSummaryDataFilter> filters = details.getDataFilters();
                    for (ProteinSummaryDataFilter proteinSummaryDataFilter : filters)
                    {
                        double probability = proteinSummaryDataFilter.getMinProbability();
                        double fdr = proteinSummaryDataFilter.getFalsePositiveErrorRate();
                        dao.createProbabilityToFDRMapping(dataSetID, probability, fdr);
                    }
                    return;
                }
            }
        }
        throw new UserFailureException("Missing Protein Prophet details.");
    }
    
}
