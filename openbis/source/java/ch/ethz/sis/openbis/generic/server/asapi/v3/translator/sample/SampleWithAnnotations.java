/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;

/**
 * @author Franz-Josef Elmer
 *
 */
public class SampleWithAnnotations extends SamplePermId
{
    private static final long serialVersionUID = 1L;

    private final Sample sample;

    private String annotations;

    private String relatedAnnotations;

    public SampleWithAnnotations(Sample sample)
    {
        super("");
        this.sample = sample;
    }

    public Sample getSample()
    {
        return sample;
    }

    public String getAnnotations()
    {
        return annotations;
    }

    public void setAnnotations(String annotations)
    {
        this.annotations = annotations;
    }

    public String getRelatedAnnotations()
    {
        return relatedAnnotations;
    }

    public void setRelatedAnnotations(String relatedAnnotations)
    {
        this.relatedAnnotations = relatedAnnotations;
    }

    @Override
    public int hashCode()
    {
        return sample.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof SampleWithAnnotations == false)
        {
            return false;
        }
        return sample.equals(((SampleWithAnnotations) obj).sample);
    }
    
}
