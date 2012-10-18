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

package ch.systemsx.cisd.openbis.uitest.type;

import ch.systemsx.cisd.openbis.uitest.dsl.Application;
import ch.systemsx.cisd.openbis.uitest.request.CreateDataSetType;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;

/**
 * @author anttil
 */
@SuppressWarnings("hiding")
public class DataSetTypeBuilder implements Builder<DataSetType>
{

    private String code;

    private String description;

    public DataSetTypeBuilder(UidGenerator uid)
    {
        this.code = uid.uid();
        this.description = "";
    }

    public DataSetTypeBuilder withCode(String code)
    {
        this.code = code;
        return this;
    }

    @Override
    public DataSetType build(Application openbis)
    {
        return openbis.execute(new CreateDataSetType(new DataSetType(code, description)));
    }
}
