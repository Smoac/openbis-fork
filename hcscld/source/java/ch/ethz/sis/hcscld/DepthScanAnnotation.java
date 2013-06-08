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

package ch.ethz.sis.hcscld;

/**
 * Annotation for depth scans sequences.
 *
 * @author Bernd Rinn
 */
public class DepthScanAnnotation
{
    private final String unit;
    
    private final double[] zValues;

    /**
     * Creates a depth scan annotation.
     *
     * @param unit The unit of the z values
     * @param zValues The z values.
     */
    public DepthScanAnnotation(String unit, double[] zValues)
    {
        this.unit = unit;
        this.zValues = zValues;
    }

    /**
     * Returns the unit of the z values.
     */
    public String getUnit()
    {
        return unit;
    }

    /**
     * Returns the z values.
     */
    public double[] getZValues()
    {
        return zValues;
    }
}
