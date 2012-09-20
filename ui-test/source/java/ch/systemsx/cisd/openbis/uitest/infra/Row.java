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

package ch.systemsx.cisd.openbis.uitest.infra;

import java.util.Map;

/**
 * @author anttil
 */
public class Row
{

    private Map<String, Cell> row;

    private boolean exists;

    public Row()
    {
        this.exists = false;
    }

    public Row(Map<String, Cell> row)
    {
        this.row = row;
        this.exists = true;
    }

    public boolean exists()
    {
        return exists;
    }

    public Cell get(String columnName)
    {
        return this.row.get(columnName);
    }

}
