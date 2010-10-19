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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.mock.web.MockMultipartFile;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class BatchMaterialRegistrationAndUpdate extends SystemTestCase
{
    private static final String MATERIAL_TYPE = "CONTROL";
    private static final String SESSION_KEY = "session-key";

    @Test
    public void testRegistration()
    {
        logIntoCommonClientService();
        String materialBatchData = "code\tdescription\tsize\nc1\tcompound 1\t42\nc2\tcompound 2\t43";
        
        List<BatchRegistrationResult> result = registerMaterials(materialBatchData);
        
        assertEquals("2 material(s) found and registered.", result.get(0).getMessage());
        assertEquals(1, result.size());
        
        assertProperties("[DESCRIPTION: compound 1, SIZE: 42]", "C1");
        assertProperties("[DESCRIPTION: compound 2, SIZE: 43]", "C2");
    }
    
    @Test
    public void testUpdate()
    {
        logIntoCommonClientService();
        if (getMaterialOrNull("C1") == null)
        {
            String materialBatchData =
                "code\tdescription\tsize\n" + "c1\tcompound 1\t42\n" + "c2\tcompound 2\t43";
            registerMaterials(materialBatchData);
        }

        List<BatchRegistrationResult> result =
                updateMaterials("code\tdescription\n" + "c1\tnew description\n" + "c2\t--DELETE--",
                        false);
        
        assertEquals("2 material(s) updated.", result.get(0).getMessage());
        assertEquals(1, result.size());
        
        assertProperties("[DESCRIPTION: new description, SIZE: 42]", "C1");
        assertProperties("[SIZE: 43]", "C2");
    }
    
    private void assertProperties(String expectedProperties, String materialCode)
    {
        assertEquals(expectedProperties, getMaterialOrNull(materialCode).getProperties().toString());
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
                    return p1.getPropertyType().getCode().compareTo(p2.getPropertyType().getCode());
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
        uploadFile(materialBatchData);
        MaterialType materialType = new MaterialType();
        materialType.setCode(MATERIAL_TYPE);
        return genericClientService.registerMaterials(materialType, SESSION_KEY);
    }
    
    private List<BatchRegistrationResult> updateMaterials(String materialBatchData, boolean ignoreUnregistered)
    {
        uploadFile(materialBatchData);
        MaterialType materialType = new MaterialType();
        materialType.setCode(MATERIAL_TYPE);
        return genericClientService.updateMaterials(materialType, SESSION_KEY, ignoreUnregistered);
    }

    private void uploadFile(String fileContent)
    {
        UploadedFilesBean bean = new UploadedFilesBean();
        bean.addMultipartFile(new MockMultipartFile("my-file", fileContent.getBytes()));
        HttpSession session = request.getSession();
        session.setAttribute(SESSION_KEY, bean);
    }
}
