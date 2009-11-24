/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;

/**
 * Utilities for {@link Field} class.
 * 
 * @author Tomasz Pylak
 */
public class FieldUtil
{
    private static final String MANDATORY_LABEL_SEPARATOR = ":&nbsp;*";

    public static void setMandatoryFlag(Field<?> field, boolean isMandatory)
    {
        if (isMandatory)
        {
            markAsMandatory(field);
        }
    }

    public static void setMandatoryFlag(TextField<?> field, boolean isMandatory)
    {
        if (isMandatory)
        {
            markAsMandatory(field);
        } else
        {
            field.setAllowBlank(true);
        }
    }

    public static void markAsMandatory(TextField<?> field)
    {
        markAsMandatory((Field<?>) field);
        field.setAllowBlank(false);
    }

    public static void markAsMandatory(Field<?> field)
    {
        field.setLabelSeparator(MANDATORY_LABEL_SEPARATOR);
    }

    /** makes all given fields visible or invisible including validation dependent on given value */
    public static void setVisibility(boolean visible, Field<?>... fields)
    {
        for (Field<?> field : fields)
        {
            setVisibility(field, visible);
        }
    }

    /** makes field visible or invisible including validation dependent on given value */
    private static void setVisibility(Field<?> field, boolean visible)
    {
        field.setEnabled(visible);
        field.setVisible(visible);
        field.syncSize();
        if (visible == false)
        {
            // invalidation mark is not removed automatically when we make field invisible
            field.clearInvalid();
        } else if (field.isDirty())
        {
            // validate only if something have been modified and field is shown
            field.validate();
        }
    }
}
