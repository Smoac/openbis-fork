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

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.widgets.layout.ContentPanel;

import ch.systemsx.cisd.cifex.client.application.ui.FileUploadWidget;

/**
 * The main page for non-administrators (permanent and temporary users).
 * 
 * @author Franz-Josef Elmer
 */
final class MainPage extends AbstractMainPage
{
    MainPage(final ViewContext context)
    {
        super(context);
    }

    private final static Widget createPartTitle(final String text)
    {
        final HTML html = new HTML(text);
        html.setStyleName("cifex-heading");
        return html;
    }

    private final HTML createExplanationPanel()
    {
        return new HTML(context.getMessageResources().getUploadFilesHelp());
    }

    //
    // AbstractMainPage
    //

    protected final ContentPanel createMainPanel()
    {
        final ContentPanel contentPanel = new ContentPanel("Main-Page");
        final VerticalPanel verticalPanel = new VerticalPanel();
        contentPanel.setWidth("100%");
        verticalPanel.setSpacing(5);
        verticalPanel.add(createPartTitle(context.getMessageResources().getUploadFilesPartTitle()));
        verticalPanel.add(createExplanationPanel());
        verticalPanel.add(new FileUploadWidget(context));
        contentPanel.add(verticalPanel);
        return contentPanel;
    }

}
