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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Tomasz Pylak
 */
public class MzRunDTO
{
    private MzInstrumentDTO instrument;

    private List<MzScanDTO> scans;

    @XmlElement(name = "msInstrument", namespace = MzXmlDTO.NAMESPACE)
    public MzInstrumentDTO getInstrument()
    {
        return instrument;
    }

    public void setInstrument(MzInstrumentDTO instrument)
    {
        this.instrument = instrument;
    }

    @XmlElement(name = "scan", namespace = MzXmlDTO.NAMESPACE)
    public List<MzScanDTO> getScans()
    {
        return scans;
    }

    public void setScans(List<MzScanDTO> scans)
    {
        this.scans = scans;
    }

}
