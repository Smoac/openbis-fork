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

package ch.systemsx.cisd.yeastx.mzxml.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author Tomasz Pylak
 */
public class MzPrecursorDTO
{
    private Double mz;

    private Double intensity;

    private Integer charge;

    @XmlValue
    // not null
    public Double getMz()
    {
        return mz;
    }

    public void setMz(Double mz)
    {
        this.mz = mz;
    }

    @XmlAttribute(name = "precursorIntensity", required = true)
    public Double getIntensity()
    {
        return intensity;
    }

    public void setIntensity(Double intensity)
    {
        this.intensity = intensity;
    }

    @XmlAttribute(name = "precursorCharge")
    public Integer getCharge()
    {
        return charge;
    }

    public void setCharge(Integer charge)
    {
        this.charge = charge;
    }

}
