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
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.lemnik.eodsql.DataIterator;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesHolderResolver;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.AbstractLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.SecondaryEntityDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;

/**
 * Fast DB operations on material table.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
@Friend(toClasses =
    { MaterialRecord.class, IMaterialListingQuery.class })
public class MaterialLister extends AbstractLister implements IMaterialLister
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
        super(referencedEntityDAO);
        assert query != null;

        this.databaseInstanceId = databaseInstanceId;
        this.databaseInstance = databaseInstance;
        this.query = query;
        this.propertiesEnricher = propertiesEnricher;
    }

    //
    // Listing
    //

    public List<Material> list(ListMaterialCriteria criteria, boolean withProperties)
    {
        MaterialType materialTypeOrNull = criteria.tryGetMaterialType();
        Collection<Long> materialIdsOrNull = criteria.tryGetMaterialIds();
        DataIterator<MaterialRecord> materials =
                materialIdsOrNull != null ? getIteratorByIds(materialIdsOrNull)
                        : getIteratorByType(materialTypeOrNull);
        return convertAndEnrich(materials, materialTypeOrNull, withProperties);
    }

    private List<Material> convertAndEnrich(DataIterator<MaterialRecord> materials,
            MaterialType materialTypeOrNull, boolean withProperties)
    {
        final Long2ObjectMap<Material> materialMap = asMaterials(materials, materialTypeOrNull);
        if (withProperties)
        {
            enrichWithProperties(materialMap);
        }
        return asList(materialMap);
    }

    private DataIterator<MaterialRecord> getIteratorByType(MaterialType materialType)
    {
        assert materialType != null;
        return query.getMaterialsForMaterialType(databaseInstanceId, materialType.getId());
    }

    private DataIterator<MaterialRecord> getIteratorByIds(Collection<Long> materialIds)
    {
        return query.getMaterialsForMaterialTypeWithIds(databaseInstanceId, new LongOpenHashSet(
                materialIds));
    }

    //
    // Enriching
    //

    private Long2ObjectMap<Material> asMaterials(Iterable<MaterialRecord> materials,
            MaterialType materialTypeOrNull)
    {
        List<MaterialRecord> materialRecords = asList(materials);
        final Long2ObjectMap<Material> materialMap =
                createMaterials(materialRecords, materialTypeOrNull);
        return materialMap;
    }

    private Long2ObjectMap<Material> createMaterials(Iterable<MaterialRecord> records,
            MaterialType materialTypeOrNull)
    {
        Long2ObjectMap<Material> materials = new Long2ObjectOpenHashMap<Material>();
        Long2ObjectMap<MaterialType> materialTypesOrNull = null;
        if (materialTypeOrNull == null)
        {
            materialTypesOrNull = getMaterialTypes();
        }
        for (MaterialRecord record : records)
        {
            materials.put(record.id,
                    createMaterial(record, materialTypeOrNull, materialTypesOrNull));
        }
        return materials;
    }

    private Material createMaterial(MaterialRecord record, MaterialType materialTypeOrNull,
            Long2ObjectMap<MaterialType> materialTypesOrNull)
    {
        Material material = new Material();
        material.setId(record.id);
        material.setCode(record.code);

        MaterialType materialType =
                materialTypeOrNull != null ? materialTypeOrNull : materialTypesOrNull
                        .get(record.maty_id);
        material.setMaterialType(materialType);
        assert record.dbin_id == databaseInstanceId;
        material.setDatabaseInstance(databaseInstance);

        material.setRegistrator(getOrCreateRegistrator(record.pers_id_registerer));
        material.setRegistrationDate(record.registration_timestamp);
        material.setModificationDate(record.modification_timestamp);

        material.setProperties(new ArrayList<IEntityProperty>());

        return material;
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
        result.addAll(items.values());
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
        Long2ObjectMap<Material> materialMap = asMap(materials);
        enrichWithProperties(materialMap);
        enrichRepeatedMaterialsWithProperties(materials, materialMap);
    }

    private void enrichRepeatedMaterialsWithProperties(List<Material> allMaterials,
            Long2ObjectMap<Material> enrichedMaterials)
    {
        for (Material material : allMaterials)
        {
            if (material.getProperties().isEmpty())
            {
                Material enrichedMaterial = enrichedMaterials.get(material.getId());
                material.setProperties(new ArrayList<IEntityProperty>(enrichedMaterial
                        .getProperties()));
            }
        }
    }

    private void setEmptyProperties(List<Material> materials)
    {
        for (Material material : materials)
        {
            material.setProperties(new ArrayList<IEntityProperty>());
        }
    }

    private Long2ObjectMap<MaterialType> getMaterialTypes()
    {
        final CodeRecord[] typeCodes = query.getMaterialTypes();
        final Long2ObjectOpenHashMap<MaterialType> materialTypeMap =
                new Long2ObjectOpenHashMap<MaterialType>(typeCodes.length);
        for (CodeRecord t : typeCodes)
        {
            final MaterialType type = new MaterialType();
            type.setCode(t.code);
            materialTypeMap.put(t.id, type);
        }
        materialTypeMap.trim();
        return materialTypeMap;
    }

}
