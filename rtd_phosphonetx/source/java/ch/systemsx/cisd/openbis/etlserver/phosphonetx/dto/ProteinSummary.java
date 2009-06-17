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

import java.util.List;

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
@XmlRootElement(name = "protein_summary", namespace = Constants.NAMESPACE)
@XmlType
public class ProteinSummary
{
    private String summaryXML;
    private ProteinSummaryHeader summaryHeader;
    private List<ProteinGroup> proteinGroups;

    @XmlAttribute(name = "summary_xml")
    public final String getSummaryXML()
    {
        return summaryXML;
    }

    public final void setSummaryXML(String summaryXML)
    {
        this.summaryXML = summaryXML;
    }

    @XmlElement(name = "protein_summary_header", namespace = Constants.NAMESPACE)
    public final ProteinSummaryHeader getSummaryHeader()
    {
        return summaryHeader;
    }

    public final void setSummaryHeader(ProteinSummaryHeader summaryHeader)
    {
        this.summaryHeader = summaryHeader;
    }

    @XmlElement(name = "protein_group", namespace = Constants.NAMESPACE)
    public final List<ProteinGroup> getProteinGroups()
    {
        return proteinGroups;
    }

    public final void setProteinGroups(List<ProteinGroup> proteinGroups)
    {
        this.proteinGroups = proteinGroups;
    }
    
    
}
