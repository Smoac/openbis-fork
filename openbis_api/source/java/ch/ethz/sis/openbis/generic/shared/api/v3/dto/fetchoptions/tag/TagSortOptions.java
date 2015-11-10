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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.tag;

import static ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.comparator.CodeComparator.CODE;
import static ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.comparator.RegistrationDateComparator.REGISTRATION_DATE;

import java.util.Comparator;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.SortOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.SortOrder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.comparator.CodeComparator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.comparator.RegistrationDateComparator;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("dto.fetchoptions.tag.TagSortOptions")
public class TagSortOptions extends SortOptions<Tag>
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

    public SortOrder registrationDate()
    {
        return getOrCreateSorting(REGISTRATION_DATE);
    }

    public SortOrder getRegistrationDate()
    {
        return getSorting(REGISTRATION_DATE);
    }

    @Override
    public Comparator<Tag> getComparator(String field)
    {
        if (CODE.equals(field))
        {
            return new CodeComparator<Tag>();
        } else if (REGISTRATION_DATE.equals(field))
        {
            return new RegistrationDateComparator<Tag>();
        } else
        {
            return null;
        }
    }
}
