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

package ch.systemsx.cisd.datamover.console.client.application.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.datamover.console.client.application.ViewContext;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class HeaderFooterDecorator extends Composite
{
    public HeaderFooterDecorator(Widget widget, ViewContext viewContext)
    {
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setStyleName("border");
        verticalPanel.setSpacing(10);
        verticalPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        verticalPanel.add(createHeader(viewContext));
        verticalPanel.add(widget);
        verticalPanel.add(createFooter(viewContext));
        
        initWidget(verticalPanel);
    }
    
    private Widget createHeader(ViewContext viewContext)
    {
        HorizontalPanel headerPanel = new HorizontalPanel();
        headerPanel.setSpacing(10);
        headerPanel.setStyleName("header");
        
        headerPanel.add(viewContext.getImageBundle().getLogo().createImage());
        Label title = new Label(viewContext.getMessageResources().getHeaderTitle());
        title.setStyleName("header-title");
        headerPanel.add(title);
        return headerPanel;
    }
    
    private Widget createFooter(ViewContext viewContext)
    {
        final HorizontalPanel footerPanel = new HorizontalPanel();
        final String version = viewContext.getModel().getApplicationInfo().getVersion();
        final Label label = new Label(viewContext.getMessageResources().getFooterText(version));
        label.setStyleName("footer");
        footerPanel.add(label);
        return footerPanel;
    }

}
