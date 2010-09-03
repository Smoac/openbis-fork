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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.Collection;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;

/**
 * Criteria for listing <i>materials</i>.
 * 
 * @author Izabela Adamczyk
 */
public final class ListMaterialCriteria extends DefaultResultSetConfig<String, Material> implements
        IsSerializable
{
    private MaterialType materialType;

    private Collection<Long> materialIdsOrNull;

    // GWT only
    public ListMaterialCriteria()
    {
    }

    public ListMaterialCriteria(MaterialType materialType)
    {
        this(materialType, null);
    }

    public ListMaterialCriteria(MaterialType materialType, Collection<Long> materialIdsOrNull)
    {
        this.materialType = materialType;
        this.materialIdsOrNull = materialIdsOrNull;
    }

    public MaterialType getMaterialType()
    {
        return materialType;
    }

    public Collection<Long> getMaterialIdsOrNull()
    {
        return materialIdsOrNull;
    }

}
