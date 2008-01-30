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

import com.gwtext.client.widgets.grid.ColumnConfig;

import ch.systemsx.cisd.cifex.client.application.model.AbstractDataGridModel;
import ch.systemsx.cisd.cifex.client.application.ui.DateRenderer;
import ch.systemsx.cisd.cifex.client.application.ui.LinkRenderer;
import ch.systemsx.cisd.cifex.client.application.ui.UserRenderer;
import ch.systemsx.cisd.cifex.client.dto.File;

/**
 * A <code>AbstractDataGridModel</code> extension suitable for {@link File}.
 * 
 * @author Christian Ribeaud
 */
abstract class AbstractFileGridModel extends AbstractDataGridModel
{

    static final String NAME = "name";

    protected static final String CONTENT_TYPE = "contentType";

    protected static final String SIZE = "size";

    protected static final String EXPIRATION_DATE = "expirationDate";

    protected static final String REGISTERER = "registerer";

    protected static final String ACTION = "action";

    AbstractFileGridModel(final IMessageResources messageResources)
    {
        super(messageResources);
    }

    protected ColumnConfig createNameColumnConfig()
    {
        final ColumnConfig nameConfig = createSortableColumnConfig(NAME, messageResources.getFileNameLabel(), 100);
        nameConfig.setRenderer(LinkRenderer.LINK_RENDERER);
        return nameConfig;
    }

    protected ColumnConfig createExpirationDateColumnConfig()
    {
        final ColumnConfig expirationDateConfig =
                createSortableColumnConfig(EXPIRATION_DATE, messageResources.getFileExpirationDateLabel(), 140);
        expirationDateConfig.setRenderer(DateRenderer.DATE_RENDERER);
        return expirationDateConfig;
    }

    protected ColumnConfig createRegistererColumnConfig()
    {
        final ColumnConfig registererConfig =
                createSortableColumnConfig(REGISTERER, messageResources.getFileRegistererLabel(), 120);
        registererConfig.setRenderer(UserRenderer.USER_RENDERER);
        return registererConfig;
    }

    protected ColumnConfig createActionColumnConfig()
    {
        final ColumnConfig registererConfig =
                createSortableColumnConfig(ACTION, messageResources.getActionLabel(), 120);
        registererConfig.setRenderer(LinkRenderer.LINK_RENDERER);
        return registererConfig;
    }

}
