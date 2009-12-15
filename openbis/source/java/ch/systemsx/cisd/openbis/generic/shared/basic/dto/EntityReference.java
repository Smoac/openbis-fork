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

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;

/**
 * Reference to an entity with minimal information to uniquely identify it in the database and to
 * choose the right plugin to handle it.
 * 
 * @author Tomasz Pylak
 */
public class EntityReference implements Serializable, IsSerializable, IEntityInformationHolder
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private long id;

    private String code;

    private String typeCode;

    private EntityKind kind;

    // GWT only
    @SuppressWarnings("unused")
    private EntityReference()
    {
    }

    public EntityReference(long id, String code, String typeCode, EntityKind kind)
    {
        this.id = id;
        this.code = code;
        this.typeCode = typeCode;
        this.kind = kind;
    }

    public EntityKind getEntityKind()
    {
        return kind;
    }

    public BasicEntityType getEntityType()
    {
        BasicEntityType basicEntityType = new BasicEntityType();
        basicEntityType.setCode(typeCode);
        return basicEntityType;
    }

    public Long getId()
    {
        return id;
    }

    public String getCode()
    {
        return code;
    }

}
