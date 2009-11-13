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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * A clickable {@link FormPanel}.
 * 
 * @author Christian Ribeaud
 */
public final class ClickableFormPanel extends FormPanel
{
    // FIXME: check clicking on panels

    public final void addClickListener(final ClickHandler handler)
    {
        addDomHandler(handler, ClickEvent.getType());
    }

    @Override
    protected final void afterRender()
    {
        super.afterRender();
    }

    /**
     * Validates and scrolls to the first invalid field.
     */
    @Override
    public boolean isValid()
    {
        boolean valid = super.isValid();
        if (valid == false)
        {
            for (Field<?> field : getFields())
            {
                if (field.isValid() == false)
                {
                    field.getElement().scrollIntoView();
                    break;
                }
            }
        }
        return valid;
    }
}
