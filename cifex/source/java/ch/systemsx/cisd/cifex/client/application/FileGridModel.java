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

import com.gwtext.client.data.DateFieldDef;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.widgets.grid.ColumnConfig;

import ch.systemsx.cisd.cifex.client.application.model.AbstractDataGridModel;
import ch.systemsx.cisd.cifex.client.application.ui.DateRenderer;
import ch.systemsx.cisd.cifex.client.application.ui.LinkRenderer;
import ch.systemsx.cisd.cifex.client.dto.File;

/**
 * A <code>AbstractDataGridModel</code> extension suitable for {@link File}.
 * 
 * @author Christian Ribeaud
 */
final class FileGridModel extends AbstractDataGridModel
{

    static final String NAME = "name";

    private static final String CONTENT_TYPE = "contentType";

    private static final String SIZE = "size";

    private static final String EXPIRATION_DATE = "expirationDate";

    FileGridModel(final IMessageResources messageResources)
    {
        super(messageResources);
    }

    //
    // AbstractDataGridModel
    //

    public final List getColumnConfigs()
    {
        final List configs = new ArrayList();
        final ColumnConfig nameConfig = createSortableColumnConfig(NAME, messageResources.getFileNameLabel(), 100);
        nameConfig.setRenderer(LinkRenderer.LINK_RENDERER);
        configs.add(nameConfig);
        configs.add(createSortableColumnConfig(CONTENT_TYPE, messageResources.getFileContentTypeLabel(), 120));
        configs.add(createSortableColumnConfig(SIZE, messageResources.getFileSizeLabel(), 120));
        final ColumnConfig expirationDateConfig =
                createSortableColumnConfig(EXPIRATION_DATE, messageResources.getFileExpirationDateLabel(), 140);
        expirationDateConfig.setRenderer(DateRenderer.DATE_RENDERER);
        configs.add(expirationDateConfig);
        return configs;
    }

    public final List getData(final Object[] data)
    {
        final List list = new ArrayList();
        for (int i = 0; i < data.length; i++)
        {
            final File file = (File) data[i];
            final Object[] objects = new Object[]
                { file.getName(), file.getContentType(), file.getSize(), file.getExpirationDate() };
            list.add(objects);
        }
        return list;
    }

    public final List getFieldDefs()
    {
        final List fieldDefs = new ArrayList();
        fieldDefs.add(new StringFieldDef(NAME));
        fieldDefs.add(new StringFieldDef(CONTENT_TYPE));
        fieldDefs.add(new StringFieldDef(SIZE));
        fieldDefs.add(new DateFieldDef(EXPIRATION_DATE));
        return fieldDefs;
    }

}
