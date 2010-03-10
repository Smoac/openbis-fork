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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSampleCriteriaDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * A generic sample <i>Business Object</i>.
 * 
 * @author Tomasz Pylak
 */
public interface ISampleTable
{
    /**
     * Use {@link ISampleLister} instead.<br>
     * Lists sample of a particular type.
     * <p>
     * Container and generator fields will be initialized up to the specified nesting level.
     * </p>
     */
    @Deprecated
    void loadSamplesByCriteria(final ListSampleCriteriaDTO criteria);

    /**
     * Lists samples filtered by specified criteria, see {@link ListSamplesByPropertyCriteria} to
     * see the details.
     */
    void loadSamplesByCriteria(final ListSamplesByPropertyCriteria criteria);

    /**
     * Returns the loaded {@link SamplePE}s.
     */
    List<SamplePE> getSamples();

    /**
     * Adds given <var>newSample</var> sample to this table.
     */
    public void add(final NewSample newSample) throws UserFailureException;

    /**
     * Adds given samples to this table.
     */
    public void add(List<NewSample> newSamples) throws UserFailureException;

    /**
     * Writes changed are added data to the Data Access Layers.
     */
    public void save() throws UserFailureException;

}
