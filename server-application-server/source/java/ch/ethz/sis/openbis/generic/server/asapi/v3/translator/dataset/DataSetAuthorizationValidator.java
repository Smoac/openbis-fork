/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.AbstractAuthorizationValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
@Component
public class DataSetAuthorizationValidator extends AbstractAuthorizationValidator implements IDataSetAuthorizationValidator
{

    @Override
    public Set<Long> validate(PersonPE person, Collection<Long> dataSetIds)
    {
        AuthorizationDataProvider provider = new AuthorizationDataProvider(daoFactory);
        Set<DataSetAccessPE> accessDatas =
                provider.getDatasetCollectionAccessDataByTechIds(TechId.createList(new ArrayList<Long>(dataSetIds)), false);
        Set<Long> result = new HashSet<Long>();

        for (DataSetAccessPE accessData : accessDatas)
        {
            if (isValid(person, accessData.getSpaceIdentifier(), accessData.getProjectIdentifier()))
            {
                result.add(accessData.getDataSetId());
            }
        }

        return result;
    }

}
