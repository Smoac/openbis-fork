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

package ch.systemsx.cisd.openbis.etlserver.proteomics;

import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.AminoAcidMass;

/**
 * Data class keeping data extracted from a peptide <parameter> element of type 'modification'.
 * 
 * @author Franz-Josef Elmer
 */
final class ModificationFraction
{
    private final String sample;

    private final double fraction;

    private final AminoAcidMass aminoAcidMass;

    ModificationFraction(String sample, String value)
    {
        this.sample = sample;
        String[] parts = value.split(":");
        if (parts.length != 3)
        {
            throw exception(value, "Three parts separated by ':' expected.");
        }
        aminoAcidMass = new AminoAcidMass();
        try
        {
            aminoAcidMass.setPosition(Integer.parseInt(parts[0]));
        } catch (NumberFormatException ex)
        {
            throw exception(value, "Position part isn't an integer number: " + parts[0]);
        }
        try
        {
            aminoAcidMass.setMass(Double.parseDouble(parts[1]));
        } catch (NumberFormatException ex)
        {
            throw exception(value, "Mass part isn't a floating-point number: " + parts[1]);
        }
        try
        {
            fraction = Double.parseDouble(parts[2]);
        } catch (NumberFormatException ex)
        {
            throw exception(value, "Fraction part isn't a floating-point number: " + parts[2]);
        }
    }

    private IllegalArgumentException exception(String value, String message)
    {
        return new IllegalArgumentException("Peptide parameter value [" + value
                + "] for sample '" + sample + "' is invalid: " + message);
    }

    public String getSample()
    {
        return sample;
    }

    public AminoAcidMass getAminoAcidMass()
    {
        return aminoAcidMass;
    }

    public double getFraction()
    {
        return fraction;
    }
}