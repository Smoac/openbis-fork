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

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

/**
 * {@link ModelData} for {@link Space}.
 * 
 * @author Izabela Adamczyk
 */
public class GroupModel extends SimplifiedBaseModelData
{
    private static final long serialVersionUID = 1L;

    public GroupModel()
    {
    }

    public GroupModel(final Space space)
    {
        set(ModelDataPropertyNames.CODE, space.getCode());
        set(ModelDataPropertyNames.DESCRIPTION, space.getDescription());
        set(ModelDataPropertyNames.REGISTRATOR, space.getRegistrator());
        set(ModelDataPropertyNames.REGISTRATION_DATE, space.getRegistrationDate());
        set(ModelDataPropertyNames.OBJECT, space);
    }

    public final static List<GroupModel> convert(final List<Space> groups)
    {
        final List<GroupModel> result = new ArrayList<GroupModel>();
        for (final Space g : groups)
        {
            result.add(new GroupModel(g));
        }
        return result;
    }

    public final Space getBaseObject()
    {
        return get(ModelDataPropertyNames.OBJECT);
    }
}
