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

package ch.systemsx.cisd.cifex.client.application.ui;

import com.gwtext.client.data.Record;
import com.gwtext.client.data.Store;
import com.gwtext.client.widgets.grid.CellMetadata;
import com.gwtext.client.widgets.grid.Renderer;

import ch.systemsx.cisd.cifex.client.application.Constants;
import ch.systemsx.cisd.cifex.client.application.utils.FileUtils;

/**
 * A <code>Renderer</code> implementation for file size.
 * 
 * @author Christian Ribeaud
 */
public final class FileSizeRenderer implements Renderer
{

    public final static FileSizeRenderer FILE_SIZE_RENDERER = new FileSizeRenderer();

    private FileSizeRenderer()
    {
        // Can not be instantiated.
    }

    //
    // Renderer
    //

    public final String render(final Object value, final CellMetadata cellMetadata,
            final Record record, final int rowIndex, final int colNum, final Store store)
    {
        if (value == null)
        {
            return Constants.TABLE_NULL_VALUE;
        }
        return FileUtils.byteCountToDisplaySize(((Number) value).longValue());
    }

}
