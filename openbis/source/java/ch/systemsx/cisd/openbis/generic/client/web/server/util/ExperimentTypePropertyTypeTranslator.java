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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * A {@link ExperimentTypePropertyType} &lt;---&gt; {@link ExperimentTypePropertyTypePE} translator.
 * 
 * @author Franz-Josef Elmer
 */
public final class ExperimentTypePropertyTypeTranslator
{
    private ExperimentTypePropertyTypeTranslator()
    {
        // Can not be instantiated.
    }

    public final static ExperimentTypePropertyType translate(
            final ExperimentTypePropertyTypePE expTypePropertyType)
    {
        final ExperimentTypePropertyType result = new ExperimentTypePropertyType();
        result.setManagedInternally(expTypePropertyType.isManagedInternally());
        result.setMandatory(expTypePropertyType.isMandatory());
        result.setPropertyType(PropertyTypeTranslator.translate(expTypePropertyType
                .getPropertyType()));
        return result;
    }

    public final static List<ExperimentTypePropertyType> translate(
            final Set<ExperimentTypePropertyTypePE> expTypePropertyTypes,
            final ExperimentType expType)
    {
        if (HibernateUtils.isInitialized(expTypePropertyTypes) == false)
        {
            return DtoConverters.createUnmodifiableEmptyList();
        }
        final List<ExperimentTypePropertyType> result = new ArrayList<ExperimentTypePropertyType>();
        for (final ExperimentTypePropertyTypePE etpt : expTypePropertyTypes)
        {
            final ExperimentTypePropertyType etptTranslated = translate(etpt);
            etptTranslated.setEntityType(expType);
            result.add(etptTranslated);
        }
        Collections.sort(result);
        return result;
    }

}
