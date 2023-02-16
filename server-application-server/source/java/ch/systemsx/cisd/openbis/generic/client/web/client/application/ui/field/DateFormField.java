/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

import com.extjs.gxt.ui.client.widget.form.DateField;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;

/**
 * A {@link DateField} extension suitable for registering a date.
 * 
 * @author Christian Ribeaud
 */
public final class DateFormField extends DateField
{
    public DateFormField(final String fieldLabel, final boolean mandatory)
    {
        VarcharField.configureField(this, fieldLabel, mandatory);
        getPropertyEditor().setFormat(DateRenderer.DEFAULT_DATE_TIME_FORMAT);
        final String pattern = DateRenderer.DEFAULT_DATE_FORMAT_PATTERN;
        setEmptyText(pattern.toUpperCase());
        setMaxLength(pattern.length() + 6);
    }
}
