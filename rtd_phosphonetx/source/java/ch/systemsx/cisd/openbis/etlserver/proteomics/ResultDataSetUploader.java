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

package ch.systemsx.cisd.openbis.etlserver.proteomics;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.AminoAcidMass;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.AnnotatedProtein;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.DataSet;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.Database;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.Experiment;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.Parameter;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.Peptide;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.PeptideModification;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.Protein;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.ProteinAnnotation;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.ProteinGroup;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.ProteinProphetDetails;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.ProteinReference;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.ProteinSummary;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.ProteinSummaryDataFilter;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.Sample;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.Sequence;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.ProbabilityToFDRCalculator;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.Occurrence;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.OccurrenceUtil;
import net.lemnik.eodsql.QueryTool;

/**
 * @author Franz-Josef Elmer
 */
class ResultDataSetUploader extends AbstractHandler
{
    private static final double MAX_FALSE_DISCOVERY_RATE = 0.1;

    static final String PARAMETER_TYPE_ABUNDANCE = "abundance";

    static final String PARAMETER_TYPE_MODIFICATION = "modification";

    private final Connection connection;

    private final IEncapsulatedOpenBISService openbisService;

    private final StringBuffer errorMessages;

    private final boolean assumingExtendedProtXML;

    private final String delimiter;

    private final boolean restrictedSampleResolving;

    ResultDataSetUploader(Connection connection, IEncapsulatedOpenBISService openbisService,
            boolean assumingExtendedProtXML, String delimiter,
            boolean restrictedSampleResolving)
    {
        this(QueryTool.getQuery(connection, IProtDAO.class), connection, openbisService,
                assumingExtendedProtXML, delimiter, restrictedSampleResolving);
    }

    ResultDataSetUploader(IProtDAO dao, Connection connection,
            IEncapsulatedOpenBISService openbisService, boolean assumingExtendedProtXML,
            String delimiter, boolean restrictedSampleResolving)
    {
        super(dao);
        this.connection = connection;
        this.openbisService = openbisService;
        this.assumingExtendedProtXML = assumingExtendedProtXML;
        this.delimiter = delimiter;
        this.restrictedSampleResolving = restrictedSampleResolving;
        this.errorMessages = new StringBuffer();
    }

    void upload(DataSetInformation dataSetInfo, ProteinSummary summary)
    {
        try
        {
            Experiment experiment =
                    getOrCreateExperiment(dataSetInfo.tryToGetExperiment().getPermId());
            ExperimentIdentifier experimentIdentifier = dataSetInfo.getExperimentIdentifier();
            String referenceDatabase = summary.getSummaryHeader().getReferenceDatabase();
            Database database = getOrGreateDatabase(referenceDatabase);
            DataSet ds =
                    getOrCreateDataSet(experiment, database, dataSetInfo.getDataSetCode());
            addToDatabase(ds, experiment, experimentIdentifier, summary);
        } catch (Throwable throwable)
        {
            try
            {
                connection.rollback();
            } catch (SQLException ex)
            {
            }
            throw CheckedExceptionTunnel.wrapIfNecessary(throwable);
        }
        if (errorMessages.length() != 0)
        {
            rollback();
            throw UserFailureException.fromTemplate(
                    "Following errors occurred while uploading protein information"
                            + " to the dataset database from the dataset '%s': %s" + " ",
                    dataSetInfo.getDataSetCode(), errorMessages.toString());
        }
    }

    /** the uploader should not be used after calling this method */
    public void rollback()
    {
        try
        {
            if (connection.isClosed() == false)
            {
                connection.rollback();
                connection.close();
            }
        } catch (SQLException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    /** the uploader should not be used after calling this method */
    public void commit()
    {
        try
        {
            connection.commit();
            connection.close();
        } catch (SQLException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
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

    private DataSet getOrCreateDataSet(Experiment experiment, Database database,
            String dataSetPermID)
    {
        DataSet dataSet = dao.tryToGetDataSetByPermID(dataSetPermID);
        if (dataSet == null)
        {
            dataSet = new DataSet();
            dataSet.setPermID(dataSetPermID);
            long experimentID = experiment.getId();
            dataSet.setExperimentID(experimentID);
            long databaseID = database.getId();
            dataSet.setDatabaseID(databaseID);
            dataSet.setId(dao.createDataSet(experimentID, dataSetPermID, databaseID));
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

    private void addToDatabase(DataSet dataSet, Experiment experiment,
            ExperimentIdentifier experimentIdentifier, ProteinSummary summary)
    {
        long dataSetID = dataSet.getId();
        Long databaseID = dataSet.getDatabaseID();
        AbundanceHandler abundanceHandler =
                new AbundanceHandler(openbisService, dao, experimentIdentifier, experiment,
                        delimiter, restrictedSampleResolving);
        ModificationFractionHandler modificationFractionHandler =
                new ModificationFractionHandler(openbisService, dao, experimentIdentifier,
                        experiment, delimiter, restrictedSampleResolving);
        ProbabilityToFDRCalculator calculator = createProbabilityToFDRMapping(dataSetID, summary);
        List<ProteinGroup> proteinGroups = summary.getProteinGroups();
        for (ProteinGroup proteinGroup : proteinGroups)
        {
            for (Protein protein : proteinGroup.getProteins())
            {
                try
                {
                    double fdr = calculator.calculateFDR(protein.getProbability());
                    if (Double.isNaN(fdr) || fdr <= MAX_FALSE_DISCOVERY_RATE)
                    {
                        addProtein(protein, dataSetID, databaseID, abundanceHandler,
                                modificationFractionHandler);
                    }
                } catch (Exception e)
                {
                    logException(e, "protein", protein.getName());
                }
            }
        }
    }

    private void logException(Exception e, String objectType, String instanceDescription)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Cannot load following '");
        sb.append(objectType);
        sb.append("': ");
        sb.append(instanceDescription);
        sb.append(" because of the following exception: ");
        String message = e.getMessage();
        sb.append(message == null ? e.toString() : message);
        sb.append("\n");
        errorMessages.append(sb.toString());
    }

    private void addProtein(Protein protein, long dataSetID, Long databaseID,
            AbundanceHandler abundanceHandler,
            ModificationFractionHandler modificationFractionHandler)
    {
        long proteinID = dao.createProtein(dataSetID, protein.getProbability());
        for (Parameter parameter : protein.getParameters())
        {
            if (PARAMETER_TYPE_ABUNDANCE.equals(parameter.getType()))
            {
                abundanceHandler.addAbundancesToDatabase(parameter, proteinID, protein.getName());
            }
        }
        List<Peptide> peptides = protein.getPeptides();
        Set<String> peptideSequences = new HashSet<String>();
        for (Peptide peptide : peptides)
        {
            try
            {
                addPeptide(proteinID, peptide, modificationFractionHandler);
                peptideSequences.add(peptide.getSequence());
            } catch (Exception e)
            {
                logException(e, "peptide", peptide.getSequence().toString());
            }
        }
        createIdentifiedProtein(proteinID, peptideSequences, databaseID, protein.getAnnotation(), true);
        for (AnnotatedProtein annotatedProtein : protein.getIndistinguishableProteins())
        {
            createIdentifiedProtein(proteinID, peptideSequences, databaseID, annotatedProtein.getAnnotation(), false);
        }
    }

    private void addPeptide(long proteinID, Peptide peptide,
            ModificationFractionHandler modificationFractionHandler)
    {
        String peptideSequence = peptide.getSequence();
        int charge = peptide.getCharge();
        long peptideID = dao.createPeptide(proteinID, peptideSequence, charge);
        List<PeptideModification> modifications = peptide.getModifications();
        for (PeptideModification modification : modifications)
        {
            try
            {
                addPeptideModification(peptideID, modification);
            } catch (Exception e)
            {
                logException(e, "modification", modification.toString());
            }
        }
        List<ModificationFraction> modificationFractions = extractModificationFractions(peptide);
        if (modificationFractions.isEmpty())
        {
            return;
        }
        long modPeptideID = dao.createModifiedPeptide(peptideID, 0, 0);
        Map<AminoAcidMass, List<ModificationFraction>> map =
                groupByPositionAndMass(modificationFractions);
        Set<Entry<AminoAcidMass, List<ModificationFraction>>> entrySet = map.entrySet();
        for (Entry<AminoAcidMass, List<ModificationFraction>> entry : entrySet)
        {
            AminoAcidMass positionAndMass = entry.getKey();
            long modID = createModification(modPeptideID, positionAndMass);
            List<ModificationFraction> list = entry.getValue();
            modificationFractionHandler.addModificationFractions(peptideSequence, modID, list);
        }
    }

    private Map<AminoAcidMass, List<ModificationFraction>> groupByPositionAndMass(
            List<ModificationFraction> modificationFractions)
    {
        Map<AminoAcidMass, List<ModificationFraction>> result =
                new HashMap<AminoAcidMass, List<ModificationFraction>>();
        for (ModificationFraction modificationFraction : modificationFractions)
        {
            AminoAcidMass positionAndMass = modificationFraction.getAminoAcidMass();
            List<ModificationFraction> list = result.get(positionAndMass);
            if (list == null)
            {
                list = new ArrayList<ModificationFraction>();
                result.put(positionAndMass, list);
            }
            list.add(modificationFraction);
        }
        return result;
    }

    private List<ModificationFraction> extractModificationFractions(Peptide peptide)
    {
        List<ModificationFraction> result = new ArrayList<ModificationFraction>();
        for (Parameter parameter : peptide.getParameters())
        {
            if (PARAMETER_TYPE_MODIFICATION.equals(parameter.getType()))
            {
                result.add(new ModificationFraction(parameter.getName(), parameter.getValue()));
            }
        }
        return result;
    }

    private void addPeptideModification(long peptideID, PeptideModification modification)
    {
        double ntermMass = modification.getNTermMass();
        double ctermMass = modification.getCTermMass();
        long modPeptideID = dao.createModifiedPeptide(peptideID, ntermMass, ctermMass);
        List<AminoAcidMass> aminoAcidMasses = modification.getAminoAcidMasses();
        for (AminoAcidMass aminoAcidMass : aminoAcidMasses)
        {
            createModification(modPeptideID, aminoAcidMass);
        }
    }

    private long createModification(long modPeptideID, AminoAcidMass aminoAcidMass)
    {
        return dao.createModification(modPeptideID, aminoAcidMass.getPosition(),
                aminoAcidMass.getMass());
    }

    private void createIdentifiedProtein(long proteinID, Set<String> peptideSequences,
            Long databaseID, ProteinAnnotation annotation, boolean primary)
    {
        ProteinDescription protDesc =
                new ProteinDescription(annotation, proteinID, assumingExtendedProtXML);
        String accessionNumber = protDesc.getAccessionNumber();
        String description = protDesc.getDescription();
        ProteinReference proteinReference = dao.tryToGetProteinReference(accessionNumber);
        if (proteinReference == null)
        {
            proteinReference = new ProteinReference();
            proteinReference.setId(dao.createProteinReference(accessionNumber, description));
        } else if (StringUtils.equals(description, proteinReference.getDescription()) == false)
        {
            dao.updateProteinReferenceDescription(proteinReference.getId(), description);
        }
        Sequence sequence =
                tryFindSequence(proteinReference.getId(), databaseID, protDesc.getSequence());
        if (sequence == null)
        {
            sequence = new Sequence(protDesc.getSequence());
            sequence.setDatabaseID(databaseID);
            sequence.setProteinReferenceID(proteinReference.getId());
            sequence.setId(dao.createSequence(sequence));
        }
        double coverage = calculateCoverage(sequence.getSequence(), peptideSequences);
        dao.createIdentifiedProtein(proteinID, sequence.getId(), coverage, primary);
    }

    private double calculateCoverage(String aminoAcidSequence, Set<String> peptideSequences)
    {
        List<Occurrence> list = OccurrenceUtil.getCoverage(aminoAcidSequence, peptideSequences);
        int sumPeptides = 0;
        for (Occurrence occurrence : list)
        {
            sumPeptides += occurrence.getWord().length();
        }
        return sumPeptides / (double) aminoAcidSequence.length();
    }

    private Sequence tryFindSequence(long referenceID, Long databaseID, String sequence)
    {
        List<Sequence> sequences =
                dao.tryToGetSequencesByReferenceAndDatabase(referenceID, databaseID);
        if (sequences == null || sequences.isEmpty())
        {
            return null;
        }
        for (Sequence foundSequence : sequences)
        {
            if (sequence.equals(foundSequence.getSequence()))
            {
                return foundSequence;
            }
        }
        return null;
    }

    private ProbabilityToFDRCalculator createProbabilityToFDRMapping(long dataSetID, ProteinSummary summary)
    {
        ProbabilityToFDRCalculator calculator = new ProbabilityToFDRCalculator();
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
                        calculator.add(probability, fdr);
                        calculator.init();
                        dao.createProbabilityToFDRMapping(dataSetID, probability, fdr);
                    }
                    return calculator;
                }
            }
        }
        if (assumingExtendedProtXML == false)
        {
            return calculator;
        }
        throw new UserFailureException("Missing Protein Prophet details.");
    }

    protected Sample getOrCreateSample(Experiment experiment, String samplePermID)
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
}
