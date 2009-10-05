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
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.AminoAcidMass;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.AnnotatedProtein;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Parameter;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Peptide;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.PeptideModification;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Protein;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinGroup;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinProphetDetails;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinSummary;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinSummaryDataFilter;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinSummaryHeader;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ProtXMLLoaderTest extends ProtXMLTestCase
{
    @Test
    public void test()
    {
        File file = new File(workingDirectory, "test.xml");
        FileUtilities.writeToFile(file, EXAMPLE);
        ProteinSummary summary = new ProtXMLLoader().readProtXML(file);
        
        ProteinSummaryHeader header = summary.getSummaryHeader();
        assertEquals("some/path/uniprot.HUMAN.v125.fasta", header.getReferenceDatabase());
        ProteinProphetDetails proteinProphet =
                (ProteinProphetDetails) summary.getSummaryHeader().getProgramDetails().getSummary()[0];
        List<ProteinSummaryDataFilter> dataFilters = proteinProphet.getDataFilters();
        assertEquals(1, dataFilters.size());
        assertEquals(0.25, dataFilters.get(0).getMinProbability());
        assertEquals(1.0, dataFilters.get(0).getSensitivity());
        assertEquals(0.5, dataFilters.get(0).getFalsePositiveErrorRate());
        
        List<ProteinGroup> groups = summary.getProteinGroups();
        assertEquals(2, groups.size());
        assertEquals(1.0, groups.get(0).getProbability());
        List<Protein> proteins = groups.get(0).getProteins();
        assertEquals(2, proteins.size());
        Protein protein1 = proteins.get(0);
        assertEquals(1.0, protein1.getProbability());
        List<Parameter> parameters = protein1.getParameters();
        assertEquals(2, parameters.size());
        assertEquals("key1", parameters.get(0).getName());
        assertEquals("value1", parameters.get(0).getValue());
        assertEquals("type1", parameters.get(0).getType());
        assertEquals("key2", parameters.get(1).getName());
        assertEquals("value2", parameters.get(1).getValue());
        assertEquals("type2", parameters.get(1).getType());
        assertEquals("P42", protein1.getAnnotation().getDescription());
        List<AnnotatedProtein> indistinguishableProteins = protein1.getIndistinguishableProteins();
        assertEquals(2, indistinguishableProteins.size());
        assertEquals("P43", indistinguishableProteins.get(0).getAnnotation().getDescription());
        assertEquals("P44", indistinguishableProteins.get(1).getAnnotation().getDescription());
        List<Peptide> peptides = protein1.getPeptides();
        assertEquals(2, peptides.size());
        assertEquals("VYQIDGNYSR", peptides.get(0).getSequence());
        assertEquals(1, peptides.get(0).getModifications().size());
        PeptideModification peptideModification = peptides.get(0).getModifications().get(0);
        assertEquals(42.0, peptideModification.getNTermMass());
        assertEquals(24.25, peptideModification.getCTermMass());
        List<AminoAcidMass> masses = peptideModification.getAminoAcidMasses();
        assertEquals(2, masses.size());
        assertEquals(1, masses.get(0).getPosition());
        assertEquals(115.25, masses.get(0).getMass());
        assertEquals(4, masses.get(1).getPosition());
        assertEquals(31.75, masses.get(1).getMass());
        assertEquals("ITSN", peptides.get(1).getSequence());
        assertEquals(0, peptides.get(1).getModifications().size());
        Protein protein2 = proteins.get(1);
        assertEquals(0.0, protein2.getProbability());
        assertEquals("Q42", protein2.getAnnotation().getDescription());
        assertEquals(0, protein2.getIndistinguishableProteins().size());
        assertEquals(1, protein2.getPeptides().size());
        assertEquals("YSR", protein2.getPeptides().get(0).getSequence());
        assertEquals(0, protein2.getPeptides().get(0).getModifications().size());
        
        assertEquals(0.75, groups.get(1).getProbability());
        assertEquals(1, groups.get(1).getProteins().size());
        Protein protein = groups.get(1).getProteins().get(0);
        assertEquals(0.75, protein.getProbability());
        assertEquals("R42", protein.getAnnotation().getDescription());
        assertEquals(0, protein.getIndistinguishableProteins().size());
        assertEquals(1, protein.getPeptides().size());
        assertEquals("IYSR", protein.getPeptides().get(0).getSequence());
        assertEquals(0, protein.getPeptides().get(0).getModifications().size());
    }
}
