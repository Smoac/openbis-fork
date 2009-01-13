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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityType;

/**
 * The {@link LayoutContainer} extension for registering an entity.
 * 
 * @author Izabela Adamczyk
 */
abstract public class EntityRegistrationPanel<T extends ModelData, S extends ComboBox<T>> extends
        ContentPanel
{
    private final S entityTypeSelection;

    protected static String createId(EntityKind entityKind)
    {
        return GenericConstants.ID_PREFIX + entityKind.name().toLowerCase() + "-registration";
    }

    public EntityRegistrationPanel(final CommonViewContext viewContext, EntityKind entityKind,
            S entityTypeSelection)
    {
        this.entityTypeSelection = entityTypeSelection;
        setId(createId(entityKind));
        setScrollMode(Scroll.AUTO);
        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem(entityTypeSelection.getFieldLabel()
                + GenericConstants.LABEL_SEPARATOR));
        toolBar.add(new AdapterToolItem(entityTypeSelection));
        setTopComponent(toolBar);
        entityTypeSelection.addSelectionChangedListener(new EntityTypeSelectionChangeListener(
                viewContext, entityKind));
    }

    private class EntityTypeSelectionChangeListener extends SelectionChangedListener<T>
    {

        private Widget registrationWidget;

        private final CommonViewContext viewContext;

        private PreviousSelection previousSelection = new PreviousSelection();

        private final EntityKind entityKind;

        public EntityTypeSelectionChangeListener(CommonViewContext viewContext,
                EntityKind entityKind)
        {
            this.viewContext = viewContext;
            this.entityKind = entityKind;
        }

        @Override
        public void selectionChanged(final SelectionChangedEvent<T> se)
        {
            final T entityTypeModel = se.getSelectedItem();
            if (entityTypeModel != null)
            {
                final EntityType entityType = entityTypeModel.get(ModelDataPropertyNames.OBJECT);
                if (registrationWidget == null)

                {
                    showRegistrationForm(entityType);
                    previousSelection.update(entityTypeModel);
                } else
                {
                    new ConfirmationDialog(viewContext.getMessage(Dict.CONFIRM_TITLE), viewContext
                            .getMessage(Dict.CONFIRM_CLOSE_MSG))
                        {
                            @Override
                            protected void onYes()
                            {
                                showRegistrationForm(entityType);
                                previousSelection.update(entityTypeModel);
                            }

                            @Override
                            protected void onNo()
                            {
                                List<T> selection = new ArrayList<T>();
                                selection.add(previousSelection.getValue());
                                entityTypeSelection.disableEvents(true);
                                entityTypeSelection.setSelection(selection);
                                entityTypeSelection.disableEvents(false);
                            }
                        }.show();
                }
            }
        }

        private void showRegistrationForm(final EntityType entityType)
        {
            removeAll();
            registrationWidget =
                    viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                            entityType).createClientPlugin(entityKind)
                            .createRegistrationForEntityType(entityType);
            add(registrationWidget);
            layout();
        }

        private class PreviousSelection
        {
            T value;

            void update(T newValue)
            {
                this.value = newValue;
            }

            T getValue()
            {
                return value;
            }
        }
    }
}