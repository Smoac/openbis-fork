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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * A basic {@link IEntityInformationHolder} implementation.
 * 
 * @author Piotr Buczek
 */
public class BasicEntityInformationHolder implements IEntityInformationHolder, IsSerializable,
        Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private EntityKind entityKind;

    private EntityType entityType;

    private String identifier;

    private String code;

    private Long id;

    @SuppressWarnings("unused")
    private BasicEntityInformationHolder()
    {
        // needed for serialization purposes
    }

    public BasicEntityInformationHolder(EntityKind entityKind, EntityType entityType,
            String identifier, String code, Long id)
    {
        this.entityKind = entityKind;
        this.entityType = entityType;
        this.identifier = identifier;
        this.code = code;
        this.id = id;
    }

    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    public EntityType getEntityType()
    {
        return entityType;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public String getCode()
    {
        return code;
    }

    public Long getId()
    {
        return id;
    }
}
