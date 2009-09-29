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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Stores the content of the whole *.quantML file (all quantifications).
 * 
 * @author Tomasz Pylak
 */
@XmlRootElement(name = "quantML")
@XmlType
public class MSQuantificationsDTO
{
    private List<MSQuantificationDTO> quantifications;

    @XmlElement(name = "msQuantification")
    public List<MSQuantificationDTO> getQuantifications()
    {
        return quantifications;
    }

    public void setQuantifications(List<MSQuantificationDTO> quantifications)
    {
        this.quantifications = quantifications;
    }

}
