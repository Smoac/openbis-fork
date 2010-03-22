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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import net.lemnik.eodsql.QueryTool;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
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
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Sequence;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.ProbabilityToFDRCalculator;

/**
 * @author Franz-Josef Elmer
 */
class ResultDataSetUploader extends AbstractHandler
{
    private static final double MAX_FALSE_DISCOVERY_RATE = 0.1;

    static final String PARAMETER_TYPE_ABUNDANCE = "abundance";

    private final Connection connection;

    private final IEncapsulatedOpenBISService openbisService;

    private final StringBuffer errorMessages;

    ResultDataSetUploader(Connection connection, IEncapsulatedOpenBISService openbisService)
    {
        this(QueryTool.getQuery(connection, IProtDAO.class), connection, openbisService);
    }

    ResultDataSetUploader(IProtDAO dao, Connection connection,
            IEncapsulatedOpenBISService openbisService)
    {
        super(dao);
        this.connection = connection;
        this.openbisService = openbisService;
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

    public void rollback()
    {
        try
        {
            connection.rollback();
        } catch (SQLException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void commit()
    {
        try
        {
            connection.commit();
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
                new AbundanceHandler(openbisService, dao, experimentIdentifier, experiment);
        ProbabilityToFDRCalculator calculator = createProbabilityToFDRMapping(dataSetID, summary);
        List<ProteinGroup> proteinGroups = summary.getProteinGroups();
        for (ProteinGroup proteinGroup : proteinGroups)
        {
            List<Protein> proteins = proteinGroup.getProteins();
            if (proteins.isEmpty() == false)
            {
                // Only the first protein of a ProteinGroup is valid
                Protein protein = proteins.get(0);
                try
                {
                    if (calculator.calculateFDR(protein.getProbability()) <= MAX_FALSE_DISCOVERY_RATE)
                    {
                        addProtein(protein, dataSetID, databaseID, abundanceHandler);
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
            AbundanceHandler abundanceHandler)
    {
        long proteinID = dao.createProtein(dataSetID, protein.getProbability());
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
            try
            {
                addPeptide(proteinID, peptide);
            } catch (Exception e)
            {
                logException(e, "peptide", peptide.getSequence().toString());
            }
        }
    }

    private void addPeptide(long proteinID, Peptide peptide)
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
    }

    private void addPeptideModification(long peptideID, PeptideModification modification)
    {
        double ntermMass = modification.getNTermMass();
        double ctermMass = modification.getCTermMass();
        long modPeptideID = dao.createModifiedPeptide(peptideID, ntermMass, ctermMass);
        List<AminoAcidMass> aminoAcidMasses = modification.getAminoAcidMasses();
        for (AminoAcidMass aminoAcidMass : aminoAcidMasses)
        {
            double mass = aminoAcidMass.getMass();
            int position = aminoAcidMass.getPosition();
            dao.createModification(modPeptideID, position, mass);
        }
    }

    private void createIdentifiedProtein(long proteinID, Long databaseID,
            ProteinAnnotation annotation)
    {
        ProteinDescription protDesc = new ProteinDescription(annotation.getDescription());
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
        dao.createIdentifiedProtein(proteinID, sequence.getId());
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
                        dao.createProbabilityToFDRMapping(dataSetID, probability, fdr);
                    }
                    return calculator;
                }
            }
        }
        throw new UserFailureException("Missing Protein Prophet details.");
    }
}
