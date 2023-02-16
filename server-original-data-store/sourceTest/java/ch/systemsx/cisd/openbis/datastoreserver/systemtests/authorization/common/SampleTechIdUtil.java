/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest.SampleKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class SampleTechIdUtil
{

    public static TechId createNonexistentObject(Object param)
    {
        return new TechId(-1);
    }

    public static TechId createObject(CommonAuthorizationSystemTest test, SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        SamplePE samplePE = test.getSample(spacePE, projectPE, (SampleKind) param);
        return new TechId(samplePE.getId());
    }

    public static void assertWithNonexistentObjectForInstanceUser(PersonPE person, Throwable t, Object param)
    {
        CommonAuthorizationSystemTest.assertNoException(t);
    }

    public static void assertWithNonexistentObjectForProjectUser(PersonPE person, Throwable t, Object param)
    {
        CommonAuthorizationSystemTest.assertNoException(t);
    }

    public static void assertWithNonexistentObjectForSpaceUser(PersonPE person, Throwable t, Object param)
    {
        CommonAuthorizationSystemTest.assertNoException(t);
    }

}
