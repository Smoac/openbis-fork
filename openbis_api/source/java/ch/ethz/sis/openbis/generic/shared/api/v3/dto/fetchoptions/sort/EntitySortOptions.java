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

import static ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.comparator.CodeComparator.CODE;
import static ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.comparator.ModificationDateComparator.MODIFICATION_DATE;
import static ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.comparator.PropertyComparator.PROPERTY;
import static ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.comparator.RegistrationDateComparator.REGISTRATION_DATE;

import java.util.Comparator;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.comparator.CodeComparator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.comparator.ModificationDateComparator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.comparator.PropertyComparator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.comparator.RegistrationDateComparator;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("dto.fetchoptions.sort.EntitySortOptions")
public class EntitySortOptions<OBJECT extends ICodeHolder & IRegistrationDateHolder & IModificationDateHolder> extends SortOptions<OBJECT>
{

    private static final long serialVersionUID = 1L;

    public SortOrder code()
    {
        return getOrCreateSorting(CODE);
    }

    public SortOrder getCode()
    {
        return getSorting(CODE);
    }

    public SortOrder property(String propertyName)
    {
        return getOrCreateSorting(PROPERTY + propertyName);
    }

    public SortOrder getProperty(String propertyName)
    {
        return getSorting(PROPERTY + propertyName);
    }

    public SortOrder registrationDate()
    {
        return getOrCreateSorting(REGISTRATION_DATE);
    }

    public SortOrder getRegistrationDate()
    {
        return getSorting(REGISTRATION_DATE);
    }

    public SortOrder modificationDate()
    {
        return getOrCreateSorting(MODIFICATION_DATE);
    }

    public SortOrder getModificationDate()
    {
        return getSorting(MODIFICATION_DATE);
    }

    @Override
    public Comparator<OBJECT> getComparator(String field)
    {
        if (CODE.equals(field))
        {
            return new CodeComparator<OBJECT>();
        } else if (REGISTRATION_DATE.equals(field))
        {
            return new RegistrationDateComparator<OBJECT>();
        } else if (MODIFICATION_DATE.equals(field))
        {
            return new ModificationDateComparator<OBJECT>();
        } else if (field.startsWith(PROPERTY))
        {
            return new PropertyComparator<OBJECT>(field.substring(PROPERTY.length()));
        } else
        {
            return null;
        }
    }
}
