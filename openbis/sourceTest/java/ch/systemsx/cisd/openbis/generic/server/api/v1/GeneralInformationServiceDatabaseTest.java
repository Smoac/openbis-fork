/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOptions;

/**
 * @author pkupczyk
 */
public class GeneralInformationServiceDatabaseTest extends AbstractDAOTest
{
    @Resource(name = ResourceNames.GENERAL_INFORMATION_SERVICE_SERVER)
    IGeneralInformationService service;

    private String sessionToken;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        sessionToken = service.tryToAuthenticateForAllServices("test", "password");
    }

    @Test
    public void testGetDataSetMetaDataWithBasicFetchOptions()
    {
        List<String> codes = new ArrayList<String>();
        codes.add("20081105092159188-3");
        codes.add("20081105092259000-19");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        List<DataSet> result = service.getDataSetMetaData(sessionToken, codes, fetchOptions);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getFetchOptions().isSetOf(DataSetFetchOption.BASIC));
        assertTrue(result.get(1).getFetchOptions().isSetOf(DataSetFetchOption.BASIC));
    }

    @Test
    public void testGetDataSetMetaDataWithAllFetchOptions()
    {
        List<String> codes = new ArrayList<String>();
        codes.add("20081105092159188-3");
        codes.add("20081105092259000-19");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions(DataSetFetchOption.values());

        List<DataSet> result = service.getDataSetMetaData(sessionToken, codes, fetchOptions);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getFetchOptions().isSetOf(DataSetFetchOption.values()));
        assertTrue(result.get(1).getFetchOptions().isSetOf(DataSetFetchOption.values()));
    }

}
