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

import net.lemnik.eodsql.ResultColumn;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ModificationType extends AbstractDTOWithID
{
    private String code;
    
    @ResultColumn("amino_acid")
    private String aminoAcid;
    
    private double mass;
    
    @ResultColumn("mass_tolerance")
    private double deltaMass;

    public final String getCode()
    {
        return code;
    }

    public final void setCode(String code)
    {
        this.code = code;
    }

    public final String getAminoAcid()
    {
        return aminoAcid;
    }

    public final void setAminoAcid(String aminoAcid)
    {
        this.aminoAcid = aminoAcid;
    }

    public final double getMass()
    {
        return mass;
    }

    public final void setMass(double mass)
    {
        this.mass = mass;
    }

    public final double getDeltaMass()
    {
        return deltaMass;
    }

    public final void setDeltaMass(double deltaMass)
    {
        this.deltaMass = deltaMass;
    }
    
    public boolean matches(double m)
    {
        return mass - deltaMass <= m && m <= mass + deltaMass;
    }

    @Override
    public String toString()
    {
        return code + "=(" + aminoAcid + ":" + mass + "\u00b1" + deltaMass + ")";
    }
    
    
    
}
