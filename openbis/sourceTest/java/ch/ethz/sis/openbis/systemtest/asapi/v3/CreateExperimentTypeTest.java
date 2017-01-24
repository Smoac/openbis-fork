/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * @author pkupczyk
 */
@Test
public class CreateExperimentTypeTest extends CreateEntityTypeTest<ExperimentTypeCreation, ExperimentType>
{

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.EXPERIMENT;
    }

    @Override
    protected ExperimentTypeCreation newTypeCreation()
    {
        return new ExperimentTypeCreation();
    }

    @Override
    protected void fillTypeSpecificFields(ExperimentTypeCreation creation)
    {
        // nothing to do
    }

    @Override
    protected void createTypes(String sessionToken, List<ExperimentTypeCreation> creations)
    {
        v3api.createExperimentTypes(sessionToken, creations);
    }

    @Override
    protected ExperimentType getType(String sessionToken, String code)
    {
        ExperimentTypeSearchCriteria criteria = new ExperimentTypeSearchCriteria();
        criteria.withId().thatEquals(new EntityTypePermId(code));

        ExperimentTypeFetchOptions fo = new ExperimentTypeFetchOptions();
        fo.withPropertyAssignments().withPropertyType();
        fo.withPropertyAssignments().withRegistrator();

        SearchResult<ExperimentType> result = v3api.searchExperimentTypes(sessionToken, criteria, fo);

        if (result.getObjects().size() > 0)
        {
            return result.getObjects().get(0);
        } else
        {
            return null;
        }
    }

    @Override
    protected void assertTypeSpecificFields(ExperimentTypeCreation creation, ExperimentType type)
    {
        // nothing to do
    }

}
