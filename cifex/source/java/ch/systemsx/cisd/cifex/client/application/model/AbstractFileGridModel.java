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

package ch.systemsx.cisd.cifex.client.application.model;

import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.*;

import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.grid.AbstractFilterField;
import ch.systemsx.cisd.cifex.client.application.ui.CommentRenderer;
import ch.systemsx.cisd.cifex.client.application.ui.DateRenderer;
import ch.systemsx.cisd.cifex.client.application.ui.FileSizeRenderer;
import ch.systemsx.cisd.cifex.client.application.ui.LinkRenderer;
import ch.systemsx.cisd.cifex.client.application.ui.UserDescriptionRenderer;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;

/**
 * A <code>AbstractDataGridModel</code> extension suitable for {@link FileInfoDTO}.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractFileGridModel extends AbstractDataGridModel
{
    private static final long serialVersionUID = Constants.VERSION;

    public static final String NAME = "name";

    public static final String COMMENT = "comment";

    public static final String CONTENT_TYPE = "contentType";

    public static final String SIZE = "size";

    public static final String COMPLETE_SIZE = "completeSize";

    public static final String IS_COMPLETE = "isComplete";

    public static final String CRC32_CHECKSUM = "crc32Checksum";

    public static final String EXPIRATION_DATE = "expirationDate";

    public static final String REGISTRATION_DATE = "registrationDate";

    public static final String OWNER = "owner";

    public static final String REGISTRATOR = "registrator";

    public static final String SHARED_WITH = "sharedWith";

    public static final String ACTION = "action";

    AbstractFileGridModel(final IMessageResources messageResources)
    {
        super(messageResources);
    }

    protected final static ColumnConfig createNameColumnConfig(IMessageResources messageResources)
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(NAME, msg(LIST_FILES_NAME_COLUMN_HEADER), 140);
        columnConfig.setRenderer(LinkRenderer.LINK_RENDERER);
        return columnConfig;
    }

    protected final static ColumnConfig createCommentColumnConfig(IMessageResources messageResources)
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(COMMENT, msg(LIST_FILES_COMMENT_COLUMN_HEADER), 140);
        columnConfig.setRenderer(CommentRenderer.COMMENT_RENDERER);
        return columnConfig;
    }

    protected final static ColumnConfig createExpirationDateColumnConfig(
            IMessageResources messageResources)
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(EXPIRATION_DATE,
                        msg(LIST_FILES_EXPIRATIONDATE_COLUMN_HEADER), 120);
        columnConfig.setRenderer(DateRenderer.DATE_RENDERER);
        return columnConfig;
    }

    protected final static ColumnConfig createRegistrationDateColumnConfig(
            IMessageResources messageResources)
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(REGISTRATION_DATE,
                        msg(LIST_FILES_REGISTRATIONDATE_COLUMN_HEADER), 150);
        columnConfig.setRenderer(DateRenderer.FULL_DATE_RENDERER);
        return columnConfig;
    }

    /**
     * Note that this column is not sortable.
     */
    protected final static ColumnConfig createOwnerColumnConfig(IMessageResources messageResources)
    {
        final ColumnConfig columnConfig =
                createColumnConfig(OWNER, msg(LIST_FILES_OWNER_COLUMN_HEADER), 120);
        columnConfig.setRenderer(UserDescriptionRenderer.USER_DESCRIPTION_RENDERER);
        return columnConfig;
    }

    protected final static ColumnConfig createRegistratorColumnConfig(
            IMessageResources messageResources)
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(REGISTRATOR, msg(LIST_FILES_UPLOADER_COLUMN_HEADER), 80);
        columnConfig.setHidden(true);
        return columnConfig;
    }

    /**
     * Note that this column is not sortable.
     */
    protected final static ColumnConfig createSharedWithColumnConfig(
            IMessageResources messageResources)
    {
        final ColumnConfig columnConfig =
                createColumnConfig(SHARED_WITH, msg(LIST_FILES_SHAREDWITH_COLUMN_HEADER), 120);
        return columnConfig;
    }

    static protected final ColumnConfig createActionColumnConfig(IMessageResources messageResources)
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(ACTION, msg(LIST_USERSFILES_ACTIONS_COLUMN_HEADER), 180);
        return columnConfig;
    }

    protected final static ColumnConfig createSizeColumnConfig(IMessageResources messageResources)
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(SIZE, msg(LIST_FILES_SIZE_COLUMN_HEADER), 80);
        columnConfig.setRenderer(FileSizeRenderer.FILE_SIZE_RENDERER);
        return columnConfig;
    }

    protected final static ColumnConfig createCompleteSizeColumnConfig(
            IMessageResources messageResources)
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(COMPLETE_SIZE,
                        msg(LIST_FILES_COMPLETESIZE_COLUMN_HEADER), 80);
        columnConfig.setRenderer(FileSizeRenderer.FILE_SIZE_RENDERER);
        columnConfig.setHidden(true);
        return columnConfig;
    }

    protected final static ColumnConfig createIsCompleteColumnConfig(
            IMessageResources messageResources)
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(IS_COMPLETE, msg(LIST_FILES_ISCOMPLETE_COLUMN_HEADER),
                        80);
        return columnConfig;
    }

    protected final static ColumnConfig createCRC32ChecksumColumnConfig(
            IMessageResources messageResources)
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(CRC32_CHECKSUM,
                        msg(LIST_FILES_CRC32CHECKSUM_COLUMN_HEADER), 80);
        columnConfig.setHidden(true);
        return columnConfig;
    }

    protected final static ColumnConfig createContentTypeColumnConfig(
            IMessageResources messageResources)
    {
        return createSortableColumnConfig(CONTENT_TYPE, msg(LIST_FILES_CONTENTTYPE_COLUMN_HEADER),
                140);
    }

    public static <M extends ModelData> List<AbstractFilterField<M>> createFilterItems(
            IMessageResources messageResources, List<ColumnConfig> columnConfigs)
    {
        return createFilterItems(columnConfigs, getInitialFilters());
    }

    private static List<String> getInitialFilters()
    {
        return Arrays.asList(AbstractFileGridModel.NAME, AbstractFileGridModel.COMMENT,
                AbstractFileGridModel.CONTENT_TYPE);
    }
}
