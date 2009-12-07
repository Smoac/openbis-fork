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

    // if technical id is null old identifier must be provided by subclass
    private TechId sampleIdOrNull;

    private List<IEntityProperty> properties;

    private Date version;

    private String parentIdentifierOrNull;

    private String containerIdentifierOrNull;

    public String getParentIdentifierOrNull()
    {
        return parentIdentifierOrNull;
    }

    public void setParentIdentifierOrNull(String parentIdentifierOrNull)
    {
        this.parentIdentifierOrNull = parentIdentifierOrNull;
    }

    public String getContainerIdentifierOrNull()
    {
        return containerIdentifierOrNull;
    }

    public void setContainerIdentifierOrNull(String containerIdentifierOrNull)
    {
        this.containerIdentifierOrNull = containerIdentifierOrNull;
    }

    public BasicSampleUpdates()
    {
    }

    public BasicSampleUpdates(TechId sampleId, List<IEntityProperty> properties, Date version,
            String parentIdentifierOrNull, String containerIdentifierOrNull)
    {
        this.sampleIdOrNull = sampleId;
        this.properties = properties;
        this.version = version;
        this.parentIdentifierOrNull = parentIdentifierOrNull;
        this.containerIdentifierOrNull = containerIdentifierOrNull;
    }

    public TechId getSampleIdOrNull()
    {
        return sampleIdOrNull;
    }

    public void setSampleId(TechId sampleId)
    {
        this.sampleIdOrNull = sampleId;
    }

    public List<IEntityProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(List<IEntityProperty> properties)
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
