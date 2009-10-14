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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.PrimitiveValue;

/**
 * Tests of {@link TSVRenderer}
 * 
 * @author Tomasz Pylak
 */
public class TSVRendererTest
{
    @Test
    public void testRenderer()
    {
        List<IColumnDefinition<String[]>> columnDefs = createColumnDefs(2);
        List<String[]> entities = new ArrayList<String[]>();
        entities.add(new String[]
            { "x", "y" });
        entities.add(new String[]
            { "a", "b" });
        String content = TSVRenderer.createTable(asRowModel(entities), columnDefs, "#");
        Assert.assertEquals("h0\th1#x\ty#a\tb#", content);
    }

    public static <T> GridRowModels<T> asRowModel(List<T> entities)
    {
        List<GridRowModel<T>> list = new ArrayList<GridRowModel<T>>();
        for (T entity : entities)
        {
            list.add(new GridRowModel<T>(entity, new HashMap<String, PrimitiveValue>()));
        }
        return new GridRowModels<T>(list, new ArrayList<GridCustomColumnInfo>());
    }

    private static List<IColumnDefinition<String[]>> createColumnDefs(int colNum)
    {
        List<IColumnDefinition<String[]>> columnDefs = new ArrayList<IColumnDefinition<String[]>>();
        for (int i = 0; i < colNum; i++)
        {
            columnDefs.add(createColDef(i));
        }
        return columnDefs;
    }

    @Test
    public void testRendererNoRows()
    {
        List<IColumnDefinition<String[]>> columnDefs = createColumnDefs(2);
        List<String[]> entities = new ArrayList<String[]>();
        String content = TSVRenderer.createTable(asRowModel(entities), columnDefs, "\n");
        Assert.assertEquals("h0\th1\n", content);
    }

    private static IColumnDefinition<String[]> createColDef(final int colIx)
    {
        return new IColumnDefinition<String[]>()
            {

                public String getHeader()
                {
                    return "h" + colIx;
                }

                public String getIdentifier()
                {
                    return null;
                }

                public String getValue(GridRowModel<String[]> rowModel)
                {
                    return rowModel.getOriginalObject()[colIx];
                }

                public Comparable<?> getComparableValue(GridRowModel<String[]> rowModel)
                {
                    return getValue(rowModel);
                }

                public String tryToGetProperty(String key)
                {
                    return null;
                }

            };
    }
}
