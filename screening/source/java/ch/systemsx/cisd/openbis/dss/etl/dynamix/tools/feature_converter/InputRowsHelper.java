/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.dynamix.tools.feature_converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.utilities.BeanUtils;

/**
 * @author Izabela Adamczyk
 */
class InputRowsHelper
{

    public static List<InputRowsNamedCollection> extract(List<InputRow> list)
    {
        Map<String, List<InputRow>> map = new HashMap<String, List<InputRow>>();
        for (InputRow r : list)
        {
            String name = r.getContainer();
            if (map.get(name) == null)
            {
                map.put(name, new ArrayList<InputRow>());
            }
            map.get(name).add(r);
        }
        ArrayList<InputRowsNamedCollection> result = new ArrayList<InputRowsNamedCollection>();
        for (String key : map.keySet())
        {
            InputRowsNamedCollection exp = new InputRowsNamedCollection();
            exp.setName(key);
            exp.setRows(map.get(key));
            result.add(exp);
        }
        return result;
    }

    public static List<String> toTsv(List<InputRow> list)
    {
        ArrayList<String> result = new ArrayList<String>();
        result.add(new TsvBuilder(OutputRow.getHeaderColumns()).toString());
        for (InputRow r : list)
        {
            result.add(new TsvBuilder(convert(r).getColumns()).toString());
        }
        return result;
    }

    private static OutputRow convert(InputRow in)
    {
        OutputRow outputRow = new OutputRow();
        BeanUtils.fillBean(OutputRow.class, outputRow, in);
        outputRow.setWellName(StringUtils.split(in.getIdentifier(), ":")[1]);
        String category = CategoryOracle.calculateCategory(in);
        outputRow.setCategory(category);
        return outputRow;
    }

}
