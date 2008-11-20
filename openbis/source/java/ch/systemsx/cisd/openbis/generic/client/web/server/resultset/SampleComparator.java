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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import org.apache.commons.collections.comparators.BooleanComparator;

import ch.systemsx.cisd.common.utilities.FieldComparator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;

/**
 * A {@link IFieldComparator} suitable for {@link Sample}.
 * 
 * @author Christian Ribeaud
 */
final class SampleComparator implements IFieldComparator<Sample>
{
    private FieldComparator<Sample> fieldComparator;

    private String fieldName;

    private final int compareBoolean(final boolean b1, final boolean b2)
    {
        return BooleanComparator.getTrueFirstComparator().compare(b1, b2);
    }

    //
    // IFieldComparator
    //

    public final void setFieldName(final String fieldName)
    {
        assert fieldName != null : "Unspecified field name.";
        this.fieldName = fieldName;
        fieldComparator = new FieldComparator<Sample>(fieldName, '_');
    }

    public final int compare(final Sample o1, final Sample o2)
    {
        assert fieldName != null : "Field name not specified.";
        if (fieldName.equals(ModelDataPropertyNames.IS_GROUP_SAMPLE))
        {
            final boolean b1 = o1.getGroup() != null;
            final boolean b2 = o2.getGroup() != null;
            return compareBoolean(b1, b2);
        } else if (fieldName.equals(ModelDataPropertyNames.IS_INSTANCE_SAMPLE))
        {
            final boolean b1 = o1.getDatabaseInstance() != null;
            final boolean b2 = o2.getDatabaseInstance() != null;
            return compareBoolean(b1, b2);
        } else if (fieldName.equals(ModelDataPropertyNames.IS_INVALID))
        {
            final boolean b1 = o1.getInvalidation() != null;
            final boolean b2 = o2.getInvalidation() != null;
            return compareBoolean(b1, b2);
        }
        return fieldComparator.compare(o1, o2);
    }
}