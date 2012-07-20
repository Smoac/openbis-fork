/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkDataSet;

/**
 * @author pkupczyk
 */
public class LinkDataViewSection extends TabContent
{
    private final LinkDataSet dataset;

    public LinkDataViewSection(final IViewContext<?> viewContext, final LinkDataSet dataset)
    {
        super(viewContext.getMessage(Dict.DATA_VIEW), viewContext, dataset);
        this.dataset = dataset;
        setIds(DisplayTypeIDGenerator.DATA_SET_DATA_SECTION);
    }

    @Override
    protected void showContent()
    {
        Panel content = new FlowPanel();
        content.setStyleName("linkDataViewContent");
        content.add(createInfoWidget());
        content.add(createLinkWidget());
        add(content);

    }

    private Widget createInfoWidget()
    {
        Label label = new Label(viewContext.getMessage(Dict.LINKED_DATA_SET_INFO));
        label.addStyleName("linkDataViewInfo");
        return label;
    }

    private Widget createLinkWidget()
    {
        LinkDataSetAnchor anchor = LinkDataSetAnchor.tryCreate(dataset);

        if (anchor != null)
        {
            Label label = new Label(viewContext.getMessage(Dict.LINKED_DATA_SET_LINK));
            label.addStyleName("linkDataViewLabel");

            anchor.setText(StringEscapeUtils.unescapeHtml(dataset.getExternalDataManagementSystem()
                    .getLabel() + ":" + StringEscapeUtils.unescapeHtml(dataset.getExternalCode())));

            Panel panel = new HorizontalPanel();
            panel.add(label);
            panel.add(anchor);
            return panel;
        } else
        {
            return new Label(viewContext.getMessage(Dict.LINKED_DATA_SET_LINK_NOT_AVAILABLE_MSG));
        }
    }
}
