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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePropertyTypePE;

/**
 * A {@link ExperimentProperty} &lt;---&gt; {@link ExperimentPropertyPE} translator.
 * 
 * @author Izabela Adamczyk
 */
public final class ExperimentPropertyTranslator
{
    private ExperimentPropertyTranslator()
    {
        // Can not be instantiated.
    }

    public final static ExperimentProperty translate(final ExperimentPropertyPE experimentPropertyPE)
    {
        final ExperimentProperty result = new ExperimentProperty();
        result.setValue(StringEscapeUtils.escapeHtml(experimentPropertyPE.tryGetUntypedValue()));
        result.setEntityTypePropertyType(ExperimentTypePropertyTypeTranslator
                .translate((ExperimentTypePropertyTypePE) experimentPropertyPE
                        .getEntityTypePropertyType()));
        return result;

    }

    public final static List<ExperimentProperty> translate(final Set<ExperimentPropertyPE> list)
    {
        if (list == null)
        {
            return null;
        }
        final List<ExperimentProperty> result = new ArrayList<ExperimentProperty>();
        for (final ExperimentPropertyPE experimentProperty : list)
        {
            result.add(translate(experimentProperty));
        }
        return result;
    }
}
