/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.page.tab;

import ch.systemsx.cisd.openbis.uitest.infra.Browser;
import ch.systemsx.cisd.openbis.uitest.infra.Cell;
import ch.systemsx.cisd.openbis.uitest.infra.Row;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.Grid;

public class PropertyTypeBrowser implements Browser<PropertyType>
{

    @Locate("openbis_property-type-browser-grid")
    private Grid grid;

    @SuppressWarnings("unused")
    @Locate("openbis_property-type-browser-grid-add-button")
    private Button add;

    @SuppressWarnings("unused")
    @Locate("openbis_property-type-browser-grid-delete-button")
    private Button delete;

    @Override
    public Row row(PropertyType type)
    {
        return grid.getRow("Code", type.getCode());
    }

    @Override
    public Cell cell(PropertyType type, String column)
    {
        return row(type).get(column);
    }

}
