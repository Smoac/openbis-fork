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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import ch.systemsx.cisd.openbis.etlserver.phosphonetx.Constants;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@XmlType
public class Peptide
{
    private String sequence;
    private int charge;
    private double initialProbability;
    private double weight;
    private List<PeptideModification> modifications = new ArrayList<PeptideModification>();

    @XmlAttribute(name = "peptide_sequence", required = true)
    public final String getSequence()
    {
        return sequence;
    }

    public final void setSequence(String sequence)
    {
        this.sequence = sequence;
    }

    @XmlAttribute(name = "charge", required = true)
    public final int getCharge()
    {
        return charge;
    }

    public final void setCharge(int charge)
    {
        this.charge = charge;
    }

    @XmlAttribute(name = "initial_probability", required = true)
    public final double getInitialProbability()
    {
        return initialProbability;
    }

    public final void setInitialProbability(double initialProbability)
    {
        this.initialProbability = initialProbability;
    }

    @XmlAttribute(name = "weight")
    public final double getWeight()
    {
        return weight;
    }

    public final void setWeight(double weight)
    {
        this.weight = weight;
    }

    @XmlElement(name = "modification_info", namespace = Constants.NAMESPACE)
    public final List<PeptideModification> getModifications()
    {
        return modifications;
    }

    public final void setModifications(List<PeptideModification> modifications)
    {
        this.modifications = modifications;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(sequence);
        if (charge > 0)
        {
            builder.append("(");
            for (int i = 0; i < charge; i++)
            {
                builder.append('+');
            }
            builder.append(")");
        }
        builder.append("[initialProbability=").append(initialProbability);
        builder.append(", weight=").append(weight);
        builder.append("]");
        return builder.toString();
    }
    
    
}
