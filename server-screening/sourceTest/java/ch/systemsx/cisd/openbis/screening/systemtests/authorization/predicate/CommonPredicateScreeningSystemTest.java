/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.screening.systemtests.authorization.predicate;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTest;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.screening.systemtests.authorization.CommonAuthorizationScreeningSystemTest;

/**
 * @author pkupczyk
 */
public abstract class CommonPredicateScreeningSystemTest<O> extends CommonPredicateSystemTest<O>
{

    @Override
    protected void setUpDatabaseProperties()
    {
        TestInitializer.setForceCreateWithInitialData(true);
        new CommonAuthorizationScreeningSystemTest().setUpDatabaseProperties();
    }

    @Override
    protected String getApplicationContextLocation()
    {
        return new CommonAuthorizationScreeningSystemTest().getApplicationContextLocation();
    }

}
