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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;

public class SampleBrowser extends LayoutContainer
{

    public SampleBrowser(GenericViewContext viewContext)
    {
        setLayout(new RowLayout());

        CommonColumns commonColumns = new CommonColumns();
        ParentColumns parentColumns = new ParentColumns();
        PropertyColumns propertyColumns = new PropertyColumns();

        final SampleBrowserGrid grid =
                new SampleBrowserGrid(viewContext, commonColumns, parentColumns, propertyColumns);
        final SampleBrowserToolbar toolbar =
                new SampleBrowserToolbar(viewContext, grid, commonColumns, parentColumns,
                        propertyColumns);

        add(toolbar);
        add(grid, new RowData(1, 1));
    }
}