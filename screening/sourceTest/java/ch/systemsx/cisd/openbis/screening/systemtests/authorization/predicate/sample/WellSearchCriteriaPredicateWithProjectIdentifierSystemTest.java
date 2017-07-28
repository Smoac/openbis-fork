/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.screening.systemtests.authorization.predicate.sample;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialSearchCriteria;
import ch.systemsx.cisd.openbis.screening.systemtests.authorization.predicate.experiment.ExperimentSearchCriteriaPredicateWithProjectIdentifierSystemTest;

/**
 * @author pkupczyk
 */
public class WellSearchCriteriaPredicateWithProjectIdentifierSystemTest extends ExperimentSearchCriteriaPredicateWithProjectIdentifierSystemTest
{

    @Override
    protected void evaluateObjects(IAuthSessionProvider session, List<ExperimentSearchCriteria> objects, Object param)
    {
        WellSearchCriteria criteria =
                new WellSearchCriteria(objects.get(0), MaterialSearchCriteria.createIdCriteria(new TechId()),
                        AnalysisProcedureCriteria.createAllProcedures());
        getBean(SamplePredicateScreeningTestService.class).testWellSearchCriteriaPredicate(session, criteria);
    }

    @Override
    protected void assertWithNullForInstanceUser(PersonPE person, Throwable t, Object param)
    {
        assertException(t, AssertionError.class, null);
    }

    @Override
    protected void assertWithNullForSpaceUser(PersonPE person, Throwable t, Object param)
    {
        assertException(t, AssertionError.class, null);
    }

    @Override
    protected void assertWithNullForProjectUser(PersonPE person, Throwable t, Object param)
    {
        assertException(t, AssertionError.class, null);
    }

}
