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

package ch.systemsx.cisd.openbis.uitest.rmi;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.uitest.request.CreateDataSetType;
import ch.systemsx.cisd.openbis.uitest.request.Executor;
import ch.systemsx.cisd.openbis.uitest.type.DataSetType;

/**
 * @author anttil
 */
public class CreateDataSetTypeRmi extends Executor<CreateDataSetType, DataSetType>
{

    @Override
    public DataSetType run(CreateDataSetType request)
    {
        DataSetType type = request.getType();
        commonServer.registerDataSetType(session, convert(type));
        return type;
    }

    private ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType convert(DataSetType type)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType t =
                new ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType();
        t.setCode(type.getCode());
        t.setDataSetKind(DataSetKind.PHYSICAL);
        return t;
    }

}
