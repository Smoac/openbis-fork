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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.BiologicalSampleGridColumnIDs.IDENTIFIER;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.BiologicalSampleGridColumnIDs.REGISTRATION_DATE;

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractCommonTableModelProvider;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.CommonConstants;

/**
 * Provider of biological samples registered by the user. A biological sample is a sample with sample type code starting with BIO.
 * 
 * @author Franz-Josef Elmer
 */
public class BiologicalSampleProvider extends AbstractCommonTableModelProvider<Sample>
{
    public BiologicalSampleProvider(ICommonServer commonServer, String sessionToken)
    {
        super(commonServer, sessionToken);
    }

    @Override
    protected TypedTableModel<Sample> createTableModel()
    {
        DetailedSearchCriteria criteria = new DetailedSearchCriteria();
        criteria.setConnection(SearchCriteriaConnection.MATCH_ALL);
        DetailedSearchCriterion typeCriterion = new DetailedSearchCriterion();
        typeCriterion.setField(DetailedSearchField
                .createAttributeField(SampleAttributeSearchFieldKind.SAMPLE_TYPE));
        typeCriterion.setValue(CommonConstants.BIOLOGICAL_SAMPLE_PREFIX + "*");
        DetailedSearchCriterion registratorCriterion = new DetailedSearchCriterion();
        String userName = commonServer.tryGetSession(sessionToken).getUserName();
        registratorCriterion.setField(DetailedSearchField.createRegistratorField());
        registratorCriterion.setValue(userName);
        criteria.setCriteria(Arrays.asList(typeCriterion, registratorCriterion));
        List<Sample> samples = commonServer.searchForSamples(sessionToken, criteria);
        TypedTableModelBuilder<Sample> builder = new TypedTableModelBuilder<Sample>();
        builder.addColumn(IDENTIFIER);
        builder.addColumn(REGISTRATION_DATE);
        for (Sample sample : samples)
        {
            builder.addRow(sample);
            builder.column(IDENTIFIER).addString(sample.getIdentifier());
            builder.column(REGISTRATION_DATE).addDate(sample.getRegistrationDate());
            builder.columnGroup("PROPERTY").uneditablePropertyColumns()
                    .addProperties(sample.getProperties());
        }
        return builder.getModel();
    }

}
