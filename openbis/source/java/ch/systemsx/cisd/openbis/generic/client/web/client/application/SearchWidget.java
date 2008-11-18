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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.List;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.TableRowLayout;
import com.google.gwt.user.client.ui.Image;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.EnterKeyListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;

/**
 * A <code>LayoutContainer</code> extension for searching.
 * <p>
 * It is composed of:
 * <ul>
 * <li>An entity chooser</li>
 * <li>An input field</li>
 * <li>A submit button</li>
 * </ul>
 * </p>
 * 
 * @author Christian Ribeaud
 */
final class SearchWidget extends LayoutContainer
{
    private static final String PREFIX = GenericConstants.ID_PREFIX + "search-widget_";

    static final String TEXT_FIELD_ID = PREFIX + "text-field";

    static final String SUBMIT_BUTTON_ID = PREFIX + "submit-button";

    static final String ENTITY_CHOOSER_ID = PREFIX + "entity-chooser";

    private final EntityChooser entityChooser;

    private final TextField<String> textField;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private final Button searchButton;

    private final EnterKeyListener enterKeyListener;

    private final Image loadingImage;

    private boolean searching;

    SearchWidget(final IViewContext<IGenericClientServiceAsync> viewContext)
    {
        final TableRowLayout tableRowLayout = createLayout();
        setLayout(tableRowLayout);
        this.viewContext = viewContext;
        enterKeyListener = new EnterKeyListener()
            {

                //
                // EnterKeyListener
                //

                @Override
                protected final void onEnterKey()
                {
                    if (searching == false)
                    {
                        doSearch();
                    }
                }
            };
        textField = createTextField();
        entityChooser = createEntityChooser();
        searchButton = createSearchButton();
        add(entityChooser);
        add(textField);
        add(searchButton);
        loadingImage = createLoadingImage();
        add(loadingImage);
        layout();
    }

    private final void enableSearch(final boolean enable)
    {
        searching = enable == false;
        searchButton.setEnabled(enable);
        loadingImage.setVisible(enable == false);
    }

    private final static Image createLoadingImage()
    {
        final Image image = new Image("images/loading.gif");
        image.setVisible(false);
        return image;
    }

    private final EntityChooser createEntityChooser()
    {
        final EntityChooser comboBox = new EntityChooser(viewContext);
        comboBox.setStyleAttribute("marginRight", "3px");
        comboBox.setId(ENTITY_CHOOSER_ID);
        return comboBox;
    }

    private final static TableRowLayout createLayout()
    {
        final TableRowLayout tableRowLayout = new TableRowLayout();
        tableRowLayout.setBorder(0);
        tableRowLayout.setCellPadding(0);
        tableRowLayout.setCellSpacing(0);
        return tableRowLayout;
    }

    private final TextField<String> createTextField()
    {
        final TextField<String> field = new TextField<String>();
        field.setId(TEXT_FIELD_ID);
        field.setWidth(200);
        field.addKeyListener(enterKeyListener);
        return field;
    }

    private final void doSearch()
    {
        final SearchableEntity selectedSearchableEntityOrNull =
                entityChooser.tryGetSelectedSearchableEntity();
        final String queryText = textField.getValue();
        if (StringUtils.isBlank(queryText) == false)
        {
            enableSearch(false);
            viewContext.getService()
                    .listMatchingEntities(
                            selectedSearchableEntityOrNull,
                            queryText,
                            createResultSetConfig(),
                            new SearchResultCallback(viewContext, selectedSearchableEntityOrNull,
                                    queryText));
        }
    }

    private final static DefaultResultSetConfig<String> createResultSetConfig()
    {
        final DefaultResultSetConfig<String> resultSetConfig = new DefaultResultSetConfig<String>();
        resultSetConfig.setLimit(MatchingEntitiesPanel.PAGE_SIZE);
        return resultSetConfig;
    }

    private final Button createSearchButton()
    {
        final Button button =
                new Button(viewContext.getMessageProvider().getMessage("search_button"));
        button.setId(SUBMIT_BUTTON_ID);
        button.setStyleAttribute("marginLeft", "3px");
        button.setStyleAttribute("marginRight", "3px");
        button.addSelectionListener(new SelectionListener<ComponentEvent>()
            {

                //
                // SelectionListener
                //

                @Override
                public final void componentSelected(final ComponentEvent ce)
                {
                    doSearch();
                }

            });
        return button;
    }

    //
    // Helper classes
    //

    public final class SearchResultCallback extends
            AbstractAsyncCallback<ResultSet<MatchingEntity>>
    {
        private final SearchableEntity searchableEntityOrNull;

        private final String queryText;

        SearchResultCallback(final IViewContext<IGenericClientServiceAsync> viewContext,
                final SearchableEntity searchableEntityOrNull, final String queryText)
        {
            super(viewContext);
            this.searchableEntityOrNull = searchableEntityOrNull;
            this.queryText = queryText;
        }

        @SuppressWarnings("unchecked")
        private final IViewContext<IGenericClientServiceAsync> castViewContext()
        {
            return (IViewContext<IGenericClientServiceAsync>) viewContext;
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected final void finishOnFailure(final Throwable caught)
        {
            enableSearch(true);
        }

        @Override
        protected final void process(final ResultSet<MatchingEntity> result)
        {
            enableSearch(true);
            final List<MatchingEntity> entities = result.getList();
            if (entities.size() == 0)
            {
                final IMessageProvider messageProvider = viewContext.getMessageProvider();
                MessageBox.alert(messageProvider.getMessage("messagebox_warning"), messageProvider
                        .getMessage("no_match", queryText), new Listener<WindowEvent>()
                    {

                        //
                        // Listener
                        //

                        public final void handleEvent(final WindowEvent be)
                        {
                            textField.focus();
                        }
                    });
                return;
            }
            textField.reset();
            final AppEvent<ITabItem> event = new AppEvent<ITabItem>(AppEvents.NAVI_EVENT);
            final String selectedText =
                    entityChooser.getValue().get(ModelDataPropertyNames.DESCRIPTION);
            final MatchingEntitiesPanel matchingEntitiesPanel =
                    new MatchingEntitiesPanel(castViewContext(), searchableEntityOrNull, queryText);
            event.data =
                    new DefaultTabItem(viewContext.getMessageProvider().getMessage("global_search",
                            selectedText, queryText), matchingEntitiesPanel, matchingEntitiesPanel);
            matchingEntitiesPanel.setFirstResulSet(result);
            Dispatcher.get().dispatch(event);
        }
    }

}
