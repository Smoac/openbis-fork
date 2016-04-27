/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application;

import java.util.Arrays;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application.ProteinRenderers;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.Peptide;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.PeptideModification;

/**
 * @author Franz-Josef Elmer
 */
public class ProteinRenderersTest extends AssertJUnit
{
    @Test
    public void testRenderAminoAcidSymbol()
    {
        assertEquals("<font style='text-decoration:underline; cursor:pointer' "
                + "color='blue' title='mass=4.5'>A</font>",
                ProteinRenderers.renderAminoAcidSymbol('A', null, 4.5));
        assertEquals("<font style='text-decoration:underline; cursor:pointer' "
                + "color='blue' title='position=42, mass=4.5'>A</font>",
                ProteinRenderers.renderAminoAcidSymbol('A', 42, 4.5));
    }

    @Test
    public void testMarkOccurrencesWithHtml()
    {
        Peptide p1 = peptide("defgh:2=4.25");
        Peptide p2 = peptide("ijk:1=-2.75");
        Peptide p3 = peptide("jko");
        Peptide p4 = peptide("jjab");
        String e = ProteinRenderers.renderAminoAcidSymbol('e', 5, 4.25);
        String i1 = ProteinRenderers.renderAminoAcidSymbol('i', 9, -2.75);
        String i2 = ProteinRenderers.renderAminoAcidSymbol('i', 15, -2.75);
        assertEquals(
                "abc<font color='red'>d" + e + " fgh" + i1 + "j k</font>lmn<font color='red'>"
                        + i2 + " jko</font>pq rstu",
                ProteinRenderers.markOccurrencesWithHtml("abcdefghijklmnijkopqrstu",
                        Arrays.asList(p1, p2, p3, p4), 5));
    }

    private Peptide peptide(String description)
    {
        Peptide peptide = new Peptide();
        int indexOfColon = description.indexOf(':');
        if (indexOfColon < 0)
        {
            peptide.setSequence(description);
            return peptide;
        }
        peptide.setSequence(description.substring(0, indexOfColon));
        String[] modificationDescriptions = description.substring(indexOfColon + 1).split(",");
        for (String modificationDescription : modificationDescriptions)
        {
            String[] posAndMass = modificationDescription.split("=");
            PeptideModification peptideModification = new PeptideModification();
            peptideModification.setPosition(Integer.parseInt(posAndMass[0]));
            peptideModification.setMass(Double.parseDouble(posAndMass[1]));
            peptide.getModifications().add(peptideModification);
        }
        return peptide;
    }

}
