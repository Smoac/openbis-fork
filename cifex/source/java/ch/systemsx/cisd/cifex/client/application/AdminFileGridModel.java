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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

import ch.systemsx.cisd.cifex.client.application.ui.CommentRenderer;
import ch.systemsx.cisd.cifex.client.application.ui.UserRenderer;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.client.application.utils.FileUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.AdminFileInfoDTO;

/**
 * A <code>AbstractFileGridModel</code> extension for files in the administration.
 * 
 * @author Izabela Adamczyk
 */
public class AdminFileGridModel extends AbstractFileGridModel
{
    private static final long serialVersionUID = Constants.VERSION;

    public final static List<ColumnConfig> getColumnConfigs(IMessageResources messageResources)
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(createIdColumnConfig());
        configs.add(createNameColumnConfig(messageResources));
        configs.add(createCommentColumnConfig(messageResources));
        configs.add(createRegistererColumnConfig(messageResources));
        configs.add(createSharedWithColumnConfig(messageResources));
        configs.add(createContentTypeColumnConfig(messageResources));
        configs.add(createSizeColumnConfig(messageResources));
        configs.add(createCompleteSizeColumnConfig(messageResources));
        configs.add(createIsCompleteColumnConfig(messageResources));
        configs.add(createRegistrationDateColumnConfig(messageResources));
        configs.add(createExpirationDateColumnConfig(messageResources));
        configs.add(createActionColumnConfig(messageResources));
        return configs;
    }

    public AdminFileGridModel(IMessageResources messageResources, AdminFileInfoDTO file)
    {
        super(messageResources);
        set(ID, file.getID());// long
        set(NAME, file.getName());// String
        set(COMMENT, CommentRenderer.createCommentAnchor(file));// String
        set(OWNER, UserRenderer.createUserAnchor(file.getOwner()));// String
        set(SHARED_WITH, UserRenderer.createUserAnchor(file.getSharingUsers()));// String
        set(CONTENT_TYPE, file.getContentType());// String
        set(SIZE, FileUtils.tryToGetFileSize(file));// Double
        set(COMPLETE_SIZE, new Double(file.getCompleteSize()));// Double
        set(IS_COMPLETE, Boolean.valueOf(file.isComplete()));// Boolean
        set(REGISTRATION_DATE, file.getRegistrationDate());// Date
        set(EXPIRATION_DATE, file.getExpirationDate());// Date
        set(ACTION, DOMUtils.createAnchor(messageResources.getActionRenewLabel(),
                Constants.RENEW_ID)
                + " | "
                + DOMUtils.createAnchor(messageResources.getActionDeleteLabel(),
                        Constants.DELETE_ID));// String
    }

    public final static List<AbstractFileGridModel> convert(IMessageResources messageResources,
            final List<AdminFileInfoDTO> filters)
    {
        final List<AbstractFileGridModel> result = new ArrayList<AbstractFileGridModel>();

        for (final AdminFileInfoDTO filter : filters)
        {
            result.add(new AdminFileGridModel(messageResources, filter));
        }

        return result;
    }
}
