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

package ch.systemsx.cisd.phosphonetx;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.phosphonetx.db.DBFactory;
import ch.systemsx.cisd.phosphonetx.db.IProtDAO;
import ch.systemsx.cisd.phosphonetx.db.dto.ModificationType;
import ch.systemsx.cisd.phosphonetx.db.dto.Sequence;
import ch.systemsx.cisd.phosphonetx.dto.AminoAcidMass;
import ch.systemsx.cisd.phosphonetx.dto.Peptide;
import ch.systemsx.cisd.phosphonetx.dto.PeptideModification;
import ch.systemsx.cisd.phosphonetx.dto.Protein;
import ch.systemsx.cisd.phosphonetx.dto.ProteinGroup;
import ch.systemsx.cisd.phosphonetx.dto.ProteinProphetDetails;
import ch.systemsx.cisd.phosphonetx.dto.ProteinSummary;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class Main
{
    public static void main(String[] args) throws SQLException, JAXBException
    {
        DBFactory factory = new DBFactory();
        Connection connection = factory.getConnection();
        IProtDAO dao = QueryTool.getQuery(connection, IProtDAO.class);
        Iterable<ModificationType> modificationTypes = dao.listModificationTypes();

        JAXBContext context =
                JAXBContext.newInstance(ProteinSummary.class, ProteinProphetDetails.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        for (String argument : args)
        {
            long t0 = System.currentTimeMillis();
            Object object = unmarshaller.unmarshal(new File(argument));
            if (object instanceof ProteinSummary == false)
            {
                throw new IllegalArgumentException("Wrong type: " + object);
            }
            ProteinSummary summary = (ProteinSummary) object;
            long t1 = System.currentTimeMillis();
            System.out.println((t1 - t0) + " msec for oxm");
            addToDatabase("42", modificationTypes, summary, dao);
            System.out.println((System.currentTimeMillis() - t1) + " msec for db");
        }
        connection.commit();
    }
    
    private static void addToDatabase(String dataSetCode,
            Iterable<ModificationType> modificationTypes, ProteinSummary summary, IProtDAO dao)
    {
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
                    Sequence sequence = getOrCreateSequence(dao, peptide);
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

    private static Sequence getOrCreateSequence(IProtDAO dao, Peptide peptide)
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
    
    private static ModificationType findModificationType(Iterable<ModificationType> modificationTypes, double mass)
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
