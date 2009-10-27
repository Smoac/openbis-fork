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

package ch.systemsx.cisd.cifex.client.application.ui;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.LayoutData;
import com.google.gwt.user.client.ui.Widget;

/**
 * Allows to add the fields to the form column in a convenient way.
 * 
 * @author Izabela Adamczyk
 */
class FormColumn extends LayoutContainer
{
    private static final int COLUMN_WIDTH = 360;

    private final FormData formData;

    public FormColumn(FormData formData)
    {
        this.formData = formData;
        setLayout(new FormLayout());
        setWidth(COLUMN_WIDTH);
    }

    /**
     * Adds the field to the form using common column settings (form data).
     */
    public void addField(Field<?> field)
    {
        super.add(field, formData);
    }

    /**
     * Adds the field to the form using common column settings (form data).
     */
    public void addFieldSet(FieldSet fieldSet)
    {
        super.add(fieldSet, formData);
    }

    /**
     * @deprecated Use {@link #addField(Field)} instead.
     */
    @Deprecated
    @Override
    public boolean add(Widget widget, LayoutData layoutData)
    {
        return super.add(widget, layoutData);
    }

    /**
     * @deprecated Use {@link #addField(Field)} instead.
     */
    @Deprecated
    @Override
    protected boolean add(Component item)
    {
        return super.add(item);
    }

}