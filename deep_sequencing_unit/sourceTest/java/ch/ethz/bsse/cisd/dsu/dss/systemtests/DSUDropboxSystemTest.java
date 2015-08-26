/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.bsse.cisd.dsu.dss.systemtests;

import org.testng.annotations.BeforeSuite;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.GenericDropboxSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * @author pkupczyk
 */
public abstract class DSUDropboxSystemTest extends GenericDropboxSystemTest
{

    @Override
    @BeforeSuite
    public void beforeSuite() throws Exception
    {
        initializePythonImportPath();
        DSUTestInitializer.init();
        super.beforeSuite();
        initializeMasterData();
    }

    @Override
    protected String getDropboxIncomingDirectoryName()
    {
        return getDropboxName();
    }

    private void initializePythonImportPath()
    {
        System.setProperty("python.path", "sourceTest/core-plugins/illumina-qgf/1/jython-lib");
    }

    private void initializeMasterData()
    {
        String sessionToken = getGeneralInformationService().tryToAuthenticateForAllServices("kohleman", "password");
        ICommonServer commonServer = getBean("common-server");
        PropertyType propertyType = new PropertyType();
        propertyType.setCode("CONTROL_SOFTWARE_VERSION");
        propertyType.setDataType(new DataType(DataTypeCode.VARCHAR));
        propertyType.setLabel("Control Software Version");
        propertyType.setDescription(propertyType.getLabel());
        NewETPTAssignment assignment = new NewETPTAssignment();
        assignment.setPropertyTypeCode(propertyType.getCode());
        assignment.setEntityTypeCode("ILLUMINA_FLOW_CELL");
        assignment.setEntityKind(EntityKind.SAMPLE);
        assignment.setOrdinal(1000L);
        commonServer.registerAndAssignPropertyType(sessionToken, propertyType, assignment);
    }

}
