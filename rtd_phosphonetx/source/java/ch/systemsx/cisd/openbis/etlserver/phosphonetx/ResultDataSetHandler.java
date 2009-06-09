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
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.AminoAcidMass;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ModificationType;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Peptide;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.PeptideModification;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Protein;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinGroup;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinProphetDetails;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinSummary;
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
        Connection connection = createDatabaseConnection(properties);
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
        if (dataSets.size() != 1)
        {
            throw new ConfigurationFailureException(
                    "More than one data set registered: " +
                    "Only data set handlers (like the primary one) " +
                    "registering exactly one data set are allowed.");
        }
        addToDatabase(dataSets.get(0).getDataSetCode(), summary);
        return dataSets;
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

    private void addToDatabase(String dataSetCode,
            ProteinSummary summary)
    {
        Iterable<ModificationType> modificationTypes = dao.listModificationTypes();
        List<ProteinGroup> proteinGroups = summary.getProteinGroups();
        int maxGroupSize = 0;
        String maxGroupName = null;
        for (ProteinGroup proteinGroup : proteinGroups)
        {
            List<Protein> proteins = proteinGroup.getProteins();
            if (maxGroupSize < proteins.size())
            {
                maxGroupSize = proteins.size();
                maxGroupName = proteinGroup.getGroupNumber();
            }
            for (Protein protein : proteins)
            {
                long proteinID = dao.createProtein(dataSetCode);
                List<Peptide> peptides = protein.getPeptides();
                for (Peptide peptide : peptides)
                {
                    Sequence sequence = getOrCreateSequence(peptide);
                    long peptideID = dao.createPeptide(proteinID, sequence.getId());
                    int charge = peptide.getCharge();
                    List<PeptideModification> modifications = peptide.getModifications();
                    for (PeptideModification modification : modifications)
                    {
                        long modifiedPeptideID = dao.createModifiedPeptide(peptideID, charge);
                        List<AminoAcidMass> aminoAcidMasses = modification.getAminoAcidMasses();
                        for (AminoAcidMass aminoAcidMass : aminoAcidMasses)
                        {
                            double mass = aminoAcidMass.getMass();
                            ModificationType modificationType =
                                    findModificationType(modificationTypes, mass);
                            dao.createModification(modifiedPeptideID, modificationType.getId(),
                                    aminoAcidMass.getPosition(), mass);
                        }
                    }
                }
            }
        }
        System.out.println("maximum group size: " + maxGroupSize + ", name:" + maxGroupName);
    }

    private Sequence getOrCreateSequence(Peptide peptide)
    {
        String s = peptide.getSequence();
        Sequence sequence = dao.tryToGetBySequence(s);
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
