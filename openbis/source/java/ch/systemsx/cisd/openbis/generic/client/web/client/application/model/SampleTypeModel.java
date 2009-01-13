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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.shared.SampleType;

/**
 * {@link ModelData} for {@link SampleType}.
 * 
 * @author Izabela Adamczyk
 */
public class SampleTypeModel extends BaseModelData
{

    private static final long serialVersionUID = 1L;

    public SampleTypeModel(final SampleType sampleType)
    {
        set(ModelDataPropertyNames.CODE, sampleType.getCode());
        set(ModelDataPropertyNames.OBJECT, sampleType);
    }

    public final static List<SampleTypeModel> convert(final List<SampleType> sampleTypes,
            final boolean onlyListable)
    {
        final List<SampleTypeModel> result = new ArrayList<SampleTypeModel>();
        for (final SampleType sampleType : sampleTypes)
        {
            if (onlyListable && sampleType.isListable() == false)
            {
                continue;
            }
            result.add(new SampleTypeModel(sampleType));
        }
        return result;
    }

}
