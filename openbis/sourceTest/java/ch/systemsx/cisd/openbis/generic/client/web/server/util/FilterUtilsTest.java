/*
 * Copyright 2009 ETH Zuerich, CISD
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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ParameterWithValue;
import ch.systemsx.cisd.openbis.generic.client.web.server.calculator.GridExpressionUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;

/**
 * @author Franz-Josef Elmer
 */
public class FilterUtilsTest extends AssertJUnit
{
    private static final class Data
    {
        private double value;

        public void setValue(double value)
        {
            this.value = value;
        }

        public double getValue()
        {
            return value;
        }
    }

    @Test
    public void test()
    {
        CustomFilterInfo<Data> filterInfo = new CustomFilterInfo<Data>();
        filterInfo.setExpression("row.col('VALUE') < ${threshold}");
        ParameterWithValue parameter = new ParameterWithValue();
        parameter.setParameter("threshold");
        parameter.setValue("42");
        filterInfo.setParameters(new LinkedHashSet<ParameterWithValue>(Arrays.asList(parameter)));
        Set<IColumnDefinition<Data>> availableColumns =
                new LinkedHashSet<IColumnDefinition<Data>>();
        availableColumns.add(new AbstractColumnDefinition<Data>("header", 100, true)
            {

                public String getIdentifier()
                {
                    return "VALUE";
                }

                @Override
                protected String tryGetValue(Data entity)
                {
                    return Double.toString(entity.getValue());
                }

                @Override
                public Comparable<?> getComparableValue(GridRowModel<Data> rowModel)
                {
                    return rowModel.getOriginalObject().getValue();
                }
            });

        GridRowModels<Data> filterdList =
                GridExpressionUtils.applyCustomFilter(createData(57, 34), availableColumns,
                        filterInfo);

        assertEquals(1, filterdList.size());
        assertEquals(34.0, filterdList.get(0).getOriginalObject().getValue());
    }

    private GridRowModels<Data> createData(double... values)
    {
        List<Data> list = new ArrayList<Data>();
        for (double value : values)
        {
            Data data = new Data();
            data.setValue(value);
            list.add(data);
        }
        return TSVRendererTest.asRowModel(list);
    }

}
