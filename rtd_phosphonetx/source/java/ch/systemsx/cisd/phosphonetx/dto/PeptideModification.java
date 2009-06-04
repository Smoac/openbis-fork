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

package ch.systemsx.cisd.phosphonetx.dto;

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
public class PeptideModification
{
    private String modifiedPeptide;
    private String nTermMass;
    private String cTermMass;
    private List<AminoAcidMass> aminoAcidMasses;

    @XmlAttribute(name = "modified_peptide")
    public final String getModifiedPeptide()
    {
        return modifiedPeptide;
    }

    public final void setModifiedPeptide(String modifiedPeptide)
    {
        this.modifiedPeptide = modifiedPeptide;
    }

    @XmlAttribute(name = "mod_nterm_mass")
    public final String getNTermMass()
    {
        return nTermMass;
    }

    public final void setNTermMass(String termMass)
    {
        nTermMass = termMass;
    }

    @XmlAttribute(name = "mod_cterm_mass")
    public final String getCTermMass()
    {
        return cTermMass;
    }

    public final void setCTermMass(String termMass)
    {
        cTermMass = termMass;
    }

    @XmlElement(name = "mod_aminoacid_mass", namespace = Constants.NAMESPACE)
    public final List<AminoAcidMass> getAminoAcidMasses()
    {
        return aminoAcidMasses;
    }

    public final void setAminoAcidMasses(List<AminoAcidMass> aminoAcidMasses)
    {
        this.aminoAcidMasses = aminoAcidMasses;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Modification[modifiedPeptide=").append(modifiedPeptide);
        builder.append(", nTermMass=").append(nTermMass);
        builder.append(", cTermMass=").append(cTermMass);
        builder.append("]");
        if (aminoAcidMasses != null && aminoAcidMasses.isEmpty() == false)
        {
            builder.append("(");
            for (int i = 0; i < aminoAcidMasses.size(); i++)
            {
                if (i > 0)
                {
                    builder.append(", ");
                }
                builder.append(aminoAcidMasses.get(i));
            }
            builder.append(")");
        }
        return builder.toString();
    }
    
}
