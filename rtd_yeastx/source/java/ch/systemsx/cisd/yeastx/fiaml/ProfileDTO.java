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

package ch.systemsx.cisd.yeastx.fiaml;

/**
 * A data transfer object for "mz over intensity" profiles.
 *
 * @author Bernd Rinn
 */
public class ProfileDTO
{
    private float lowMz;
    
    private float highMz;
    
    private float[] mz;

    private float[] intensities;

    public static ProfileDTO split(float[] mz, float[] intensities, int imin, int imax)
    {
        final ProfileDTO dto = new ProfileDTO();
        final float[] mzChunk = new float[imax - imin];
        System.arraycopy(mz, imin, mzChunk, 0, imax - imin);
        dto.setMz(mzChunk);
        dto.setLowMz(mzChunk[0]);
        dto.setHighMz(mzChunk[mzChunk.length - 1]);
        final float[] intChunk = new float[imax - imin];
        System.arraycopy(intensities, imin, intChunk, 0, imax - imin);
        dto.setIntensities(intChunk);
        return dto;
    }
    
    public float getLowMz()
    {
        return lowMz;
    }

    public void setLowMz(float lowMz)
    {
        this.lowMz = lowMz;
    }

    public float getHighMz()
    {
        return highMz;
    }

    public void setHighMz(float highMz)
    {
        this.highMz = highMz;
    }

    public float[] getMz()
    {
        return mz;
    }

    public void setMz(float[] mz)
    {
        this.mz = mz;
    }

    public float[] getIntensities()
    {
        return intensities;
    }

    public void setIntensities(float[] intensities)
    {
        this.intensities = intensities;
    }
}