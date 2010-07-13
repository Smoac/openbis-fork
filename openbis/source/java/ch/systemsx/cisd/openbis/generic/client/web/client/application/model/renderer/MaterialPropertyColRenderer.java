/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.EntityPropertyColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;

/**
 * An {@link AbstractPropertyColRenderer} which renders value preserving newlines.
 * 
 * @author Piotr Buczek
 */
class MaterialPropertyColRenderer<T extends IEntityPropertiesHolder> extends
        AbstractPropertyColRenderer<T>
{
    final IViewContext<?> viewContext;

    public MaterialPropertyColRenderer(final IViewContext<?> viewContext,
            EntityPropertyColDef<T> colDef)
    {
        super(colDef);
        this.viewContext = viewContext;
    }

    @Override
    protected String renderValue(GridRowModel<T> entity)
    {
        String value = colDef.getValue(entity);
        final MaterialIdentifier identifier = MaterialIdentifier.tryParseIdentifier(value);
        // TODO 2010-07-13, Piotr Buczek: cannot create link in default mode
        // listener will not work after calling Widget.toString()
        if (identifier != null && viewContext.isSimpleMode())
        {
            String href = LinkExtractor.tryExtract(identifier);
            final Widget link = LinkRenderer.getLinkWidget(identifier.getCode(), null, href);

            FlowPanel panel = new FlowPanel();
            panel.add(link);
            panel.add(new InlineHTML(" [" + identifier.getTypeCode() + "]"));
            return panel.toString();
        } else
        {
            return value;
        }
    }
}
