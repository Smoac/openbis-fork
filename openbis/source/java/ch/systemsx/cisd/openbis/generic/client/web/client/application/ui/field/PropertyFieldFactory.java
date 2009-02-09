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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import java.util.Date;

import com.extjs.gxt.ui.client.widget.form.Field;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

public class PropertyFieldFactory
{
    /**
     * Creates a field for given data type.
     */
    public static Field<?> createField(final PropertyType pt, boolean isMandatory, String label,
            String fieldId)
    {
        final Field<?> field;
        final DataTypeCode dataType = pt.getDataType().getCode();
        switch (dataType)
        {
            case BOOLEAN:
                field = new CheckBoxField(label, isMandatory);
                break;
            case VARCHAR:
                field = new VarcharField(label, isMandatory);
                break;
            case TIMESTAMP:
                field = new DateFormField(label, isMandatory);
                break;
            case CONTROLLEDVOCABULARY:
                field =
                        new ControlledVocabullaryField(label, isMandatory, pt.getVocabulary()
                                .getTerms());
                break;
            case INTEGER:
                field = new IntegerField(label, isMandatory);
                break;
            case REAL:
                field = new RealField(label, isMandatory);
                break;
            default:
                field = new VarcharField(label, isMandatory);
                break;
        }
        field.setId(fieldId);
        return field;
    }

    public static final String valueToString(final Object value)
    {
        if (value == null)
        {
            return null;
        } else if (value instanceof Date)
        {
            return DateRenderer.renderDate((Date) value);
        } else
        {
            return value.toString();
        }
    }
}