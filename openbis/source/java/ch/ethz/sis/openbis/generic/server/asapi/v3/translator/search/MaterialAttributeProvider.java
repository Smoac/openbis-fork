/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.search;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind.ID;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind.CODE;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind.PERM_ID;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind.MODIFICATION_DATE;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind.MODIFICATION_DATE_FROM;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind.MODIFICATION_DATE_UNTIL;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind.REGISTRATION_DATE;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind.REGISTRATION_DATE_FROM;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind.REGISTRATION_DATE_UNTIL;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ModificationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.RegistrationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.TechIdSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAttributeSearchFieldKind;

/**
 * @author pkupczyk
 */
public class MaterialAttributeProvider extends AbstractEntityAttributeProvider
{

    @Override
    public IAttributeSearchFieldKind getAttribute(ISearchCriteria criteria)
    {
        if (criteria instanceof TechIdSearchCriteria)
        {
            return ID;
        } else if (criteria instanceof CodeSearchCriteria)
        {
            return CODE;
        } else if (criteria instanceof PermIdSearchCriteria)
        {
            return PERM_ID;
        } else if (criteria instanceof RegistrationDateSearchCriteria)
        {
            RegistrationDateSearchCriteria dateCriteria = (RegistrationDateSearchCriteria) criteria;
            return getDateAttribute(dateCriteria.getFieldValue(), REGISTRATION_DATE, REGISTRATION_DATE_UNTIL, REGISTRATION_DATE_FROM);
        } else if (criteria instanceof ModificationDateSearchCriteria)
        {
            ModificationDateSearchCriteria dateCriteria = (ModificationDateSearchCriteria) criteria;
            return getDateAttribute(dateCriteria.getFieldValue(), MODIFICATION_DATE, MODIFICATION_DATE_UNTIL, MODIFICATION_DATE_FROM);
        } else
        {
            throw new IllegalArgumentException("Unknown attribute criteria: " + criteria);
        }
    }

}
