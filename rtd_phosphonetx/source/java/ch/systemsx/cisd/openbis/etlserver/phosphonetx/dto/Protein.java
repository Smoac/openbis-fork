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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import ch.systemsx.cisd.phosphonetx.Constants;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@XmlType
public class Protein extends AnnotatedProtein
{
    private double probability;
    private double coverage;
    private int numberOfIndistinguishableProteins;
    private String uniqueStrippedPeptides;
    private String groupSiblingID;
    private int totalNumberOfPeptides;
    private String subsumingProtein;
    private String fractionOfCorrectPeptideIdentifications;
    private List<AnnotatedProtein> indistinguishableProteins;
    private List<Peptide> peptides;
    
    @XmlAttribute(name = "probability", required = true)
    public final double getProbability()
    {
        return probability;
    }

    public final void setProbability(double probability)
    {
        this.probability = probability;
    }

    @XmlAttribute(name = "percent_coverage")
    public final double getCoverage()
    {
        return coverage;
    }

    public final void setCoverage(double coverage)
    {
        this.coverage = coverage;
    }

    @XmlAttribute(name = "n_indistinguishable_proteins", required = true)
    public final int getNumberOfIndistinguishableProteins()
    {
        return numberOfIndistinguishableProteins;
    }

    public final void setNumberOfIndistinguishableProteins(int numberOfIndistinguishableProteins)
    {
        this.numberOfIndistinguishableProteins = numberOfIndistinguishableProteins;
    }

    @XmlAttribute(name = "unique_stripped_peptides")
    public final String getUniqueStrippedPeptides()
    {
        return uniqueStrippedPeptides;
    }

    public final void setUniqueStrippedPeptides(String uniqueStrippedPeptides)
    {
        this.uniqueStrippedPeptides = uniqueStrippedPeptides;
    }

    @XmlAttribute(name = "group_sibling_id", required = true)
    public final String getGroupSiblingID()
    {
        return groupSiblingID;
    }

    public final void setGroupSiblingID(String groupSiblingID)
    {
        this.groupSiblingID = groupSiblingID;
    }

    @XmlAttribute(name = "total_number_peptides")
    public final int getTotalNumberOfPeptides()
    {
        return totalNumberOfPeptides;
    }

    public final void setTotalNumberOfPeptides(int totalNumberOfPeptides)
    {
        this.totalNumberOfPeptides = totalNumberOfPeptides;
    }

    @XmlAttribute(name = "subsuming_protein_entry")
    public final String getSubsumingProtein()
    {
        return subsumingProtein;
    }

    public final void setSubsumingProtein(String subsumingProtein)
    {
        this.subsumingProtein = subsumingProtein;
    }

    @XmlAttribute(name = "pct_spectrum_ids")
    public final String getFractionOfCorrectPeptideIdentifications()
    {
        return fractionOfCorrectPeptideIdentifications;
    }

    public final void setFractionOfCorrectPeptideIdentifications(String fractionOfCorrectPeptideIdentifications)
    {
        this.fractionOfCorrectPeptideIdentifications = fractionOfCorrectPeptideIdentifications;
    }

    @XmlElement(name = "indistinguishable_protein", namespace = Constants.NAMESPACE)
    public final List<AnnotatedProtein> getIndistinguishableProteins()
    {
        return indistinguishableProteins;
    }

    public final void setIndistinguishableProteins(List<AnnotatedProtein> indistinguishableProteins)
    {
        this.indistinguishableProteins = indistinguishableProteins;
    }

    @XmlElement(name = "peptide", namespace = Constants.NAMESPACE)
    public final List<Peptide> getPeptides()
    {
        return peptides;
    }

    public final void setPeptides(List<Peptide> peptides)
    {
        this.peptides = peptides;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append("[probability=").append(probability);
        builder.append(", coverage=").append(coverage);
        builder.append(", indistinguishableProteins=").append(numberOfIndistinguishableProteins);
        if (uniqueStrippedPeptides != null)
        {
            builder.append(", uniqueStrippedPeptides=").append(uniqueStrippedPeptides);
        }
        builder.append(", groupSiblingID=").append(groupSiblingID);
        if (totalNumberOfPeptides > 0)
        {
            builder.append(", totalNumberOfPeptides=").append(totalNumberOfPeptides);
        }
        if (subsumingProtein != null)
        {
            builder.append(", subsumingProtein=").append(subsumingProtein);
        }
        if (fractionOfCorrectPeptideIdentifications != null)
        {
            builder.append(", fractionOfCorrectPeptideIdentifications=").append(fractionOfCorrectPeptideIdentifications);
        }
        builder.append("]");
        if (annotation != null)
        {
            builder.append("\n  ").append(annotation);
        }
        if (indistinguishableProteins != null && indistinguishableProteins.isEmpty() == false)
        {
            builder.append("\n  indistinguishable proteins:");
            for (AnnotatedProtein protein : indistinguishableProteins)
            {   
                builder.append("\n    ").append(protein);
            }
        }
        if (peptides != null && peptides.isEmpty() == false)
        {
            builder.append("\n  peptides:");
            for (Peptide peptide : peptides)
            {
                builder.append("\n    ").append(peptide);
                List<PeptideModification> modifications = peptide.getModifications();
                if (modifications != null && modifications.isEmpty() == false)
                {
                    for (PeptideModification modification : modifications)
                    {
                        builder.append("\n      ").append(modification);
                    }
                }
            }
        }
        return builder.toString();
    }
}
