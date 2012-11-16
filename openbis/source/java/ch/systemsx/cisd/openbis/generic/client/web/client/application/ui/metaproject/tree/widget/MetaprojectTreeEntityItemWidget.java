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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.model.MetaprojectTreeEntityItemData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;

/**
 * @author pkupczyk
 */
public class MetaprojectTreeEntityItemWidget extends MetaprojectTreeItemWidget
{

    public MetaprojectTreeEntityItemWidget(final IViewContext<?> viewContext,
            final MetaprojectTreeEntityItemData model)
    {
        super(viewContext);

        if (model.isEntityStub())
        {
            initWidget(new InlineLabel(model.getEntityLabel()));
        } else
        {
            ClickHandler listener = new ClickHandler()
                {
                    @Override
                    public void onClick(ClickEvent event)
                    {
                        OpenEntityDetailsTabHelper.open(viewContext, model.getEntity()
                                .getEntityKind(), model.getEntity().getPermId(), WidgetUtils
                                .ifSpecialKeyPressed(event.getNativeEvent()));
                    }
                };

            Widget link =
                    LinkRenderer.getLinkWidget(model.getEntityLabel(), listener,
                            LinkExtractor.tryExtract(model.getEntity()));
            initWidget(link);
        }
    }

}
