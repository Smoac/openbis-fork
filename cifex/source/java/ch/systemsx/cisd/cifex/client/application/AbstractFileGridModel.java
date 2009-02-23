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
import ch.systemsx.cisd.cifex.client.application.ui.FileSizeRenderer;
import ch.systemsx.cisd.cifex.client.application.ui.LinkRenderer;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;

/**
 * A <code>AbstractDataGridModel</code> extension suitable for {@link FileInfoDTO}.
 * 
 * @author Christian Ribeaud
 */
abstract class AbstractFileGridModel extends AbstractDataGridModel
{

    protected static final String ID = "id";

    protected static final String NAME = "name";

    protected static final String COMMENT = "comment";

    protected static final String CONTENT_TYPE = "contentType";

    protected static final String SIZE = "size";

    protected static final String EXPIRATION_DATE = "expirationDate";

    protected static final String REGISTRATION_DATE = "registrationDate";

    protected static final String REGISTERER = "registerer";

    protected static final String SHARED_WITH = "sharedWith";

    protected static final String ACTION = "action";

    AbstractFileGridModel(final IMessageResources messageResources)
    {
        super(messageResources);
    }

    protected final ColumnConfig createIdColumnConfig()
    {
        final ColumnConfig columnConfig = createSortableColumnConfig(ID, "Id", 20);
        columnConfig.setHidden(true);
        return columnConfig;
    }

    protected final ColumnConfig createNameColumnConfig()
    {
        final ColumnConfig nameConfig =
                createSortableColumnConfig(NAME, messageResources.getFileNameLabel(), 140);
        nameConfig.setRenderer(LinkRenderer.LINK_RENDERER);
        return nameConfig;
    }

    protected final ColumnConfig createCommentColumnConfig()
    {
        final ColumnConfig commentConfig =
                createSortableColumnConfig(COMMENT, messageResources.getFileCommentLabel(), 140);
        return commentConfig;
    }

    protected final ColumnConfig createExpirationDateColumnConfig()
    {
        final ColumnConfig expirationDateConfig =
                createSortableColumnConfig(EXPIRATION_DATE, messageResources
                        .getFileExpirationDateLabel(), 200);
        expirationDateConfig.setRenderer(DateRenderer.DATE_RENDERER);
        return expirationDateConfig;
    }

    protected final ColumnConfig createRegistrationDateColumnConfig()
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(REGISTRATION_DATE, messageResources
                        .getFileRegistrationDateLabel(), 200);
        columnConfig.setRenderer(DateRenderer.DATE_RENDERER);
        return columnConfig;
    }

    /**
     * Note that this column is not sortable.
     */
    protected final ColumnConfig createRegistererColumnConfig()
    {
        final ColumnConfig registererConfig =
                createColumnConfig(REGISTERER, messageResources.getFileRegistratorLabel(), 120);
        return registererConfig;
    }

    /**
     * Note that this column is not sortable.
     */
    protected final ColumnConfig createSharedWithColumnConfig()
    {
        final ColumnConfig registererConfig =
                createColumnConfig(SHARED_WITH, messageResources.getFileSharedWithLabel(), 120);
        return registererConfig;
    }

    protected final ColumnConfig createActionColumnConfig()
    {
        final ColumnConfig registererConfig =
                createSortableColumnConfig(ACTION, messageResources.getActionLabel(), 80);
        return registererConfig;
    }

    protected final ColumnConfig createSizeColumnConfig()
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(SIZE, messageResources.getFileSizeLabel(), 60);
        columnConfig.setRenderer(FileSizeRenderer.FILE_SIZE_RENDERER);
        return columnConfig;
    }

    protected final ColumnConfig createContentTypeColumnConfig()
    {
        return createSortableColumnConfig(CONTENT_TYPE, messageResources.getFileContentTypeLabel(),
                140);
    }

}
