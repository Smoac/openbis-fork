/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.List;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesHolderResolver;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;

/**
 * Fast bd operations on material table.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { IMaterialListingQuery.class })
public class MaterialLister implements IMaterialLister
{
    private final IEntityPropertiesEnricher propertiesEnricher;

    public static IMaterialLister create(IDAOFactory daoFactory, String baseIndexURL)
    {
        MaterialListerDAO dao = MaterialListerDAO.create(daoFactory);
        SecondaryEntityDAO referencedEntityDAO = SecondaryEntityDAO.create(daoFactory);

        return create(dao, referencedEntityDAO, baseIndexURL);
    }

    static IMaterialLister create(MaterialListerDAO dao, SecondaryEntityDAO referencedEntityDAO,
            String baseIndexURL)
    {
        IMaterialListingQuery query = dao.getQuery();
        EntityPropertiesEnricher propertiesEnricher =
                new EntityPropertiesEnricher(query, dao.getPropertySetQuery());
        return new MaterialLister(propertiesEnricher);
    }

    // For unit tests
    MaterialLister(IEntityPropertiesEnricher propertiesEnricher)
    {
        this.propertiesEnricher = propertiesEnricher;
    }

    private void enrichWithProperties(final Long2ObjectMap<Material> resultMap)
    {
        propertiesEnricher.enrich(resultMap.keySet(), new IEntityPropertiesHolderResolver()
            {
                public Material get(long id)
                {
                    return resultMap.get(id);
                }
            });
    }

    private static Long2ObjectMap<Material> asMap(Iterable<Material> materials)
    {
        Long2ObjectMap<Material> map = new Long2ObjectOpenHashMap<Material>();
        for (Material material : materials)
        {
            map.put(material.getId(), material);
        }
        return map;
    }

    public void enrichWithProperties(List<Material> materials)
    {
        setEmptyProperties(materials);
        enrichWithProperties(asMap(materials));
    }

    private void setEmptyProperties(List<Material> materials)
    {
        for (Material material : materials)
        {
            material.setProperties(new ArrayList<IEntityProperty>());
        }
    }
}
