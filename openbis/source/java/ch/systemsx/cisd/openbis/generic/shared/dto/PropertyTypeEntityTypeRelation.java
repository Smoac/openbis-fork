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
import java.util.Comparator;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * Class that defines a relation between a {@link PropertyTypePE} and a
 * {@link EntityTypeMandatorilyRelation}.
 * 
 * @author Franz-Josef Elmer
 */
public final class PropertyTypeEntityTypeRelation implements Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    public static final PropertyTypeEntityTypeRelation[] EMPTY_ARRAY =
            new PropertyTypeEntityTypeRelation[0];

    private PropertyTypePE property;

    private EntityTypeMandatorilyRelation[] relations = new EntityTypeMandatorilyRelation[0];

    public final PropertyTypePE getProperty()
    {
        return property;
    }

    public final void setProperty(final PropertyTypePE property)
    {
        this.property = property;
    }

    public final EntityTypeMandatorilyRelation[] getRelations()
    {
        return relations;
    }

    public final void setRelations(final EntityTypeMandatorilyRelation[] relations)
    {
        this.relations = relations;
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
        if (obj instanceof PropertyTypeEntityTypeRelation == false)
        {
            return false;
        }
        final PropertyTypeEntityTypeRelation that = (PropertyTypeEntityTypeRelation) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(that.property, property);
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(property);
        return builder.toHashCode();
    }

    public static Comparator<PropertyTypeEntityTypeRelation> cerateComparator()
    {
        return new Comparator<PropertyTypeEntityTypeRelation>()
            {

                public int compare(final PropertyTypeEntityTypeRelation o1,
                        final PropertyTypeEntityTypeRelation o2)
                {
                    final PropertyTypePE property1 = o1.getProperty();
                    final PropertyTypePE property2 = o2.getProperty();
                    if (property1 == null)
                    {
                        return property2 == null ? 0 : -1;
                    }
                    if (property2 == null)
                    {
                        return 1;
                    }
                    return property1.compareTo(property2);
                }

            };
    }
}
