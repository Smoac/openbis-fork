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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.widget;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.model.MetaprojectTreeMetaprojectItemData;

/**
 * @author pkupczyk
 */
public class MetaprojectTreeMetaprojectItemWidget extends MetaprojectTreeItemWidget
{

    private Widget link;

    public MetaprojectTreeMetaprojectItemWidget(IViewContext<?> viewContext,
            MetaprojectTreeMetaprojectItemData model)
    {
        super(viewContext);

        // TODO make a real link that opens a metaproject detail view
        link = new InlineLabel(viewContext.getMessage(Dict.METAPROJECT_TREE_INFO_LINK));
        link.setVisible(isSelected());

        FlowPanel panel = new FlowPanel();
        panel.add(new InlineLabel(model.getMetaproject().getName() + " "));
        panel.add(link);

        FocusPanel focusPanel = new FocusPanel();
        focusPanel.add(panel);
        focusPanel.addMouseOverHandler(new MouseOverHandler()
            {
                @Override
                public void onMouseOver(MouseOverEvent event)
                {
                    link.setVisible(true);
                }
            });
        focusPanel.addMouseOutHandler(new MouseOutHandler()
            {
                @Override
                public void onMouseOut(MouseOutEvent event)
                {
                    link.setVisible(isSelected());
                }
            });

        initWidget(focusPanel);
    }

    @Override
    protected void onSelectedChange()
    {
        link.setVisible(isSelected());
    }

}
