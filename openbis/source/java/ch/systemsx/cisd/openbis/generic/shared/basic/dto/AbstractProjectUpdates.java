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

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * Description of the updates which should be performed on the project.
 * 
 * @author Tomasz Pylak
 */
public class AbstractProjectUpdates implements IsSerializable, Serializable
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Date version;

    private TechId id;

    // ----- the data which should be changed:

    private String description;

    // Code of the group to which project should be moved. If null nothing happens.
    private String groupCodeOrNull;

    public String getGroupCode()
    {
        return groupCodeOrNull;
    }

    public void setGroupCode(String groupCode)
    {
        this.groupCodeOrNull = groupCode;
    }

    public Date getVersion()
    {
        return version;
    }

    public void setVersion(Date version)
    {
        this.version = version;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public TechId getTechId()
    {
        return id;
    }

    public void setTechId(TechId id)
    {
        this.id = id;
    }
}
