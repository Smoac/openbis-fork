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

package ch.systemsx.cisd.openbis.etlserver.proteomics.dto;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class AminoAcidMass
{
    private int position;
    private double mass;
    
    @XmlAttribute(name = "position", required = true)
    public final int getPosition()
    {
        return position;
    }
    public final void setPosition(int position)
    {
        this.position = position;
    }
    
    @XmlAttribute(name = "mass", required = true)
    public final double getMass()
    {
        return mass;
    }
    public final void setMass(double mass)
    {
        this.mass = mass;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof AminoAcidMass == false)
        {
            return false;
        }
        AminoAcidMass that = (AminoAcidMass) obj;
        return this.position == that.position && this.mass == that.mass;
    }
    
    @Override
    public int hashCode()
    {
        return (int) (37 * position + mass);
    }
    
    @Override
    public String toString()
    {
        return mass + "@" + position;
    }
}
