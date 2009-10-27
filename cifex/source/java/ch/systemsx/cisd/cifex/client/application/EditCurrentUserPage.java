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

package ch.systemsx.cisd.cifex.client.application;

import com.extjs.gxt.ui.client.widget.LayoutContainer;

import ch.systemsx.cisd.cifex.client.application.IHistoryController.Page;
import ch.systemsx.cisd.cifex.client.application.ui.EditUserWidget;

/**
 * An <code>AbstractMainPage</code> extension for administrators.
 * 
 * @author Franz-Josef Elmer
 */
final class EditCurrentUserPage extends AbstractMainPage
{

    EditCurrentUserPage(final ViewContext context)
    {
        super(context);
    }

    @Override
    protected final LayoutContainer createMainPanel()
    {
        final LayoutContainer mainPanel = new LayoutContainer();
        mainPanel.add(createEditUserWidget(context));
        return mainPanel;
    }

    static private final LayoutContainer createEditUserWidget(ViewContext context)
    {
        // Otherwise the user can remove its own admin rights.
        boolean allowPermanentUsers = false;
        LayoutContainer editUserPanel = createContainer();
        addTitlePart(editUserPanel, context.getMessageResources().getEditUserLabel());
        final EditUserWidget editUserWidget =
                new EditUserWidget(context, allowPermanentUsers, context.getModel().getUser(), true)
                    {

                        @Override
                        protected final void finishEditing()
                        {
                            final IPageController pageController = context.getPageController();
                            final IHistoryController historyController =
                                    context.getHistoryController();
                            final Page previousPage = historyController.getPreviousPage();
                            assert previousPage != null : "Undefined previous page.";
                            pageController.createPage(previousPage);
                        }
                    };
        editUserPanel.add(editUserWidget);
        return editUserPanel;
    }

}
