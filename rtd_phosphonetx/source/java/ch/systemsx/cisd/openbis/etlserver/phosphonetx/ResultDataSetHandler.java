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
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Experiment;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ModificationType;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Peptide;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.PeptideModification;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Protein;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinAnnotation;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinGroup;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinProphetDetails;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinSummary;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinSummaryDataFilter;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Sample;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Sequence;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ResultDataSetHandler implements IDataSetHandler
{
    private static final String DATABASE_ENGINE = "database.engine";
    private static final String DATABASE_URL_HOST_PART = "database.url-host-part";
    private static final String DATABASE_BASIC_NAME = "database.basic-name";
    private static final String DATABASE_KIND = "database.kind";
    private static final String DATABASE_OWNER = "database.owner";
    private static final String DATABASE_PASSWORD = "database.password";
    
    private final IDataSetHandler delegator;
    private final Unmarshaller unmarshaller;
    private final IProtDAO dao;

    public ResultDataSetHandler(Properties properties, IDataSetHandler delegator,
            IEncapsulatedOpenBISService openbisService)
    {
        this.delegator = delegator;
        try
        {
            JAXBContext context =
                JAXBContext.newInstance(ProteinSummary.class, ProteinProphetDetails.class);
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        ExtendedProperties dataSetHandlerProperties =
                ExtendedProperties.getSubset(properties, IDataSetHandler.DATASET_HANDLER_KEY + '.',
                        true);
        Connection connection = createDatabaseConnection(dataSetHandlerProperties);
        dao = QueryTool.getQuery(connection, IProtDAO.class);
    }

    private Connection createDatabaseConnection(Properties properties)
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
        dataSetInfo.getSample().getPermId();
        Experiment experiment = getOrCreateExperiment(dataSetInfo.getExperiment().getPermId());
        Sample sample = getOrCreateSample(experiment, dataSetInfo.getSample().getPermId());
        DataSet ds = getOrCreateDataSet(experiment, sample, dataSetInfo.getDataSetCode());
        addToDatabase(ds, summary);
        return dataSets;
    }

    private DataSet getOrCreateDataSet(Experiment experiment, Sample sample, String dataSetPermID)
    {
        DataSet dataSet = dao.tryToGetDataSetByPermID(dataSetPermID);
        if (dataSet == null)
        {
            dataSet = new DataSet();
            dataSet.setPermID(dataSetPermID);
            long experimentID = experiment.getId();
            dataSet.setExperimentID(experimentID);
            dataSet.setId(dao.createDataSet(experimentID, sample.getId(), dataSetPermID));
        }
        return dataSet;
    }

    private Sample getOrCreateSample(Experiment experiment, String samplePermID)
    {
        Sample sample = dao.tryToGetSampleByPermID(samplePermID);
        if (sample == null)
        {
            sample = new Sample();
            sample.setPermID(samplePermID);
            sample.setId(dao.createSample(experiment.getId(), samplePermID));
        }
        return sample;
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

    private void addToDatabase(DataSet dataSet, ProteinSummary summary)
    {
        long dataSetID = dataSet.getId();
        createProbabilityToFDRMapping(dataSetID, summary);
        Iterable<ModificationType> modificationTypes = dao.listModificationTypes();
        List<ProteinGroup> proteinGroups = summary.getProteinGroups();
        int maxGroupSize = 0;
        String maxGroupName = null;
        for (ProteinGroup proteinGroup : proteinGroups)
        {
            double probability = proteinGroup.getProbability();
            List<Protein> proteins = proteinGroup.getProteins();
            if (maxGroupSize < proteins.size())
            {
                maxGroupSize = proteins.size();
                maxGroupName = proteinGroup.getGroupNumber();
            }
            for (Protein protein : proteins)
            {
                ProteinAnnotation annotation = protein.getAnnotation();
                long proteinID = dao.createProtein(dataSetID, probability);
                dao.createIdentifiedProtein(proteinID, null, annotation.getDescription());
                for (AnnotatedProtein annotatedProtein : protein.getIndistinguishableProteins())
                {
                    String description = annotatedProtein.getAnnotation().getDescription();
                    dao.createIdentifiedProtein(proteinID, null, description);
                }
                List<Peptide> peptides = protein.getPeptides();
                for (Peptide peptide : peptides)
                {
                    int charge = peptide.getCharge();
                    long peptideID = dao.createPeptide(proteinID, peptide.getSequence(), charge);
                    List<PeptideModification> modifications = peptide.getModifications();
                    for (PeptideModification modification : modifications)
                    {
                        List<AminoAcidMass> aminoAcidMasses = modification.getAminoAcidMasses();
                        for (AminoAcidMass aminoAcidMass : aminoAcidMasses)
                        {
                            double mass = aminoAcidMass.getMass();
                            ModificationType modificationType =
                                    findModificationType(modificationTypes, mass);
                            dao.createModification(peptideID, modificationType.getId(),
                                    aminoAcidMass.getPosition(), mass);
                        }
                    }
                }
            }
        }
        System.out.println("maximum group size: " + maxGroupSize + ", name:" + maxGroupName);
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

    private Sequence getOrCreateSequence(Peptide peptide)
    {
        String s = peptide.getSequence();
        Sequence sequence = dao.tryToGetSequenceBySequenceString(s);
        if (sequence == null)
        {
            sequence = new Sequence(s);
            long id = dao.createSequence(sequence);
            sequence.setId(id);
        }
        return sequence;
    }

    private ModificationType findModificationType(Iterable<ModificationType> modificationTypes,
            double mass)
    {
        ModificationType result = null;
        for (ModificationType modificationType : modificationTypes)
        {
            if (modificationType.matches(mass))
            {
                if (result == null || modificationType.getDeltaMass() < result.getDeltaMass())
                {
                    result = modificationType;
                }
            }
        }
        return result;
    }
}
