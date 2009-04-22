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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.google.gwt.user.client.Event;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SimpleDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * A dialog which lets the user to choose one entity from the provided entity browser.
 * 
 * @author Tomasz Pylak
 */
class EntityChooserDialog<T> extends SimpleDialog
{

    private static final int PAGING_TOOLBAR_HEIGHT = 65;

    private static final int WIDTH = 730;

    private static final int HEIGHT = 600;

    private final DisposableEntityChooser<T> entityBrowser;

    private final IDelegatedAction onAcceptAction;

    private final IDelegatedAction onCancelAction;

    interface ChosenEntitySetter<T>
    {
        void setChosenEntity(T entity);
    }

    public EntityChooserDialog(DisposableEntityChooser<T> entityBrowser,
            ChosenEntitySetter<T> chosenEntitySetter, String title, IMessageProvider messageProvider)
    {
        super(entityBrowser.getComponent(), title, messageProvider.getMessage(Dict.BUTTON_CHOOSE),
                messageProvider);
        this.entityBrowser = entityBrowser;
        this.onAcceptAction = createAcceptAction(chosenEntitySetter, entityBrowser);
        this.onCancelAction = createCancelAction(chosenEntitySetter, entityBrowser);

        entityBrowser.getComponent().setHeight("" + (HEIGHT - PAGING_TOOLBAR_HEIGHT));
        setWidth(WIDTH);
        setHeight(HEIGHT);
        sinkEvents(Event.ONDBLCLICK);

        setAcceptAction(onAcceptAction);
        setCancelAction(onCancelAction);
        setModal(true);

    }

    @Override
    public void onComponentEvent(ComponentEvent ce)
    {
        if (ce.type == Event.ONDBLCLICK)
        {
            if (entityBrowser.tryGetSingleSelected() != null)
            {
                onAcceptAction.execute();
                hide();
                return;
            }
        }
        super.onComponentEvent(ce);
    }

    private static <T> IDelegatedAction createAcceptAction(
            final ChosenEntitySetter<T> chosenEntitySetter,
            final DisposableEntityChooser<T> entityBrowser)
    {
        return new IDelegatedAction()
            {
                public void execute()
                {
                    entityBrowser.dispose();
                    T selected = entityBrowser.tryGetSingleSelected();
                    chosenEntitySetter.setChosenEntity(selected);
                }
            };
    }

    private static <T> IDelegatedAction createCancelAction(
            final ChosenEntitySetter<T> chosenEntitySetter,
            final IDisposableComponent componentDisposer)
    {
        return new IDelegatedAction()
            {
                public void execute()
                {
                    componentDisposer.dispose();
                    chosenEntitySetter.setChosenEntity(null);
                }
            };
    }
}