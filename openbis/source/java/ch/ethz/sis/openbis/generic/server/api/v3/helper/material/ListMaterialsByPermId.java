/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.helper.material;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.sis.openbis.generic.server.api.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.MaterialPermId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMaterialDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;

/**
 * @author pkupczyk
 */
public class ListMaterialsByPermId implements IListObjectById<MaterialPermId, MaterialPE>
{

    private IMaterialDAO materialDAO;

    public ListMaterialsByPermId(IMaterialDAO materialDAO)
    {
        this.materialDAO = materialDAO;
    }

    @Override
    public Class<MaterialPermId> getIdClass()
    {
        return MaterialPermId.class;
    }

    @Override
    public MaterialPermId createId(MaterialPE material)
    {
        return new MaterialPermId(material.getCode(), material.getMaterialType().getCode());
    }

    @Override
    public List<MaterialPE> listByIds(List<MaterialPermId> ids)
    {
        System.out.println("list by ids: " + ids);
        Set<MaterialIdentifier> codes = new HashSet<MaterialIdentifier>();
        for (MaterialPermId id : ids)
        {
            codes.add(new MaterialIdentifier(id.getCode(), id.getTypeCode()));
        }
        return materialDAO.listMaterialsByMaterialIdentifier(codes);
    }
}
