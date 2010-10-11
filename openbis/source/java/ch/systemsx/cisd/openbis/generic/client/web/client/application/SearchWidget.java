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

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.TableRowLayout;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.EnterKeyListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ButtonWithLoadingMask;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
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
public final class SearchWidget extends LayoutContainer
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
        final SearchableEntitySelectionWidget comboBox =
                new SearchableEntitySelectionWidget(viewContext);
        comboBox.setStyleAttribute("marginRight", "3px");
        comboBox.setId(ENTITY_CHOOSER_ID);
        comboBox.setWidth(100);
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
        final String queryText = textField.getValue();
        if (StringUtils.isBlank(queryText))
        {
            return;
        }
        if (hasOnlyWildcards(queryText))
        {
            MessageBox.alert(viewContext.getMessage(Dict.MESSAGEBOX_WARNING),
                    viewContext.getMessage(Dict.TOO_GENERIC, queryText), null);
            return;
        }
        enableSearch(false);
        final SearchableEntity selectedSearchableEntityOrNull =
                entityChooser.getSelectedSearchableEntity();
        final boolean useWildcardSearchMode =
                viewContext.getDisplaySettingsManager().isUseWildcardSearchMode();

        final MatchingEntitiesPanel matchingEntitiesGrid =
                new MatchingEntitiesPanel(viewContext, selectedSearchableEntityOrNull, queryText,
                        useWildcardSearchMode);
        String title = createTabTitle(queryText);
        final AbstractTabItemFactory tabFactory =
                createTabFactory(matchingEntitiesGrid, title, viewContext);

        matchingEntitiesGrid.refresh(new IDataRefreshCallback()
            {
                public void postRefresh(boolean wasSuccessful)
                {
                    enableSearch(true);
                    if (wasSuccessful == false)
                    {
                        return;
                    }
                    if (matchingEntitiesGrid.getRowNumber() == 0)
                    {
                        Object[] msgParameters = (useWildcardSearchMode == true) ? new String[]
                            { queryText, "", "off", } : new String[]
                            { queryText, "not", "on" };
                        MessageBox.alert(viewContext.getMessage(Dict.MESSAGEBOX_WARNING),
                                viewContext.getMessage(Dict.NO_MATCH, msgParameters), null);
                        return;
                    } else
                    {
                        textField.reset();
                        DispatcherHelper.dispatchNaviEvent(tabFactory);
                    }
                }
            });
    }

    private String createTabTitle(final String queryText)
    {
        final String selectedText =
                entityChooser.getValue().get(ModelDataPropertyNames.DESCRIPTION);
        return viewContext.getMessage(Dict.GLOBAL_SEARCH, selectedText, queryText);
    }

    private static AbstractTabItemFactory createTabFactory(
            final MatchingEntitiesPanel matchingEntitiesPanel, final String title,
            IViewContext<?> viewContext)
    {
        final ITabItem tab =
                DefaultTabItem.create(title, matchingEntitiesPanel.asDisposableComponent(),
                        viewContext);
        // this tab cannot be opened for the second time, so we can create it outside of the
        // factory
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    return tab;
                }

                @Override
                public String getId()
                {
                    return matchingEntitiesPanel.getId();
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SEARCH, HelpPageAction.ACTION);
                }

                @Override
                public String getTabTitle()
                {
                    return title;
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    private static boolean hasOnlyWildcards(final String queryText)
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
        return onlyWildcard;
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
