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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ProteinWithAbundances extends ProteinReference
{
    private static final long serialVersionUID = 1L;

    private static final double[] EMPTY_ARRAY = new double[0];
    
    private double coverage;
    
    private final Map<Long, DoubleArrayList> abundances = new LinkedHashMap<Long, DoubleArrayList>();
    
    public double getCoverage()
    {
        return coverage;
    }
    
    public void setCoverage(double coverage)
    {
        this.coverage = coverage;
    }
    
    public void addAbundanceFor(long sampleID, double abundance)
    {
        DoubleArrayList list = abundances.get(sampleID);
        if (list == null)
        {
            list = new DoubleArrayList();
            abundances.put(sampleID, list);
        }
        list.add(abundance);
    }
    
    public Set<Long> getSampleIDs()
    {
        return abundances.keySet();
    }
    
    public double[] getAbundancesForSample(long sampleID)
    {
        DoubleArrayList list = abundances.get(sampleID);
        return list == null ? EMPTY_ARRAY : list.toDoubleArray();
    }
}
