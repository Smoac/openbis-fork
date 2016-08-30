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

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.TableRowLayout;
import com.google.gwt.user.client.History;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.GlobalSearchLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SearchableEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.EnterKeyListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ButtonWithLoadingMask;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Type;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;

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
public final class SearchWidget extends LayoutContainer
{
    private static final String PREFIX = GenericConstants.ID_PREFIX + "search-widget_";

    static final String TEXT_FIELD_ID = PREFIX + "text-field";

    static final String CHECKBOX_FIELD_ID = PREFIX + "wildcard-checkbox";

    static final String SUBMIT_BUTTON_ID = PREFIX + "submit-button";

    static final String ENTITY_CHOOSER_ID = PREFIX + "entity-chooser";

    private final SearchableEntitySelectionWidget entityChooser;

    private final TextField<String> textField;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final ButtonWithLoadingMask searchButton;

    private final EnterKeyListener enterKeyListener;

    private final CheckBox useWildcardsCheckBox;

    public SearchWidget(final IViewContext<ICommonClientServiceAsync> viewContext)
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
        useWildcardsCheckBox = createCheckBox();
        add(entityChooser);
        add(textField);
        add(useWildcardsCheckBox);
        add(searchButton);

        Dispatcher.get().addListener(AppEvents.GLOBAL_SEARCH_STARTED_EVENT, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    searchButton.setEnabled(false);
                }
            });

        Dispatcher.get().addListener(AppEvents.GLOBAL_SEARCH_FINISHED_EVENT, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    searchButton.setEnabled(true);
                }
            });

        layout();
    }

    private final SearchableEntitySelectionWidget createEntityChooser()
    {
        final SearchableEntitySelectionWidget comboBox =
                new SearchableEntitySelectionWidget(viewContext);
        comboBox.setStyleAttribute("marginRight", "3px");
        comboBox.setId(ENTITY_CHOOSER_ID);
        comboBox.setWidth(150);
        comboBox.addSelectionChangedListener(new SelectionChangedListener<SearchableEntityModel>()
            {
                
                @Override
                public void selectionChanged(SelectionChangedEvent<SearchableEntityModel> se)
                {
                    SearchableEntity searchableEntity = se.getSelectedItem().getSearchableEntity();
                    Type type = searchableEntity.getType();
                    useWildcardsCheckBox.setEnabled(Type.SEARCH_DOMAIN.equals(type) == false ||
                        searchableEntity.getPossibleSearchOptionsKey() != null); 
                }
            });
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

    private final CheckBox createCheckBox()
    {
        final CheckBox field = new CheckBox();
        field.setId(CHECKBOX_FIELD_ID);
        field.setBoxLabel(viewContext.getMessage(Dict.USE_WILDCARD_CHECKBOX_TEXT));
        field.setTitle(viewContext.getMessage(Dict.USE_WILDCARD_CHECKBOX_TOOLTIP));
        field.setStyleAttribute("marginRight", "3px");
        return field;
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
        final String queryText = textField.getValue();
        if (StringUtils.isBlank(queryText))
        {
            return;
        }
        if (hasOnlyWildcards(queryText))
        {
            GWTUtils.alert(viewContext.getMessage(Dict.MESSAGEBOX_WARNING),
                    viewContext.getMessage(Dict.TOO_GENERIC, queryText));
            return;
        }

        // reset the text field
        textField.setValue("");
        SearchableEntity selectedEntity = entityChooser.getSelectedSearchableEntity();

        Boolean useWildcards = useWildcardsCheckBox.getValue();

        if (viewContext.isSimpleOrEmbeddedMode())
        {
            // redirect to another URL
            String entityDescription = (selectedEntity != null) ? selectedEntity.getName() : null;
            String url = createGlobalSearchLink(entityDescription, queryText, useWildcards);
            History.newItem(url);
        } else
        {
            GlobalSearchTabItemFactory.openTabIfEntitiesFound(viewContext, selectedEntity,
                    queryText, useWildcards);
        }
    }

    private static boolean hasOnlyWildcards(final String queryText)
    {
        boolean onlyWildcard = true;
        for (final char c : queryText.toCharArray())
        {
            if (c != '*' && c != '?' && c != '|') // '|||' is a delimiter in field global_search in index
            {
                onlyWildcard = false;
                break;
            }
        }
        return onlyWildcard;
    }

    public static String createGlobalSearchLink(String searchableEntity, String queryText, boolean useWildcards)
    {
        // forward to a new url
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(ViewLocator.ACTION_PARAMETER,
                GlobalSearchLocatorResolver.GLOBAL_SEARCH_ACTION);
        if (searchableEntity != null)
        {
            url.addParameter(GlobalSearchLocatorResolver.ENTITY_PARAMETER_KEY, searchableEntity);
        }
        url.addParameter(GlobalSearchLocatorResolver.QUERY_PARAMETER_KEY, queryText);
        if (useWildcards)
        {
            url.addParameter(GlobalSearchLocatorResolver.USE_WILDCARDS_PARAMETER_KEY, "true");
        }
        return url.toStringWithoutDelimiterPrefix();
    }

    private final ButtonWithLoadingMask createSearchButton()
    {
        final ButtonWithLoadingMask button =
                new ButtonWithLoadingMask(viewContext.getMessage(Dict.SEARCH_BUTTON),
                        SUBMIT_BUTTON_ID)
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

}
