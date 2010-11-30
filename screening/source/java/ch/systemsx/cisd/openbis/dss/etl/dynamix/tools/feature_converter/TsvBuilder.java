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

import java.util.List;

/**
 * @author Izabela Adamczyk
 */
public class TsvBuilder
{
    static final String TSV_SEPARATOR = ";";

    StringBuilder builder = new StringBuilder();

    boolean empty = true;

    public TsvBuilder()
    {
    }

    public TsvBuilder(List<String> columns)
    {
        addColumns(columns.toArray(new String[0]));
    }

    public void addColumns(String... columns)
    {
        for (String c : columns)
        {
            if (empty)
            {
                empty = false;
            } else
            {
                builder.append(TsvBuilder.TSV_SEPARATOR);
            }
            builder.append(c);
        }
    }

    @Override
    public String toString()
    {
        return builder.toString();
    }

}
