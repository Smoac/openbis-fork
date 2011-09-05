/*
 * Copyright 2011 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;

/**
 * @author Kaloyan Enimanev
 */
public class DeletedEntity implements IEntityInformationHolderWithIdentifier
{

    private static final long serialVersionUID = 1L;

    private Long id;

    private String code;

    private String permId;

    private String identifier;

    private BasicEntityType entityType;

    private EntityKind entityKind;

    // GTW
    @SuppressWarnings("unused")
    private DeletedEntity()
    {

    }

    public DeletedEntity(Long id, String code, String permId, String identifier,
            BasicEntityType entityType, EntityKind entityKind)
    {
        this.id = id;
        this.code = code;
        this.permId = permId;
        this.identifier = identifier;
        this.entityType = entityType;
        this.entityKind = entityKind;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public BasicEntityType getEntityType()
    {
        return entityType;
    }

    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    public Long getId()
    {
        return id;
    }

    public String getCode()
    {
        return code;
    }

    public String getPermId()
    {
        return permId;
    }

}
