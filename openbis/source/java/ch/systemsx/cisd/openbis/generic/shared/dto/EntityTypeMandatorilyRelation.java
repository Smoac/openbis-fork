/*
 * Copyright 2007 ETH Zuerich, CISD
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * @author Franz-Josef Elmer
 */
public class EntityTypeMandatorilyRelation implements Serializable,
        Comparable<EntityTypeMandatorilyRelation>
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    public static final EntityTypeMandatorilyRelation[] EMPTY_ARRAY =
            new EntityTypeMandatorilyRelation[0];

    private EntityType type;

    private boolean mandatory;

    public final boolean isMandatory()
    {
        return mandatory;
    }

    public final void setMandatory(final boolean mandatory)
    {
        this.mandatory = mandatory;
    }

    public final EntityType getEntityType()
    {
        return type;
    }

    public final void setEntityType(final EntityType materialType)
    {
        this.type = materialType;
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof EntityTypeMandatorilyRelation == false)
        {
            return false;
        }
        final EntityTypeMandatorilyRelation that = (EntityTypeMandatorilyRelation) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(that.type, type);
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(type);
        return builder.toHashCode();
    }

    //
    // Comparable
    //

    /**
     * If <code>null</code> values are present for <code>type</code>, then they come first.
     */
    public final int compareTo(final EntityTypeMandatorilyRelation o)
    {
        final EntityType thatType = o.type;
        if (type == null)
        {
            return thatType == null ? 0 : -1;
        }
        if (thatType == null)
        {
            return 1;
        }
        return type.compareTo(thatType);
    }
}
