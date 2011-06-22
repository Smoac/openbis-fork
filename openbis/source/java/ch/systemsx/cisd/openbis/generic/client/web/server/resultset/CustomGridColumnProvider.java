/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomGridColumnGridColumnIDs.CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomGridColumnGridColumnIDs.DESCRIPTION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomGridColumnGridColumnIDs.EXPRESSION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomGridColumnGridColumnIDs.IS_PUBLIC;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomGridColumnGridColumnIDs.NAME;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomGridColumnGridColumnIDs.REGISTRATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomGridColumnGridColumnIDs.REGISTRATOR;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class CustomGridColumnProvider extends AbstractCommonTableModelProvider<GridCustomColumn>
{
    private final String gridId;

    public CustomGridColumnProvider(ICommonServer commonServer, String sessionToken, String gridId)
    {
        super(commonServer, sessionToken);
        this.gridId = gridId;
    }

    @Override
    protected TypedTableModel<GridCustomColumn> createTableModel()
    {
        List<GridCustomColumn> customColumns = commonServer.listGridCustomColumns(sessionToken, gridId);
        TypedTableModelBuilder<GridCustomColumn> builder = new TypedTableModelBuilder<GridCustomColumn>();
        builder.addColumn(CODE);
        builder.addColumn(NAME);
        builder.addColumn(DESCRIPTION);
        builder.addColumn(EXPRESSION).hideByDefault();
        builder.addColumn(IS_PUBLIC).hideByDefault();
        builder.addColumn(REGISTRATOR).hideByDefault();
        builder.addColumn(REGISTRATION_DATE).hideByDefault();
        for (GridCustomColumn gridCustomColumn : customColumns)
        {
            builder.addRow(gridCustomColumn);
            builder.column(CODE).addString(gridCustomColumn.getCode());
            builder.column(NAME).addString(gridCustomColumn.getName());
            builder.column(DESCRIPTION).addString(gridCustomColumn.getDescription());
            builder.column(EXPRESSION).addString(gridCustomColumn.getExpression());
            builder.column(IS_PUBLIC).addString(SimpleYesNoRenderer.render(gridCustomColumn.isPublic()));
            builder.column(REGISTRATOR).addPerson(gridCustomColumn.getRegistrator());
            builder.column(REGISTRATION_DATE).addDate(gridCustomColumn.getRegistrationDate());
        }
        return builder.getModel();
    }

}
