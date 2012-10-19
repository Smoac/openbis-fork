/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exception.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.batch.IBatchOperation;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IAbstractBussinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.MaterialCodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.MaterialConfigurationProvider;
import ch.systemsx.cisd.openbis.generic.shared.util.ServerUtils;

/**
 * @author Kaloyan Enimanev
 */
public class MaterialHelper
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MaterialHelper.class);

    private final Session session;

    private final IAbstractBussinessObjectFactory businessObjectFactory;

    private final IDAOFactory daoFactory;

    private final IPropertiesBatchManager propertiesBatchManager;

    private final MaterialConfigurationProvider materialConfig;

    public MaterialHelper(Session session, IAbstractBussinessObjectFactory businessObjectFactory,
            IDAOFactory daoFactory, IPropertiesBatchManager propertiesBatchManager)
    {
        this(session, businessObjectFactory, daoFactory, propertiesBatchManager,
                MaterialConfigurationProvider.getInstance());
    }

    MaterialHelper(Session session, IAbstractBussinessObjectFactory businessObjectFactory,
            IDAOFactory daoFactory, IPropertiesBatchManager propertiesBatchManager,
            MaterialConfigurationProvider materialConfig)
    {
        this.session = session;
        this.businessObjectFactory = businessObjectFactory;
        this.daoFactory = daoFactory;
        this.propertiesBatchManager = propertiesBatchManager;
        this.materialConfig = materialConfig;
    }

    public List<Material> registerMaterials(String materialTypeCode,
            final List<NewMaterial> newMaterials)
    {
        assert materialTypeCode != null : "Unspecified material type.";
        assert newMaterials != null : "Unspecified new materials.";

        // Does nothing if material list is empty.
        if (newMaterials.size() == 0)
        {
            return Collections.emptyList();
        }
        ServerUtils.prevalidate(newMaterials, "material");
        final MaterialTypePE materialTypePE = findMaterialType(materialTypeCode);
        final List<MaterialPE> registeredMaterials = new ArrayList<MaterialPE>();
        propertiesBatchManager.manageProperties(materialTypePE, newMaterials,
                session.tryGetPerson());
        IBatchOperation<NewMaterial> strategy = new IBatchOperation<NewMaterial>()
            {

                @Override
                public void execute(List<NewMaterial> entities)
                {
                    final IMaterialTable materialTable =
                            businessObjectFactory.createMaterialTable(session);
                    materialTable.add(entities, materialTypePE);
                    materialTable.save();
                    registeredMaterials.addAll(materialTable.getMaterials());
                }

                @Override
                public List<NewMaterial> getAllEntities()
                {
                    return newMaterials;
                }

                @Override
                public String getEntityName()
                {
                    return "material";
                }

                @Override
                public String getOperationName()
                {
                    return "register";
                }
            };
        BatchOperationExecutor.executeInBatches(strategy);

        return MaterialTranslator.translate(registeredMaterials,
                new HashMap<Long, Set<Metaproject>>());
    }

    public int updateMaterials(String materialTypeCode, final List<NewMaterial> newMaterials,
            final boolean ignoreUnregisteredMaterials) throws UserFailureException
    {
        assert materialTypeCode != null : "Unspecified material type.";
        assert newMaterials != null : "Unspecified new materials.";

        // Does nothing if material list is empty.
        if (newMaterials.size() == 0)
        {
            return 0;
        }

        ServerUtils.prevalidate(newMaterials, "material");

        final AtomicInteger count = new AtomicInteger(0);
        final Map<String/* code */, Material> existingMaterials = listMaterials(materialTypeCode);

        final MaterialTypePE materialTypePE = findMaterialType(materialTypeCode);
        propertiesBatchManager.manageProperties(materialTypePE, newMaterials,
                session.tryGetPerson());
        IBatchOperation<NewMaterial> strategy = new IBatchOperation<NewMaterial>()
            {
                @Override
                public void execute(List<NewMaterial> entities)
                {
                    List<MaterialUpdateDTO> materialUpdates = new ArrayList<MaterialUpdateDTO>();
                    for (NewMaterial material : entities)
                    {
                        final String materialDBCode =
                                MaterialCodeConverter.tryMaterialCodeToDatabase(material.getCode(),
                                        materialConfig);
                        Material existingMaterial = existingMaterials.get(materialDBCode);
                        if (existingMaterial != null)
                        {
                            materialUpdates.add(createMaterialUpdate(existingMaterial, material));
                            count.incrementAndGet();
                        } else if (ignoreUnregisteredMaterials == false)
                        {
                            throw new UserFailureException("Can not update unregistered material '"
                                    + material.getCode()
                                    + "'. Please use checkbox for ignoring unregistered materials.");
                        }
                    }
                    updateMaterials(materialUpdates);
                }

                @Override
                public List<NewMaterial> getAllEntities()
                {
                    return newMaterials;
                }

                @Override
                public String getEntityName()
                {
                    return "material";
                }

                @Override
                public String getOperationName()
                {
                    return "update";
                }
            };
        BatchOperationExecutor.executeInBatches(strategy);
        return count.get();
    }

    public void registerOrUpdateMaterials(String materialTypeCode, List<NewMaterial> materials)
    {
        Map<String/* code */, Material> existingMaterials = listMaterials(materialTypeCode);
        List<NewMaterial> materialsToRegister = new ArrayList<NewMaterial>();
        List<MaterialUpdateDTO> materialUpdates = new ArrayList<MaterialUpdateDTO>();
        for (NewMaterial material : materials)
        {
            String materialDBCode =
                    MaterialCodeConverter.tryMaterialCodeToDatabase(material.getCode(),
                            materialConfig);
            Material existingMaterial = existingMaterials.get(materialDBCode);
            if (existingMaterial != null)
            {
                materialUpdates.add(createMaterialUpdate(existingMaterial, material));
            } else
            {
                materialsToRegister.add(material);
            }
        }
        registerMaterials(materialTypeCode, materialsToRegister);
        updateMaterials(materialUpdates);
        operationLog.info(String.format("Number of newly registered materials: %d, "
                + "number of existing materials which have been updated: %d",
                materialsToRegister.size(), materialUpdates.size()));

    }

    private static MaterialUpdateDTO createMaterialUpdate(Material existingMaterial,
            NewMaterial material)
    {
        return new MaterialUpdateDTO(new TechId(existingMaterial.getId()), Arrays.asList(material
                .getProperties()), existingMaterial.getModificationDate());
    }

    public void updateMaterials(List<MaterialUpdateDTO> updates)
    {
        if (updates.isEmpty())
        {
            return;
        }
        IMaterialTable materialTable = businessObjectFactory.createMaterialTable(session);
        materialTable.update(updates);
        materialTable.save();
    }

    private Map<String/* code */, Material> listMaterials(String materialTypeCode)
    {
        EntityTypePE entityTypePE =
                daoFactory.getEntityTypeDAO(EntityKind.MATERIAL).tryToFindEntityTypeByCode(
                        materialTypeCode);
        if (entityTypePE == null)
        {
            throw new UserFailureException("Material type does not exist: " + materialTypeCode);
        }
        MaterialType materialType = MaterialTypeTranslator.translateSimple(entityTypePE);

        IMaterialLister materialLister = businessObjectFactory.createMaterialLister(session);
        ListMaterialCriteria listByTypeCriteria =
                ListMaterialCriteria.createFromMaterialType(materialType);
        List<Material> materials = materialLister.list(listByTypeCriteria, false);
        return asCodeToMaterialMap(materials);
    }

    private static Map<String, Material> asCodeToMaterialMap(List<Material> materials)
    {
        Map<String, Material> map = new HashMap<String, Material>();
        for (Material material : materials)
        {
            map.put(material.getCode(), material);
        }
        return map;
    }

    private MaterialTypePE findMaterialType(String materialTypeCode)
    {
        final MaterialTypePE materialTypePE =
                (MaterialTypePE) daoFactory.getEntityTypeDAO(EntityKind.MATERIAL)
                        .tryToFindEntityTypeByCode(materialTypeCode);
        if (materialTypePE == null)
        {
            throw UserFailureException.fromTemplate("Material type with code '%s' does not exist.",
                    materialTypeCode);
        }
        return materialTypePE;
    }

}
