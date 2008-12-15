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

import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.TableRowLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.EnterKeyListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ButtonWithLoadingMask;
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

    private final SearchableEntitySelectionWidget entityChooser;

    private final TextField<String> textField;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final ButtonWithLoadingMask searchButton;

    private final EnterKeyListener enterKeyListener;

    SearchWidget(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final TableRowLayout tableRowLayout = createLayout();
        setLayout(tableRowLayout);
        this.viewContext = viewContext;
        searchButton = createSearchButton();
        enterKeyListener = new EnterKeyListener()
            {

                //
                // EnterKeyListener
                //

                @Override
                protected final void onEnterKey()
                {
                    doSearch();
                }
            };
        textField = createTextField();
        entityChooser = createEntityChooser();
        add(entityChooser);
        add(textField);
        add(searchButton);
        layout();
    }

    private final void enableSearch(final boolean enable)
    {
        searchButton.setEnabled(enable);
    }

    private final SearchableEntitySelectionWidget createEntityChooser()
    {
        final SearchableEntitySelectionWidget comboBox = new SearchableEntitySelectionWidget(viewContext);
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
        field.setStyleAttribute("marginRight", "3px");
        return field;
    }

    private final void doSearch()
    {
        // Do not trigger another search when already searching.
        if (searchButton.isEnabled() == false)
        {
            return;
        }
        final SearchableEntity selectedSearchableEntityOrNull =
                entityChooser.getSelectedSearchableEntity();
        final String queryText = textField.getValue();
        if (StringUtils.isBlank(queryText) == false)
        {
            boolean onlyWildcard = true;
            for (final char c : queryText.toCharArray())
            {
                if (c != '*' && c != '?')
                {
                    onlyWildcard = false;
                    break;
                }
            }
            if (onlyWildcard)
            {
                MessageBox.alert(viewContext.getMessage(Dict.MESSAGEBOX_WARNING), viewContext.getMessage(
                        Dict.TOO_GENERIC, queryText), null);
                return;
            }
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

    private final ButtonWithLoadingMask createSearchButton()
    {
        final ButtonWithLoadingMask button =
                new ButtonWithLoadingMask(viewContext.getMessage(Dict.SEARCH_BUTTON), SUBMIT_BUTTON_ID)
                    {
                        //
                        // ButtonWithLoadingMask
                        //

                        @Override
                        public final void doButtonClick()
                        {
                            doSearch();
                        }
                    };
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

        SearchResultCallback(final IViewContext<ICommonClientServiceAsync> viewContext,
                final SearchableEntity searchableEntityOrNull, final String queryText)
        {
            super(viewContext);
            this.searchableEntityOrNull = searchableEntityOrNull;
            this.queryText = queryText;
        }

        @SuppressWarnings("unchecked")
        private final IViewContext<ICommonClientServiceAsync> castViewContext()
        {
            return (IViewContext<ICommonClientServiceAsync>) viewContext;
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
                MessageBox.alert(viewContext.getMessage(Dict.MESSAGEBOX_WARNING), viewContext.getMessage(
                        Dict.NO_MATCH, queryText), null);
                return;
            }
            textField.reset();
            final AppEvent<ITabItem> event = new AppEvent<ITabItem>(AppEvents.NAVI_EVENT);
            final String selectedText =
                    entityChooser.getValue().get(ModelDataPropertyNames.DESCRIPTION);
            final MatchingEntitiesPanel matchingEntitiesPanel =
                    new MatchingEntitiesPanel(castViewContext(), searchableEntityOrNull, queryText);
            event.data =
                    new DefaultTabItem(viewContext.getMessage(Dict.GLOBAL_SEARCH, selectedText,
                            queryText), matchingEntitiesPanel, matchingEntitiesPanel);
            matchingEntitiesPanel.setFirstResulSet(result);
            Dispatcher.get().dispatch(event);
        }
    }

}
