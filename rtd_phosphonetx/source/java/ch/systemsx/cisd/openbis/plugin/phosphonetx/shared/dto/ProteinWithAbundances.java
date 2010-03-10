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
    
    private final Map<Long, double[]> abundances = new LinkedHashMap<Long, double[]>();
    
    public void addAbundanceFor(long sampleID, double abundance)
    {
        double[] array = abundances.get(sampleID);
        if (array == null)
        {
            array = EMPTY_ARRAY;
        }
        double[] newArray = new double[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = abundance;
        abundances.put(sampleID, newArray);
    }
    
    public Set<Long> getSampleIDs()
    {
        return abundances.keySet();
    }
    
    public double[] getAbundancesForSample(long sampleID)
    {
        double[] values = abundances.get(sampleID);
        return values == null ? EMPTY_ARRAY : values;
    }
}
