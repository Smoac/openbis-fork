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

import ch.systemsx.cisd.cifex.client.dto.File;

/**
 * A <code>AbstractFileGridModel</code> extension for uploaded files.
 * 
 * @author Izabela Adamczyk
 */
public class UploadedFileGridModel extends AbstractFileGridModel
{

    UploadedFileGridModel(IMessageResources messageResources)
    {
        super(messageResources);
    }

    public final List getColumnConfigs()
    {
        final List configs = new ArrayList();
        configs.add(createIdColumnConfig());
        configs.add(createNameColumnConfig());
        configs.add(createContentTypeColumnConfig());
        configs.add(createSizeColumnConfig());
        configs.add(createExpirationDateColumnConfig());
        return configs;
    }

    public final List getData(final Object[] data)
    {
        final List list = new ArrayList();
        for (int i = 0; i < data.length; i++)
        {
            final File file = (File) data[i];
            final Object[] objects =
                    new Object[]
                        { new Integer((int) file.getID()), file.getName(), file.getContentType(), file.getSize(),
                                file.getExpirationDate() };
            list.add(objects);
        }
        return list;
    }

    public final List getFieldDefs()
    {
        final List fieldDefs = new ArrayList();
        fieldDefs.add(new StringFieldDef(ID));
        fieldDefs.add(new StringFieldDef(NAME));
        fieldDefs.add(new StringFieldDef(CONTENT_TYPE));
        fieldDefs.add(new StringFieldDef(SIZE));
        fieldDefs.add(new DateFieldDef(EXPIRATION_DATE));
        return fieldDefs;
    }
}
