/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.ui.HTML;

/**
 * Helper methods that create a panel for displaying the results of an aggregation service. This
 * panel is not used directly in the openBIS UI. It is used to support implementers of web UIs and
 * give them an opportunity to leverage our powerful table grid framework.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class AggregationServicePanelHelper
{
    public static final Component createAggregationServicePanel(IViewContext<?> viewContext,
            String idPrefix)
    {
        final LayoutContainer layoutContainer = new LayoutContainer(new BorderLayout());
        layoutContainer.setStyleAttribute("background-color", "white");
        layoutContainer.setId(idPrefix + "aggregation_service");
        HTML content = new HTML("<p>Welcome to aggregation services</p>");
        layoutContainer.add(content, new BorderLayoutData(LayoutRegion.CENTER, 1f));
        return layoutContainer;
    }

}
