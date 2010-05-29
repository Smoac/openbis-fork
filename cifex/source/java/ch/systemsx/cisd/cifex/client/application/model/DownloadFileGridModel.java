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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.utils.FileUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;

/**
 * A <code>AbstractFileGridModel</code> extension for downloaded files.
 * 
 * @author Izabela Adamczyk
 */
public class DownloadFileGridModel extends AbstractFileGridModel
{
    private static final long serialVersionUID = Constants.VERSION;

    public DownloadFileGridModel(IMessageResources messageResources, FileInfoDTO file)
    {
        super(messageResources);
        set(ID, file.getID());// long
        set(NAME, file.getName());// String
        set(COMMENT, file.getComment());// String
        set(CONTENT_TYPE, file.getContentType());// String
        set(SIZE, FileUtils.tryToGetFileSize(file));// Integer
        set(OWNER, file.getOwner());// String
        set(CRC32_CHECKSUM, file.getCrc32Str());// String
        set(REGISTRATION_DATE, file.getRegistrationDate());// Date
        set(EXPIRATION_DATE, file.getExpirationDate());// Date
    }

    public long getID()
    {
        return get(ID);
    }

    public final static List<ColumnConfig> getColumnConfigs(IMessageResources messageResources)
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(createIdColumnConfig());
        configs.add(createNameColumnConfig(messageResources));
        configs.add(createCommentColumnConfig(messageResources));
        configs.add(createContentTypeColumnConfig(messageResources));
        configs.add(createSizeColumnConfig(messageResources));
        configs.add(createOwnerColumnConfig(messageResources));
        configs.add(createCRC32ChecksumColumnConfig(messageResources));
        configs.add(createRegistrationDateColumnConfig(messageResources));
        configs.add(createExpirationDateColumnConfig(messageResources));
        return configs;
    }

    public final static List<AbstractFileGridModel> convert(IMessageResources messageResources,
            final List<FileInfoDTO> files)
    {
        final List<AbstractFileGridModel> result = new ArrayList<AbstractFileGridModel>();

        for (final FileInfoDTO file : files)
        {
            result.add(new DownloadFileGridModel(messageResources, file));
        }

        return result;
    }
}
