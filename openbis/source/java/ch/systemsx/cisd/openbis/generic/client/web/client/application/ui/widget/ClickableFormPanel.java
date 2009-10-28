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

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ClickListenerCollection;
import com.google.gwt.user.client.ui.SourcesClickEvents;

/**
 * A clickable {@link FormPanel}.
 * 
 * @author Christian Ribeaud
 */
public final class ClickableFormPanel extends FormPanel implements SourcesClickEvents
{

    private final ClickListenerCollection clickListeners = new ClickListenerCollection();

    //
    // SourcesClickEvents
    //

    public final void addClickListener(final ClickListener listener)
    {
        clickListeners.add(listener);
    }

    public final void removeClickListener(final ClickListener listener)
    {
        clickListeners.remove(listener);
    }

    //
    // FormPanel
    //

    @Override
    protected final void afterRender()
    {
        super.afterRender();
        sinkEvents(Event.ONCLICK);
    }

    @Override
    protected final void onClick(final ComponentEvent ce)
    {
        super.onClick(ce);
        clickListeners.fireClick(this);
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
