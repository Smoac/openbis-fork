/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * The only productive implementation of {@link IMaterialTable}.
 * 
 * @author Izabela Adamczyk
 */
public final class MaterialTable extends AbstractBusinessObject implements IMaterialTable
{
    private List<MaterialPE> materials;

    public MaterialTable(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    public final void load(final String materialTypeCode)
    {
        checkNotNull(materialTypeCode);
        final EntityTypePE entityType =
                getEntityTypeDAO(EntityKind.MATERIAL).tryToFindEntityTypeByCode(materialTypeCode);
        checkNotNull(materialTypeCode, entityType);
        materials = getMaterialDAO().listMaterials((MaterialTypePE) entityType);
    }

    private void checkNotNull(final String materialTypeCode, final EntityTypePE entityType)
    {
        if (entityType == null)
        {
            throw new UserFailureException("Unknown material type '" + materialTypeCode + "'.");
        }
    }

    private void checkNotNull(final String materialTypeCode)
    {
        if (materialTypeCode == null)
        {
            throw new UserFailureException("Material type not specified.");
        }
    }

    public final void enrichWithProperties()
    {
        if (materials != null)
        {
            for (final MaterialPE material : materials)
            {
                HibernateUtils.initialize(material.getProperties());
            }
        }
    }

    public final List<MaterialPE> getMaterials()
    {
        assert materials != null : "Materials have not been loaded.";
        return materials;
    }
}
