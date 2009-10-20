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

package ch.systemsx.cisd.yeastx.quant.dto;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.systemsx.cisd.yeastx.utils.XmlDateAdapter;

/**
 * Stores the content of one quantification.
 * 
 * @author Tomasz Pylak
 */
public class MSQuantificationDTO
{
    private String source;

    private boolean valid;

    private String comment;

    private String registrator;

    private Date registrationDate;

    private List<MSConcentrationDTO> concentrations;

    @XmlElement(required = true, name = "source")
    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    @XmlElement(required = true, name = "valid")
    public boolean isValid()
    {
        return valid;
    }

    public void setValid(boolean valid)
    {
        this.valid = valid;
    }

    @XmlElement(name = "comment")
    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    @XmlElement(name = "registrator")
    public String getRegistrator()
    {
        return registrator;
    }

    public void setRegistrator(String registrator)
    {
        this.registrator = registrator;
    }

    @XmlElement(name = "registrationDate")
    @XmlJavaTypeAdapter(value = XmlDateAdapter.class, type = Date.class)
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    @XmlElement(required = true, name = "concentration")
    public List<MSConcentrationDTO> getConcentrations()
    {
        return concentrations;
    }

    public void setConcentrations(List<MSConcentrationDTO> concetrations)
    {
        this.concentrations = concetrations;
    }
}
