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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * Converter between {@link EntityProperty} and {@link EntityPropertyPE}.
 * 
 * @author Christian Ribeaud
 */
public interface IEntityPropertiesConverter
{

    /**
     * Converts the set of {@link EntityProperty} objects obtained from the specified entity to an
     * array of {@link EntityPropertyPE} objects.
     * 
     * @param registrator Will appear in the objects of the output.
     */
    public <T extends EntityPropertyPE> List<T> convertProperties(
            final EntityProperty<?, ?>[] properties, final String entityTypeCode,
            final PersonPE registrator);

    /**
     * Creates {@link EntityPropertyPE}.
     */
    public <T extends EntityPropertyPE> T createProperty(PropertyTypePE propertyType,
            EntityTypePropertyTypePE entityTypePropertyType, final PersonPE registrator,
            String value);

    /** Updates Set<T> of properties. */
    public <T extends EntityPropertyPE, P extends EntityProperty<?, ?>> Set<T> updateProperties(
            Collection<T> oldProperties, EntityTypePE entityType, List<P> newProperties,
            PersonPE registrator);

    /**
     * Checks whether all mandatory properties are provided.
     */
    public <T extends EntityPropertyPE> void checkMandatoryProperties(Collection<T> properties,
            EntityTypePE entityTypePE);

    /**
     * Checks whether all mandatory properties are provided. It uses (and fills) the
     * <var>cache</var> in order to avoid looking up the assigned properties time and again.
     */
    public <T extends EntityPropertyPE> void checkMandatoryProperties(Collection<T> properties,
            EntityTypePE entityTypePE, Map<EntityTypePE, List<EntityTypePropertyTypePE>> cache);
}
