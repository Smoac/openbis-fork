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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.EntityPropertyColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ExternalHyperlink;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;

/**
 * An {@link AbstractPropertyColRenderer} which renders value as an external hyperlink.
 * 
 * @author Piotr Buczek
 */
class HyperlinkPropertyColRenderer<T extends IEntityPropertiesHolder> extends
        AbstractPropertyColRenderer<T>
{

    public HyperlinkPropertyColRenderer(EntityPropertyColDef<T> colDef)
    {
        super(colDef);
    }

    @Override
    protected String renderValue(GridRowModel<T> entity)
    {
        String value = colDef.getValue(entity);
        return ExternalHyperlink.createAnchorString(value, value);
    }

}
