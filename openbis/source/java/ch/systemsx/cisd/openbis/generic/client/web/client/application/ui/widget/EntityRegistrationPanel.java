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
import java.util.Set;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.DropDownList;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;

/**
 * The {@link LayoutContainer} extension for registering an entity.
 * 
 * @author Izabela Adamczyk
 */
abstract public class EntityRegistrationPanel<T extends ModelData, S extends DropDownList<T, ?>>
        extends ContentPanel implements IDatabaseModificationObserver
{
    private final S entityTypeSelection;

    private final CommonViewContext viewContext;

    private final EntityKind entityKind;

    private DatabaseModificationAwareWidget registrationWidget;

    private PreviousSelection previousSelection = new PreviousSelection();

    protected static String createId(EntityKind entityKind)
    {
        return GenericConstants.ID_PREFIX + entityKind.name().toLowerCase() + "-registration";
    }

    public EntityRegistrationPanel(final CommonViewContext viewContext, EntityKind entityKind,
            S entityTypeSelection)
    {
        this.entityTypeSelection = entityTypeSelection;
        this.viewContext = viewContext;
        this.entityKind = entityKind;

        setHeaderVisible(false);
        setId(createId(entityKind));
        setScrollMode(Scroll.AUTO);
        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem(entityTypeSelection.getFieldLabel()
                + GenericConstants.LABEL_SEPARATOR));
        toolBar.add(new AdapterToolItem(entityTypeSelection));
        setTopComponent(toolBar);
        entityTypeSelection.addSelectionChangedListener(createSelectionChangedListener());
    }

    private SelectionChangedListener<T> createSelectionChangedListener()
    {
        return new SelectionChangedListener<T>()
            {
                @Override
                public void selectionChanged(final SelectionChangedEvent<T> se)
                {
                    final T entityTypeModel = se.getSelectedItem();
                    if (entityTypeModel != null)
                    {
                        onSelectionChanged(entityTypeModel);
                    }
                }
            };
    }

    private void onSelectionChanged(final T entityTypeModel)
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

    private void showRegistrationForm(final EntityType entityType)
    {
        removeAll();
        final IClientPlugin<EntityType, EntityTypePropertyType<EntityType>, EntityProperty<EntityType, EntityTypePropertyType<EntityType>>, IIdentifierHolder, ?> clientPlugin =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        entityType).createClientPlugin(entityKind);
        registrationWidget = clientPlugin.createRegistrationForEntityType(entityType);
        add(registrationWidget.get());
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

    private IDatabaseModificationObserver createCompositeDatabaseModificationObserver()
    {
        CompositeDatabaseModificationObserver observer =
                new CompositeDatabaseModificationObserver();
        if (registrationWidget != null)
        {
            observer.addObserver(registrationWidget);
        }
        observer.addObserver(entityTypeSelection);
        return observer;
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return createCompositeDatabaseModificationObserver().getRelevantModifications();
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        createCompositeDatabaseModificationObserver().update(observedModifications);
    }
}