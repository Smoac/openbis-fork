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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Stores the content of the part of *.mzXML file (raw data).
 * 
 * @author Tomasz Pylak
 */
@XmlRootElement(name = "mzXML", namespace = MzXmlDTO.NAMESPACE)
@XmlType
public class MzXmlDTO
{
    public static final String NAMESPACE =
            "http://sashimi.sourceforge.net/schema_revision/mzXML_3.1";

    private MzRunDTO run;

    @XmlElement(name = "msRun", namespace = MzXmlDTO.NAMESPACE)
    public MzRunDTO getRun()
    {
        return run;
    }

    public void setRun(MzRunDTO run)
    {
        this.run = run;
    }

}
