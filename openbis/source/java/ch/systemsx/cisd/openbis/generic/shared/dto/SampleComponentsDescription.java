/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Stores basic information about sample components.
 * 
 * @author Izabela Adamczyk
 */
public class SampleComponentsDescription implements IsSerializable, Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private final String containerPermId;

    private Set<String> codes;

    private String sampleTypeCode;

    public SampleComponentsDescription(String containerPermId, Set<String> codes,
            String sampleTypeCode)
    {
        this.containerPermId = containerPermId;
        this.codes = codes;
        this.sampleTypeCode = sampleTypeCode;
    }

    public String getContainerPermId()
    {
        return containerPermId;
    }

    public Set<String> getCodes()
    {
        return codes;
    }

    public void setCodes(Set<String> codes)
    {
        this.codes = codes;
    }

    public String getSampleTypeCode()
    {
        return sampleTypeCode;
    }

    public void setSampleTypeCode(String sampleTypeCode)
    {
        this.sampleTypeCode = sampleTypeCode;
    }
}