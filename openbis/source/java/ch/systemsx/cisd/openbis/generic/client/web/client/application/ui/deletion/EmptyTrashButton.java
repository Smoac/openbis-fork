/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.deletion;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.Event;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;

/**
 * @author Pawel Glyzewski
 */
public class EmptyTrashButton extends Button
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private boolean force = false;

    private final String emptyTrashLabel;

    private final String forceEmptyTrashLabel;

    private boolean mouseOver;

    public EmptyTrashButton(final IViewContext<ICommonClientServiceAsync> viewContext,
            final AbstractAsyncCallback<Void> callback)
    {
        super(viewContext.getMessage(Dict.BUTTON_EMPTY_TRASH));
        this.viewContext = viewContext;

        emptyTrashLabel = viewContext.getMessage(Dict.BUTTON_EMPTY_TRASH);
        forceEmptyTrashLabel = viewContext.getMessage(Dict.BUTTON_FORCE_EMPTY_TRASH);

        addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent be)
                {
                    invokeAction(callback);
                }
            });
    }

    private void invokeAction(final AbstractAsyncCallback<Void> callback)
    {
        new EmptyTrashConfirmationDialog(viewContext, isForceEmptyTrash(), callback).show();
    }

    @Override
    public void onComponentEvent(ComponentEvent ce)
    {
        super.onComponentEvent(ce);

        int eventType = ce.getEvent().getTypeInt();
        if ((eventType & Event.MOUSEEVENTS) > 0 || (eventType & Event.KEYEVENTS) > 0)
        {
            if ((ce.isAltKey() || ce.isControlKey()) && mouseOver)
            {
                setForce(true);
            } else
            {
                setForce(false);
            }
        }
    }

    @Override
    public void onMouseOver(ComponentEvent ce)
    {
        super.onMouseOver(ce);
        this.mouseOver = true;
        this.focus();
    }

    @Override
    public void onMouseOut(ComponentEvent ce)
    {
        super.onMouseOut(ce);
        this.mouseOver = false;
        setForce(false);
    }

    private boolean isForceEmptyTrash()
    {
        return force;
    }

    private void setForce(boolean force)
    {
        if (isForceEmptyTrash() == force)
        {
            return;
        }

        this.force = force;
        if (force)
        {
            this.setText(forceEmptyTrashLabel);
        } else
        {
            this.setText(emptyTrashLabel);
        }
    }
}
