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

package ch.systemsx.cisd.openbis.uitest.rmi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;
import ch.systemsx.cisd.openbis.uitest.dsl.Executor;
import ch.systemsx.cisd.openbis.uitest.request.ListDataSetsOfSample;
import ch.systemsx.cisd.openbis.uitest.rmi.eager.DataSetRmi;
import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.Sample;

/**
 * @author anttil
 */
public class ListDataSetsOfSampleRmi extends Executor<ListDataSetsOfSample, List<DataSet>>
{

    @Override
    public List<DataSet> run(ListDataSetsOfSample request)
    {
        Sample s = request.getSample();

        SampleInitializer init = new SampleInitializer();
        init.setId(1L);
        init.setPermId("aef");
        init.setCode(s.getCode());
        init.setSpaceCode(s.getSpace().getCode());
        init.setSampleTypeCode(s.getType().getCode());
        init.setIdentifier(Identifiers.get(s).toString());
        init.setSampleTypeId(1L);
        init.setRegistrationDetails(new EntityRegistrationDetails(
                new EntityRegistrationDetailsInitializer()));

        ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample sample =
                new ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample(init);

        List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet> dataSets =
                generalInformationService
                        .listDataSets(
                                session,
                                Arrays.asList(new ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample[]
                                    { sample }));

        List<DataSet> result = new ArrayList<DataSet>();
        for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet dataSet : dataSets)
        {
            result.add(new DataSetRmi(dataSet, session, commonServer));
        }
        return result;
    }
}
