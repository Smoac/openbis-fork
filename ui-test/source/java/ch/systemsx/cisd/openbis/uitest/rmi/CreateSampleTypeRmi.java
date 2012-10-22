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

import java.util.ArrayList;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.uitest.dsl.Executor;
import ch.systemsx.cisd.openbis.uitest.request.CreateSampleType;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;

/**
 * @author anttil
 */
public class CreateSampleTypeRmi extends Executor<CreateSampleType, SampleType>
{

    @Override
    public SampleType run(CreateSampleType request)
    {
        SampleType sampleType = request.getType();
        commonServer.registerSampleType(session, convert(sampleType));
        return sampleType;
    }

    private ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType convert(SampleType type)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType result =
                new ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType();

        result.setCode(type.getCode());
        result.setContainerHierarchyDepth(0);

        result.setDescription(type.getDescription());
        result.setGeneratedCodePrefix(type.getGeneratedCodePrefix());
        result.setListable(type.isListable());
        result.setGeneratedFromHierarchyDepth(0);
        result.setSampleTypePropertyTypes(new ArrayList<SampleTypePropertyType>());

        return result;
    }

}
