/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Criteria for listing <i>samples</i> and displaying them in the grid.
 * 
 * @author Tomasz Pylak
 */
public class ListSampleDisplayCriteria extends DefaultResultSetConfig<String, Sample> implements
        IsSerializable
{
    public static ListSampleDisplayCriteria createForContainer(final TechId containerSampleId,
            String baseIndexUrl)
    {
        return new ListSampleDisplayCriteria(ListSampleCriteria.createForContainer(
                containerSampleId, baseIndexUrl));
    }

    public static ListSampleDisplayCriteria createForExperiment(final TechId experimentId,
            String baseIndexUrl)
    {
        return new ListSampleDisplayCriteria(ListSampleCriteria.createForExperiment(experimentId,
                baseIndexUrl));
    }

    private ListSampleCriteria criteria;

    public ListSampleDisplayCriteria(ListSampleCriteria criteria)
    {
        this.criteria = criteria;
    }

    public ListSampleCriteria getCriteria()
    {
        return criteria;
    }

    // GWT only
    @SuppressWarnings("unused")
    private ListSampleDisplayCriteria()
    {
    }

    // GWT only
    @SuppressWarnings("unused")
    private void setCriteria(ListSampleCriteria criteria)
    {
        this.criteria = criteria;
    }

    public SampleType getSampleType()
    {
        return criteria.getSampleType();
    }

}
