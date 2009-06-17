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
 * A class that represents the data of a {@link FIAMSRunDTO}. 
 *
 * @author Bernd Rinn
 */
public class FIAMSRunDataDTO
{
    
    private float[] profileMz;
    
    private float[] profileIntensities;
    
    private float[] centroidMz;
    
    private float[] centroidIntensities;
    
    private float[] centroidCorrelations;

    public float[] getProfileMz()
    {
        return profileMz;
    }

    public void setProfileMz(float[] profileMz)
    {
        this.profileMz = profileMz;
    }

    public float[] getProfileIntensities()
    {
        return profileIntensities;
    }

    public void setProfileIntensities(float[] profileIntensities)
    {
        this.profileIntensities = profileIntensities;
    }

    public float[] getCentroidMz()
    {
        return centroidMz;
    }

    public void setCentroidMz(float[] centroidMz)
    {
        this.centroidMz = centroidMz;
    }

    public float[] getCentroidIntensities()
    {
        return centroidIntensities;
    }

    public void setCentroidIntensities(float[] centroidIntensities)
    {
        this.centroidIntensities = centroidIntensities;
    }

    public float[] getCentroidCorrelations()
    {
        return centroidCorrelations;
    }

    public void setCentroidCorrelations(float[] centroidCorrelations)
    {
        this.centroidCorrelations = centroidCorrelations;
    }
    
}
