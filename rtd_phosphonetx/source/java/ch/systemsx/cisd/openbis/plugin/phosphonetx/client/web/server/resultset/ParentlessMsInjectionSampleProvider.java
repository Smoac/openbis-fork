/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ParentlessMsInjectionSampleGridColumnIDs.IDENTIFIER;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ParentlessMsInjectionSampleGridColumnIDs.REGISTRATION_DATE;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractCommonTableModelProvider;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.CommonConstants;

/**
 * Provider of MS_INJECTION samples registered for the user.
 * 
 * @author Franz-Josef Elmer
 */
public class ParentlessMsInjectionSampleProvider extends AbstractCommonTableModelProvider<Sample>
{
    public ParentlessMsInjectionSampleProvider(ICommonServer commonServer, String sessionToken)
    {
        super(commonServer, sessionToken);
    }

    @Override
    protected TypedTableModel<Sample> createTableModel(int maxSize)
    {
        DetailedSearchCriteria criteria = new DetailedSearchCriteria();
        criteria.setConnection(SearchCriteriaConnection.MATCH_ALL);
        DetailedSearchCriterion typeCriterion = new DetailedSearchCriterion();
        typeCriterion.setField(DetailedSearchField
                .createAttributeField(SampleAttributeSearchFieldKind.SAMPLE_TYPE));
        typeCriterion.setValue(CommonConstants.MS_INJECTION_SAMPLE_TYPE_CODE);
        DetailedSearchCriterion spaceCriterion = new DetailedSearchCriterion();
        spaceCriterion.setField(DetailedSearchField
                .createAttributeField(SampleAttributeSearchFieldKind.SPACE));
        spaceCriterion.setValue(CommonConstants.MS_DATA_SPACE);
        DetailedSearchCriterion registratorCriterion = new DetailedSearchCriterion();
        String userName = commonServer.tryGetSession(sessionToken).getUserName();
        registratorCriterion.setField(DetailedSearchField.createRegistratorField());
        registratorCriterion.setValue(userName);
        criteria.setCriteria(Arrays.asList(typeCriterion, spaceCriterion, registratorCriterion));
        List<Sample> samples =
                commonServer.searchForSamples(sessionToken, criteria,
                        Collections.<DetailedSearchSubCriteria> emptyList());
        TypedTableModelBuilder<Sample> builder = new TypedTableModelBuilder<Sample>();
        builder.addColumn(IDENTIFIER).withDefaultWidth(300);
        builder.addColumn(REGISTRATION_DATE).withDefaultWidth(300);
        for (Sample sample : samples)
        {
            if (sample.getGeneratedFrom() == null)
            {
                builder.addRow(sample);
                builder.column(IDENTIFIER).addString(sample.getIdentifier());
                builder.column(REGISTRATION_DATE).addDate(sample.getRegistrationDate());
            }
        }
        return builder.getModel();
    }

}
