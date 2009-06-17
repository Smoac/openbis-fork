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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import ch.systemsx.cisd.openbis.etlserver.phosphonetx.Constants;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@XmlRootElement(name = "proteinprophet_details", namespace = Constants.NAMESPACE)
@XmlType
public class ProteinProphetDetails
{
    private String occamFlag;
    private NSPInformation nspInformation;

    @XmlAttribute(name = "occam_flag", required = true)
    public final String getOccamFlag()
    {
        return occamFlag;
    }

    public final void setOccamFlag(String occamFlag)
    {
        this.occamFlag = occamFlag;
    }

    @XmlElement(name = "nsp_information", namespace = Constants.NAMESPACE)
    public final NSPInformation getNspInformation()
    {
        return nspInformation;
    }

    public final void setNspInformation(NSPInformation nspInformation)
    {
        this.nspInformation = nspInformation;
    }

    @Override
    public String toString()
    {
        return "ProteinProphet[occamFlag=" + occamFlag + ", nspInfo=" + nspInformation + "]";
    }
}
