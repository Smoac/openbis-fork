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

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
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
        Panel content = new SimplePanel();
        content.setStyleName("linkDataViewContent");

        LinkDataSetAnchor anchor = LinkDataSetAnchor.tryCreateWithUrlAsText(dataset);

        if (anchor != null)
        {
            content.add(anchor);
        } else
        {
            content.add(new Label(viewContext
                    .getMessage(Dict.LINKED_DATA_SET_URL_NOT_AVAILABLE_MSG)));
        }

        add(content);
    }
}
