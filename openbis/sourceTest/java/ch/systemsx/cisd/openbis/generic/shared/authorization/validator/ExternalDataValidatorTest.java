/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.authorization.validator;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ExternalDataValidatorTest extends AuthorizationTestCase
{
    private ExternalData createData(SpacePE group)
    {
        ExternalData data = new ExternalData();
        data.setExperiment(ExperimentTranslator.translate(createExperiment(group), "http://someURL"));
        return data;
    }
    
    @Test
    public void testIsValidWithDataInTheRightGroup()
    {
        ExternalDataValidator validator = new ExternalDataValidator();
        PersonPE person = createPersonWithRoleAssignments();
        assertEquals(true, validator.isValid(person, createData(createAnotherGroup())));
    }

    @Test
    public void testIsValidWithDataInTheRightDatabaseInstance()
    {
        ExternalDataValidator validator = new ExternalDataValidator();
        PersonPE person = createPersonWithRoleAssignments();
        assertEquals(true, validator.isValid(person, createData(createGroup())));
    }
    
    @Test
    public void testIsValidWithDataInTheWrongGroup()
    {
        ExternalDataValidator validator = new ExternalDataValidator();
        PersonPE person = createPersonWithRoleAssignments();
        SpacePE group = createGroup("blabla", createAnotherDatabaseInstance());
        assertEquals(false, validator.isValid(person, createData(group)));
    }
    
}
