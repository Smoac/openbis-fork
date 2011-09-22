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
import static org.testng.AssertJUnit.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
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

    private static class PropertyHistory
    {
        private String propertyTypeCode;

        private String value;

        private Long termID;

        private Long materialID;

        private Date validUntilTimeStamp;

        public Date getValidUntilTimeStamp()
        {
            return validUntilTimeStamp;
        }

        public void setValidUntilTimeStamp(Date validUntilTimeStamp)
        {
            this.validUntilTimeStamp = validUntilTimeStamp;
        }

        public void setPropertyTypeCode(String propertyTypeCode)
        {
            this.propertyTypeCode = propertyTypeCode;
        }

        public void setValue(String value)
        {
            this.value = value;
        }

        public void setTermID(Long termID)
        {
            this.termID = termID;
        }

        public void setMaterialID(Long materialID)
        {
            this.materialID = materialID;
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append(propertyTypeCode).append(":");
            if (value != null)
            {
                builder.append(' ').append(value);
            }
            if (termID != null)
            {
                builder.append(" term:").append(termID);
            }
            if (materialID != null)
            {
                builder.append(" material:").append(materialID);
            }
            return builder.toString();
        }

    }

    private static final class HistoryRowMapper implements ParameterizedRowMapper<PropertyHistory>
    {

        public PropertyHistory mapRow(java.sql.ResultSet rs, int rowNum) throws SQLException
        {
            PropertyHistory propertyHistory = new PropertyHistory();
            propertyHistory.setPropertyTypeCode(rs.getString("code"));
            propertyHistory.setValue(rs.getString("value"));
            long id = rs.getLong("cvte_id");
            if (rs.wasNull() == false)
            {
                propertyHistory.setTermID(id);
            }
            id = rs.getLong("mate_prop_id");
            if (rs.wasNull() == false)
            {
                propertyHistory.setMaterialID(id);
            }
            propertyHistory.setValidUntilTimeStamp(rs.getTimestamp("valid_until_timestamp"));
            return propertyHistory;
        }

    }

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

        List<BatchRegistrationResult> result = registerMaterials(materialBatchData, MATERIAL_TYPE);

        assertEquals("Registration/update of 2 material(s) is complete.", result.get(0)
                .getMessage());
        assertEquals(1, result.size());

        assertProperties("[COMMENT: x & y, DESCRIPTION: compound 1, SIZE: 42]", "C1");
        assertProperties("[COMMENT: a & b, DESCRIPTION: compound 2, SIZE: 43]", "C2");
    }

    @Test
    public void testUpdateOfPropertiesOfVariousTypes()
    {
        logIntoCommonClientService();
        deleteTestMaterials();

        NewETPTAssignment assignment = new NewETPTAssignment();
        assignment.setEntityKind(EntityKind.MATERIAL);
        assignment.setEntityTypeCode(MATERIAL_TYPE);
        assignment.setPropertyTypeCode("GENDER");
        commonClientService.assignPropertyType(assignment);
        assignment.setPropertyTypeCode("BACTERIUM");
        commonClientService.assignPropertyType(assignment);

        String materialBatchData =
                "code\tdescription\tsize\tgender\tbacterium\n"
                        + "c1\tcompound 1\t42\tfemale\tbacterium1\n"
                        + "c2\tcompound 2\t43\tmale\tbacterium-x";
        registerMaterials(materialBatchData, MATERIAL_TYPE);

        List<BatchRegistrationResult> result =
                updateMaterials("code\tdescription\tgender\tbacterium\n"
                        + "c1\tnew description\tmale\tbacterium2\n" + "c2\t\tmale\tbacterium-y",
                        MATERIAL_TYPE, false);

        assertEquals("2 material(s) updated.", result.get(0).getMessage());
        assertEquals(1, result.size());
        assertProperties(
                "[BACTERIUM: BACTERIUM2 (BACTERIUM), DESCRIPTION: new description, GENDER: MALE, SIZE: 42]",
                "C1");
        assertProperties(
                "[BACTERIUM: BACTERIUM-Y (BACTERIUM), DESCRIPTION: compound 2, GENDER: MALE, SIZE: 43]",
                "C2");
        List<PropertyHistory> history = getHistory(getMaterialOrNull("C1").getId());
        assertEquals("[BACTERIUM: material:22, DESCRIPTION: compound 1, GENDER: term:12]",
                history.toString());
        assertCurrentValidUntilTimeStamp(history.get(0));
        assertEquals("[BACTERIUM: material:34]", getHistory(getMaterialOrNull("C2").getId())
                .toString());

        updateMaterials("code\tdescription\tgender\tbacterium\n"
                + "c2\t--DELETE--\tfemale\tbacterium2\n", MATERIAL_TYPE, false);

        assertEquals("[BACTERIUM: material:34, BACTERIUM: material:35, GENDER: term:11]",
                getHistory(getMaterialOrNull("C2").getId()).toString());
        deleteTestMaterials();
    }

    @Test
    public void testUpdateIgnoreUnregistered()
    {
        logIntoCommonClientService();
        deleteTestMaterials();
        String materialBatchData =
                "code\tdescription\tsize\n" + "c1\tcompound 1\t42\n" + "c2\tcompound 2\t43";
        registerMaterials(materialBatchData, MATERIAL_TYPE);

        List<BatchRegistrationResult> result =
                updateMaterials("code\tdescription\tsize\n" + "c1\tcompound one\t\n"
                        + "c2\tcompound two\t4711\n" + "c3\t3\t\n", MATERIAL_TYPE, true);

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
        for (Material deletedMaterial : materialsToBeDeleted)
        {
            assertEquals("Deleted material: " + deletedMaterial, 0,
                    getHistory(deletedMaterial.getId()).size());
        }
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
            Material materialInfo = commonClientService.getMaterialInfo(new TechId(m.getId()));
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

    private List<BatchRegistrationResult> registerMaterials(String materialBatchData,
            String materialTypeCode)
    {
        uploadFile("my-file", materialBatchData);
        MaterialType materialType = new MaterialType();
        materialType.setCode(materialTypeCode);
        return genericClientService.registerMaterials(materialType, false, SESSION_KEY);
    }

    private List<BatchRegistrationResult> updateMaterials(String materialBatchData,
            String materialTypeCode, boolean ignoreUnregistered)
    {
        uploadFile("my-file", materialBatchData);
        MaterialType materialType = new MaterialType();
        materialType.setCode(materialTypeCode);
        return genericClientService.updateMaterials(materialType, SESSION_KEY, ignoreUnregistered);
    }

    private void assertCurrentValidUntilTimeStamp(PropertyHistory historyEntry)
    {
        assertTrue(
                "Current time stamp: " + new Date() + " valid-until timestamp: "
                        + historyEntry.getValidUntilTimeStamp(),
                Math.abs(historyEntry.getValidUntilTimeStamp().getTime()
                        - System.currentTimeMillis()) < 10000);
    }

    private List<PropertyHistory> getHistory(long materialID)
    {
        List<PropertyHistory> list =
                simpleJdbcTemplate
                        .query("select t.code, h.value, h.cvte_id, h.mate_prop_id, valid_until_timestamp"
                                + " from material_properties_history as h "
                                + " join material_type_property_types as etpt on h.mtpt_id = etpt.id"
                                + " join property_types as t on etpt.prty_id = t.id where h.mate_id = ?",
                                new HistoryRowMapper(), materialID);
        Collections.sort(list, new Comparator<PropertyHistory>()
            {
                public int compare(PropertyHistory o1, PropertyHistory o2)
                {
                    return o1.toString().compareTo(o2.toString());
                }
            });
        return list;
    }

}
