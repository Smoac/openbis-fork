/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort;

import static ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.comparator.PropertyComparator.PROPERTY;

import java.util.Comparator;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.comparator.PropertyComparator;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("dto.fetchoptions.sort.EntityWithPropertiesSortOptions")
public class EntityWithPropertiesSortOptions<OBJECT extends ICodeHolder & IRegistrationDateHolder & IModificationDateHolder & IPropertiesHolder>
        extends EntitySortOptions<OBJECT>
{

    private static final long serialVersionUID = 1L;

    public SortOrder property(String propertyName)
    {
        return getOrCreateSorting(PROPERTY + propertyName);
    }

    public SortOrder getProperty(String propertyName)
    {
        return getSorting(PROPERTY + propertyName);
    }

    @Override
    public Comparator<OBJECT> getComparator(String field)
    {
        if (field.startsWith(PROPERTY))
        {
            return new PropertyComparator<OBJECT>(field.substring(PROPERTY.length()));
        } else
        {
            return super.getComparator(field);
        }
    }
}
