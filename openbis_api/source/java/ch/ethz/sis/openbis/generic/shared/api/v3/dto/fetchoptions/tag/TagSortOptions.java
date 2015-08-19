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

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.AbstractComparator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.SortOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.SortOrder;

/**
 * @author pkupczyk
 */
public class TagSortOptions extends SortOptions<Tag>
{

    private static final long serialVersionUID = 1L;

    private static final String CODE = "CODE";

    private static final String REGISTRATION_DATE = "REGISTRATION_DATE";

    private static final Map<String, Comparator<Tag>> comparators = new HashMap<>();

    static
    {
        comparators.put(CODE, new CodeComparator());
        comparators.put(REGISTRATION_DATE, new RegistrationDateComparator());
    }

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

    private static class CodeComparator extends AbstractComparator<Tag, String>
    {

        @Override
        protected String getValue(Tag o)
        {
            return o.getCode();
        }

    }

    private static class RegistrationDateComparator extends AbstractComparator<Tag, Date>
    {

        @Override
        protected Date getValue(Tag o)
        {
            return o.getRegistrationDate();
        }

    }

    @Override
    public Comparator<Tag> getComparator(String field)
    {
        return comparators.get(field);
    }

}
