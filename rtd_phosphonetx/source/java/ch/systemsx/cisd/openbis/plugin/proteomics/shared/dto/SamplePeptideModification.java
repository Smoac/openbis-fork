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

package ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto;

import net.lemnik.eodsql.ResultColumn;

/**
 * @author Franz-Josef Elmer
 */
public class SamplePeptideModification extends AbstractSample
{
    private static final long serialVersionUID = 1L;

    @ResultColumn("fraction")
    private double fraction;

    @ResultColumn("pos")
    private int position;

    @ResultColumn("mass")
    private double mass;

    @ResultColumn("sequence")
    private String sequence;

    public double getFraction()
    {
        return fraction;
    }

    public void setFraction(double fraction)
    {
        this.fraction = fraction;
    }

    public int getPosition()
    {
        return position;
    }

    public void setPosition(int position)
    {
        this.position = position;
    }

    public double getMass()
    {
        return mass;
    }

    public void setMass(double mass)
    {
        this.mass = mass;
    }

    public String getSequence()
    {
        return sequence;
    }

    public void setSequence(String sequence)
    {
        this.sequence = sequence;
    }

}
