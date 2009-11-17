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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.Collection;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * Extended {@link ListSampleCriteria} with 4th filter option for detailed sample search and 5th
 * option for tracking samples.<br>
 * <br>
 * NOTE: not serializable
 * 
 * @author Piotr Buczek
 */
public final class ListOrSearchSampleCriteria extends ListSampleCriteria
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private ListSampleCriteria listCriteria;

    private TrackingSampleCriteria trackingCriteria;

    private Collection<Long> sampleIds;

    /** Creates criteria that delegates to given {@link ListSampleCriteria}. */
    public ListOrSearchSampleCriteria(ListSampleCriteria listCriteria)
    {
        assert listCriteria != null;
        this.listCriteria = listCriteria;
    }

    /** Creates criteria that delegates to given {@link TrackingSampleCriteria}. */
    public ListOrSearchSampleCriteria(TrackingSampleCriteria trackingCriteria)
    {
        assert trackingCriteria != null;
        this.trackingCriteria = trackingCriteria;
    }

    /** Creates criteria for detailed search of samples with given ids. */
    public ListOrSearchSampleCriteria(final Collection<Long> sampleIds)
    {
        assert sampleIds != null;
        this.sampleIds = sampleIds;
    }

    // search

    public Collection<Long> getSampleIds()
    {
        return sampleIds;
    }

    // delegation to TrackingSampleCriteria

    public String getSampleTypeCode()
    {
        return trackingCriteria == null ? null : trackingCriteria.getSampleTypeCode();
    }

    public int getLastSeenSampleId()
    {
        return trackingCriteria == null ? null : trackingCriteria.getLastSeenSampleId();
    }

    // delegation to ListSampleCriteria

    @Override
    public TechId getContainerSampleId()
    {
        return listCriteria == null ? null : listCriteria.getContainerSampleId();
    }

    @Override
    public TechId getExperimentId()
    {
        return listCriteria == null ? null : listCriteria.getExperimentId();
    }

    @Override
    public String getGroupCode()
    {
        return listCriteria == null ? null : listCriteria.getGroupCode();
    }

    @Override
    public SampleType getSampleType()
    {
        return listCriteria == null ? null : listCriteria.getSampleType();
    }

    @Override
    public boolean isExcludeWithoutExperiment()
    {
        return listCriteria == null ? false : listCriteria.isExcludeWithoutExperiment();
    }

    @Override
    public boolean isIncludeGroup()
    {
        return listCriteria == null ? false : listCriteria.isIncludeGroup();
    }

    @Override
    public boolean isIncludeInstance()
    {
        return listCriteria == null ? false : listCriteria.isIncludeInstance();
    }

}
