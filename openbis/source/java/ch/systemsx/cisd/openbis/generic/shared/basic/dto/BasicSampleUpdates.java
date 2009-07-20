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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * @author Izabela Adamczyk
 */
public class BasicSampleUpdates implements IsSerializable, Serializable
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private TechId sampleId;

    private List<SampleProperty> properties;

    private Date version;

    public BasicSampleUpdates()
    {
    }

    public BasicSampleUpdates(TechId sampleId, List<SampleProperty> properties, Date version)
    {
        this.sampleId = sampleId;
        this.properties = properties;
        this.version = version;
    }

    public TechId getSampleId()
    {
        return sampleId;
    }

    public void setSampleId(TechId sampleId)
    {
        this.sampleId = sampleId;
    }

    public List<SampleProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(List<SampleProperty> properties)
    {
        this.properties = properties;
    }

    public Date getVersion()
    {
        return version;
    }

    public void setVersion(Date version)
    {
        this.version = version;
    }

}