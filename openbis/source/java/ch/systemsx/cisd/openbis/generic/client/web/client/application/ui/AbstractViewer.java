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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractViewer<D extends IEntityInformationHolder> extends ContentPanel
{

    public static final String ID_EDIT_SUFFIX = "_edit";

    public static final String ID_DELETE_SUFFIX = "_edit";

    private ToolBar toolBar;

    private final List<Button> toolBarButtons = new ArrayList<Button>();

    private final IViewContext<?> viewContext;

    protected Button editButton;

    private LabelToolItem titleLabel;

    private D originalData;

    public AbstractViewer(final IViewContext<?> viewContext, String id)
    {
        this(viewContext, null, id, true); // title is set later with updateTitle method
    }

    public AbstractViewer(final IViewContext<?> viewContext, String title, String id,
            boolean withToolBar)
    {
        this.viewContext = viewContext;
        setId(id);
        setHeaderVisible(false);
        if (withToolBar)
        {
            toolBar = new ToolBar();
            setTopComponent(toolBar);
            titleLabel = new LabelToolItem(title);
            toolBar.add(titleLabel);
            toolBar.add(new FillToolItem());
            editButton = createEditButton();
            addToolBarButton(editButton);
        }
    }

    private Button createEditButton()
    {
        Button result = new Button(viewContext.getMessage(Dict.BUTTON_EDIT));
        result.setId(getId() + ID_EDIT_SUFFIX);
        result.addListener(Events.Select, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    showEntityEditor();
                }
            });
        result.disable();
        return result;
    }

    protected Button createDeleteButton(final IDelegatedAction deleteAction)
    {
        Button result = new Button(viewContext.getMessage(Dict.BUTTON_DELETE));
        result.setId(getId() + ID_DELETE_SUFFIX);
        result.addListener(Events.Select, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    deleteAction.execute();
                }
            });
        result.disable();
        return result;
    }

    protected final void addToolBarButton(Button button)
    {
        toolBarButtons.add(button);
        toolBar.add(button);
    }

    protected final void updateTitle(String title)
    {
        titleLabel.setLabel(title);
    }

    protected final String getBaseIndexURL()
    {
        return GWTUtils.getBaseIndexURL();
    }

    protected D getOriginalData()
    {
        assert originalData != null : "data is not yet set";
        return originalData;
    }

    protected List<D> getOriginalDataAsSingleton()
    {
        return Collections.singletonList(getOriginalData());
    }

    protected void showEntityEditor()
    {
        assert originalData != null : "data is not yet set";
        showEntityEditor(originalData.getEntityKind(), originalData.getEntityType(), originalData);
    }

    private final void showEntityEditor(EntityKind entityKind, BasicEntityType type,
            IIdentifiable identifiable)
    {
        assert type != null : "entity type is not provided";
        final ITabItemFactory tabView;
        final IClientPluginFactory clientPluginFactory =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        type);
        final IClientPlugin<SampleType, IIdentifiable> createClientPlugin =
                clientPluginFactory.createClientPlugin(entityKind);
        tabView = createClientPlugin.createEntityEditor(identifiable);
        DispatcherHelper.dispatchNaviEvent(tabView);
    }

    public static String getTitle(final IMessageProvider messageProvider,
            final String entityKindDictKey, final ICodeProvider codeProvider)
    {
        return messageProvider.getMessage(Dict.DETAILS_TITLE, messageProvider
                .getMessage(entityKindDictKey), codeProvider.getCode());
    }

    /** Updates data displayed in the browser (needed to open editor view). */
    protected void updateOriginalData(D newData)
    {
        this.originalData = newData;
        updateTitle(getOriginalDataDescription());
        setToolBarButtonsEnabled(true);
    }

    /** Updates data displayed in the browser when shown data has been removed from DB. */
    public void setupRemovedEntityView()
    {
        removeAll();
        updateTitle(getOriginalDataDescription() + " does not exist any more.");
        setToolBarButtonsEnabled(false);
    }

    protected void setToolBarButtonsEnabled(boolean enabled)
    {
        for (Button button : toolBarButtons)
        {
            button.setEnabled(enabled);
        }
    }

    protected String getOriginalDataDescription()
    {
        return originalData.getEntityKind().getDescription() + " " + originalData.getCode() + " ["
                + originalData.getEntityType().getCode() + "]";
    }

    protected final static BorderLayoutData createLeftBorderLayoutData()
    {
        return BorderLayoutDataFactory.create(LayoutRegion.WEST, 300);
    }

    protected final static BorderLayoutData createRightBorderLayoutData()
    {
        return createBorderLayoutData(LayoutRegion.CENTER);
    }

    protected final static BorderLayoutData createBorderLayoutData(LayoutRegion region)
    {
        return BorderLayoutDataFactory.create(region);
    }

    protected final AbstractAsyncCallback<Void> createDeletionCallback()
    {
        return new CloseViewerCallback(viewContext);
    }

    private final class CloseViewerCallback extends AbstractAsyncCallback<Void>
    {
        public CloseViewerCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(Void result)
        {
            fireEvent(AppEvents.CloseViewer);
        }
    }

}
