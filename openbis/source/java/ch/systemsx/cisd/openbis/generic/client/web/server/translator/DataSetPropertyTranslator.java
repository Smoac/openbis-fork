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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePropertyTypePE;

/**
 * A {@link DataSetProperty} &lt;---&gt; {@link DataSetPropertyPE} translator.
 * 
 * @author Izabela Adamczyk
 */
public final class DataSetPropertyTranslator
{
    private DataSetPropertyTranslator()
    {
        // Can not be instantiated.
    }

    public final static DataSetProperty translate(final DataSetPropertyPE propertyPE)
    {
        final DataSetProperty result = new DataSetProperty();
        result.setValue(StringEscapeUtils.escapeHtml(propertyPE.tryGetUntypedValue()));
        result.setEntityTypePropertyType(DataSetTypePropertyTypeTranslator
                .translate((DataSetTypePropertyTypePE) propertyPE.getEntityTypePropertyType()));
        return result;

    }

    public final static List<DataSetProperty> translate(final Set<DataSetPropertyPE> list)
    {
        if (list == null)
        {
            return null;
        }
        final List<DataSetProperty> result = new ArrayList<DataSetProperty>();
        for (final DataSetPropertyPE property : list)
        {
            result.add(translate(property));
        }
        return result;
    }
}
