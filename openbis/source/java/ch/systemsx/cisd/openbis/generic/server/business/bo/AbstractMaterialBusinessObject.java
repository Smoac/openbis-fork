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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Generic operations on materials.
 * 
 * @author Tomasz Pylak
 */
public class AbstractMaterialBusinessObject extends AbstractBusinessObject
{
    protected final IEntityPropertiesConverter entityPropertiesConverter;

    protected AbstractMaterialBusinessObject(final IDAOFactory daoFactory, final Session session)
    {
        this(daoFactory, session, new EntityPropertiesConverter(EntityKind.MATERIAL, daoFactory));
    }

    protected AbstractMaterialBusinessObject(final IDAOFactory daoFactory, final Session session,
            final IEntityPropertiesConverter entityPropertiesConverter)
    {
        super(daoFactory, session);
        this.entityPropertiesConverter = entityPropertiesConverter;
    }

    private static final String PROPERTY_TYPES = "materialType.materialTypePropertyTypesInternal";

    protected MaterialPE getMaterialById(final TechId materialId)
    {
        assert materialId != null : "Material technical id unspecified.";
        String[] connections =
            { PROPERTY_TYPES };
        final MaterialPE result = getMaterialDAO().tryGetByTechId(materialId, connections);
        if (result == null)
        {
            throw new UserFailureException(String.format("Material with ID '%s' does not exist.",
                    materialId));
        }
        return result;
    }

    protected Set<String> extractDynamicProperties(final MaterialTypePE type)
    {
        Set<String> dynamicProperties = new HashSet<String>();
        for (MaterialTypePropertyTypePE etpt : type.getMaterialTypePropertyTypes())
        {
            if (etpt.isDynamic())
            {
                dynamicProperties.add(etpt.getPropertyType().getCode());
            }
        }
        return dynamicProperties;
    }

}
