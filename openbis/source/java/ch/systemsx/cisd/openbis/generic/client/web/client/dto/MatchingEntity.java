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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * An entity that matches the <i>Hibernate Search</i> query and which has been returned by the
 * server.
 * 
 * @author Christian Ribeaud
 */
public final class MatchingEntity implements IsSerializable, IIdentifierHolder
{
    private String identifier;

    private Person registrator;

    private EntityType entityType;

    private EntityKind entityKind;

    private String fieldDescription;

    private String textFragment;

    public final EntityKind getEntityKind()
    {
        return entityKind;
    }

    public final void setEntityKind(final EntityKind entityKind)
    {
        this.entityKind = entityKind;
    }

    public final void setIdentifier(final String identifier)
    {
        this.identifier = identifier;
    }

    public final Person getRegistrator()
    {
        return registrator;
    }

    public final void setRegistrator(final Person registrator)
    {
        this.registrator = registrator;
    }

    public final EntityType getEntityType()
    {
        return entityType;
    }

    public final void setEntityType(final EntityType entityType)
    {
        this.entityType = entityType;
    }

    public String getFieldDescription()
    {
        return fieldDescription;
    }

    public void setFieldDescription(String fieldDescription)
    {
        this.fieldDescription = fieldDescription;
    }

    public String getTextFragment()
    {
        return textFragment;
    }

    public void setTextFragment(String textFragment)
    {
        this.textFragment = textFragment;
    }

    //
    // IIdentifierHolder
    //

    public final String getIdentifier()
    {
        return identifier;
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return getIdentifier();
    }
}
