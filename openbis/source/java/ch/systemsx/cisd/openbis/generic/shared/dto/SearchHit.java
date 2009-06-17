/*
 * Copyright 2008 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Tomasz Pylak
 */
public class SearchHit implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private final IMatchingEntity entity;

    // which entity field matches the query
    private final String fieldDescription;

    // which text fragment contained in entity fields matches the query
    private final String textFragment;

    public SearchHit(IMatchingEntity entity, String field, String textFragment)
    {
        this.entity = entity;
        this.fieldDescription = field;
        this.textFragment = textFragment;
    }

    public String getFieldDescription()
    {
        return fieldDescription;
    }

    public String getTextFragment()
    {
        return textFragment;
    }

    public EntityKind getEntityKind()
    {
        return entity.getEntityKind();
    }

    public EntityTypePE getEntityType()
    {
        return entity.getEntityType();
    }

    public String getIdentifier()
    {
        return entity.getIdentifier();
    }

    public Long getId()
    {
        return entity.getId();
    }

    public String getCode()
    {
        return entity.getCode();
    }

    public PersonPE getRegistrator()
    {
        return entity.getRegistrator();
    }

    public IMatchingEntity getEntity()
    {
        return entity;
    }

}
