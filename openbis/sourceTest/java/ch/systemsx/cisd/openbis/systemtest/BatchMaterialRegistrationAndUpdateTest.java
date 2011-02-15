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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedIdHolderCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class BatchMaterialRegistrationAndUpdateTest extends SystemTestCase
{
    private static final String MATERIAL_TYPE = "CONTROL";

    private static final Set<String> CODES = new HashSet<String>(Arrays.asList("C1", "C2"));

    @AfterMethod
    public void tearDown()
    {
        commonClientService.unassignPropertyType(EntityKind.MATERIAL, "COMMENT", MATERIAL_TYPE);
    }

    @Test
    public void testBatchRegistrationWithManagedProperty()
    {
        logIntoCommonClientService().getSessionID();
        deleteTestMaterials();
        Script script = new Script();
        script.setScriptType(ScriptType.MANAGED_PROPERTY);
        script.setName("batch script");
        script.setScript("def batchColumnNames():\n  return ['A', 'B']\n"
                + "def updateFromBatchInput(bindings):\n"
                + "  property.setValue(bindings.get('A') + ' & ' + bindings.get('B'))\n"
                + "def configureUI():\n  None");
        commonClientService.registerScript(script);
        NewETPTAssignment assignment = new NewETPTAssignment();
        assignment.setEntityKind(EntityKind.MATERIAL);
        assignment.setEntityTypeCode(MATERIAL_TYPE);
        assignment.setPropertyTypeCode("COMMENT");
        assignment.setManaged(true);
        assignment.setScriptName("batch script");
        assignment.setOrdinal(0L);
        commonClientService.assignPropertyType(assignment);
        String materialBatchData =
                "code\tdescription\tsize\tcomment:a\tcomment:b\n" + "c1\tcompound 1\t42\tx\ty\n"
                        + "c2\tcompound 2\t43\ta\tb";

        List<BatchRegistrationResult> result = registerMaterials(materialBatchData);

        assertEquals("2 material(s) found and registered.", result.get(0).getMessage());
        assertEquals(1, result.size());

        assertProperties("[COMMENT: x & y, DESCRIPTION: compound 1, SIZE: 42]", "C1");
        assertProperties("[COMMENT: a & b, DESCRIPTION: compound 2, SIZE: 43]", "C2");
    }

    @Test(groups = "broken")
    public void testUpdate()
    {
        logIntoCommonClientService();
        deleteTestMaterials();
        String materialBatchData =
                "code\tdescription\tsize\n" + "c1\tcompound 1\t42\n" + "c2\tcompound 2\t43";
        registerMaterials(materialBatchData);

        List<BatchRegistrationResult> result =
                updateMaterials("code\tdescription\n" + "c1\tnew description\n" + "c2\t--DELETE--",
                        false);

        assertEquals("2 material(s) updated.", result.get(0).getMessage());
        assertEquals(1, result.size());

        assertProperties("[DESCRIPTION: new description, SIZE: 42]", "C1");
        assertProperties("[SIZE: 43]", "C2");
    }

    @Test
    public void testUpdateIgnoreUnregistered()
    {
        logIntoCommonClientService();
        deleteTestMaterials();
        String materialBatchData =
                "code\tdescription\tsize\n" + "c1\tcompound 1\t42\n" + "c2\tcompound 2\t43";
        registerMaterials(materialBatchData);

        List<BatchRegistrationResult> result =
                updateMaterials("code\tdescription\tsize\n" + "c1\tcompound one\t\n"
                        + "c2\tcompound two\t4711\n" + "c3\t3\t\n", true);

        assertEquals("2 material(s) updated, 1 ignored.", result.get(0).getMessage());
        assertEquals(1, result.size());

        assertProperties("[DESCRIPTION: compound one, SIZE: 42]", "C1");
        assertProperties("[DESCRIPTION: compound two, SIZE: 4711]", "C2");
    }

    private void deleteTestMaterials()
    {
        MaterialType materialType = getMaterialType(MATERIAL_TYPE);
        ResultSet<Material> materials =
                commonClientService.listMaterials(ListMaterialDisplayCriteria
                        .createForMaterialType(materialType));
        GridRowModels<Material> list = materials.getList();
        List<Material> materialsToBeDeleted = new ArrayList<Material>();
        for (GridRowModel<Material> gridRowModel : list)
        {
            if (CODES.contains(gridRowModel.getOriginalObject().getCode()))
            {
                materialsToBeDeleted.add(gridRowModel.getOriginalObject());
            }
        }
        commonClientService.deleteMaterials(DisplayedOrSelectedIdHolderCriteria
                .<Material> createSelectedItems(materialsToBeDeleted), "?");
    }

    private MaterialType getMaterialType(String code)
    {
        List<MaterialType> materialTypes = commonClientService.listMaterialTypes();
        for (MaterialType materialType : materialTypes)
        {
            if (materialType.getCode().equalsIgnoreCase(code))
            {
                return materialType;
            }
        }
        throw new IllegalArgumentException("Unknown material type: " + code);
    }

    private void assertProperties(String expectedProperties, String materialCode)
    {
        assertEquals(expectedProperties, StringEscapeUtils.unescapeHtml(getMaterialOrNull(
                materialCode).getProperties().toString()));
    }

    private Material getMaterialOrNull(String code)
    {
        try
        {
            IEntityInformationHolderWithPermId m =
                    commonClientService.getMaterialInformationHolder(new MaterialIdentifier(code,
                            MATERIAL_TYPE));
            Material materialInfo = genericClientService.getMaterialInfo(new TechId(m.getId()));
            assertEquals(code, materialInfo.getCode());
            Collections.sort(materialInfo.getProperties(), new Comparator<IEntityProperty>()
                {
                    public int compare(IEntityProperty p1, IEntityProperty p2)
                    {
                        return p1.getPropertyType().getCode()
                                .compareTo(p2.getPropertyType().getCode());
                    }
                });
            return materialInfo;
        } catch (UserFailureException ex)
        {
            return null;
        }
    }

    private List<BatchRegistrationResult> registerMaterials(String materialBatchData)
    {
        uploadFile("my-file", materialBatchData);
        MaterialType materialType = new MaterialType();
        materialType.setCode(MATERIAL_TYPE);
        return genericClientService.registerMaterials(materialType, SESSION_KEY);
    }

    private List<BatchRegistrationResult> updateMaterials(String materialBatchData,
            boolean ignoreUnregistered)
    {
        uploadFile("my-file", materialBatchData);
        MaterialType materialType = new MaterialType();
        materialType.setCode(MATERIAL_TYPE);
        return genericClientService.updateMaterials(materialType, SESSION_KEY, ignoreUnregistered);
    }
}
