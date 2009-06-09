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

import java.util.Date;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@XmlType
public class ProgramDetails
{
    private String analysis;
    
    private Date timestamp;
    
    private String version;
    
    private Object[] summary;

    @XmlAttribute(name = "analysis", required = true)
    public final String getAnalysis()
    {
        return analysis;
    }

    public final void setAnalysis(String analysis)
    {
        this.analysis = analysis;
    }

//    @XmlAttribute(name = "time", required = true)
    public final Date getTimestamp()
    {
        return timestamp;
    }

    public final void setTimestamp(Date timestamp)
    {
        this.timestamp = timestamp;
    }

    @XmlAttribute(name = "version")
    public final String getVersion()
    {
        return version;
    }

    public final void setVersion(String version)
    {
        this.version = version;
    }

    @XmlAnyElement(lax = true)
    public final Object[] getSummary()
    {
        return summary;
    }

    public final void setSummary(Object[] summary)
    {
        this.summary = summary;
    }
    
}
