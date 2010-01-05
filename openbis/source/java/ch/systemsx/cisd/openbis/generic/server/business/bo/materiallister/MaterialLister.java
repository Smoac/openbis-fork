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

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * Fast DB operations on material table.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
@Friend(toClasses =
    { IMaterialListingQuery.class })
public class MaterialLister implements IMaterialLister
{

    //
    // Input
    //

    private final long databaseInstanceId;

    private final DatabaseInstance databaseInstance;

    //
    // Working interfaces
    //

    private final IMaterialListingQuery query;

    private final IEntityPropertiesEnricher propertiesEnricher;

    private SecondaryEntityDAO referencedEntityDAO;

    //
    // Working data structures
    //

    private final Long2ObjectMap<Person> persons = new Long2ObjectOpenHashMap<Person>();

    //

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
        return new MaterialLister(dao.getDatabaseInstanceId(), dao.getDatabaseInstance(), query,
                propertiesEnricher, referencedEntityDAO);
    }

    // For unit tests
    MaterialLister(final long databaseInstanceId, DatabaseInstance databaseInstance,
            final IMaterialListingQuery query, IEntityPropertiesEnricher propertiesEnricher,
            SecondaryEntityDAO referencedEntityDAO)
    {
        assert query != null;

        this.databaseInstanceId = databaseInstanceId;
        this.databaseInstance = databaseInstance;
        this.query = query;
        this.propertiesEnricher = propertiesEnricher;
        this.referencedEntityDAO = referencedEntityDAO;
    }

    //
    // Listing
    //

    public List<Material> list(MaterialType materialType)
    {
        return enrichMaterials(query.getMaterialsForMaterialType(databaseInstanceId, materialType
                .getId()), materialType);
    }

    //
    // Enriching
    //

    private List<Material> enrichMaterials(Iterable<MaterialRecord> materials,
            MaterialType materialType)
    {
        List<MaterialRecord> materialRecords = asList(materials);
        final Long2ObjectMap<Material> materialMap = createMaterials(materialRecords, materialType);
        enrichWithProperties(materialMap);
        return asList(materialMap);
    }

    private Long2ObjectMap<Material> createMaterials(Iterable<MaterialRecord> records,
            MaterialType materialType)
    {
        Long2ObjectMap<Material> materials = new Long2ObjectOpenHashMap<Material>();
        for (MaterialRecord record : records)
        {
            materials.put(record.id, createMaterial(record, materialType));
        }
        return materials;
    }

    private Material createMaterial(MaterialRecord record, MaterialType materialType)
    {
        Material material = new Material();
        material.setId(record.id);
        material.setCode(escapeHtml(record.code));

        assert record.maty_id == materialType.getId();
        material.setMaterialType(materialType);
        assert record.dbin_id == databaseInstanceId;
        material.setDatabaseInstance(databaseInstance);

        material.setRegistrator(getOrCreateRegistrator(record));
        material.setRegistrationDate(record.registration_timestamp);
        material.setProperties(new ArrayList<IEntityProperty>());

        return material;
    }

    private Person getOrCreateRegistrator(MaterialRecord row)
    {
        return getOrCreateRegistrator(row.pers_id_registerer);
    }

    private Person getOrCreateRegistrator(long personId)
    {
        Person registrator = persons.get(personId);
        if (registrator == null)
        {
            registrator = referencedEntityDAO.getPerson(personId);
            persons.put(personId, registrator);
        }
        return registrator;
    }

    private static <T> List<T> asList(Iterable<T> items)
    {
        List<T> result = new ArrayList<T>();
        for (T item : items)
        {
            result.add(item);
        }
        return result;
    }

    private static <T> List<T> asList(Long2ObjectMap<T> items)
    {
        List<T> result = new ArrayList<T>();
        org.apache.commons.collections.CollectionUtils.addAll(result, items.values().iterator());
        return result;
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
