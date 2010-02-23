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

package ch.systemsx.cisd.bds.v1_0;

import java.util.Set;

import ch.systemsx.cisd.bds.DataSet;
import ch.systemsx.cisd.bds.ExperimentIdentifier;
import ch.systemsx.cisd.bds.ExperimentRegistrationTimestamp;
import ch.systemsx.cisd.bds.ExperimentRegistrator;
import ch.systemsx.cisd.bds.Format;
import ch.systemsx.cisd.bds.IAnnotations;
import ch.systemsx.cisd.bds.IDataStructure;
import ch.systemsx.cisd.bds.IFormattedData;
import ch.systemsx.cisd.bds.Reference;
import ch.systemsx.cisd.bds.Sample;
import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * An <code>IDataStructure</code> extension for <i>v1.0</i>.
 * 
 * @author Christian Ribeaud
 */
public interface IDataStructureV1_0 extends IDataStructure
{

    /**
     * Return the original data directory.
     */
    public IDirectory getOriginalData();

    /**
     * Returns the directory containing the standardized data.
     */
    public IDirectory getStandardData();

    /**
     * Returns the <code>DataSet</code> of this data structure.
     */
    public DataSet getDataSet();

    /**
     * Sets the <code>DataSet</code> for this data structure.
     */
    public void setDataSet(final DataSet dataSet);

    /**
     * Returns the experiment registrator.
     * 
     * @throws DataStructureException if the processing type hasn't be loaded nor hasn't be set by
     *             {@link #setExperimentRegistrator(ExperimentRegistrator)}.
     */
    public ExperimentRegistrator getExperimentRegistrator();

    /**
     * Sets the experiment registrator.
     */
    public void setExperimentRegistrator(final ExperimentRegistrator experimentRegistrator);

    /**
     * Sets the format for this data structure.
     */
    public void setFormat(final Format format);

    /**
     * Returns the {@link IFormattedData} implementation.
     */
    public IFormattedData getFormattedData() throws DataStructureException;

    /**
     * Sets the annotations for this data structure.
     */
    public void setAnnotations(final IAnnotations imageAnnotations);

    /**
     * Add a {@link Reference}.
     */
    public void addReference(final Reference reference);

    /**
     * Returns the standard-original mapping.
     */
    public Set<Reference> getStandardOriginalMapping();

    /**
     * Sets the experiment identifier. Overwrites an already set or loaded value.
     */
    public void setExperimentIdentifier(final ExperimentIdentifier experimentIdentifier);

    /**
     * Returns the experiment identifier.
     * 
     * @throws DataStructureException if the experiment identifier hasn't be loaded nor hasn't be
     *             set by {@link #setExperimentIdentifier(ExperimentIdentifier)}.
     */
    public ExperimentIdentifier getExperimentIdentifier();

    /**
     * Returns the date of registration of the experiment.
     * 
     * @throws DataStructureException if the processing type hasn't be loaded nor hasn't be set by
     *             {@link #setExperimentRegistrationTimestamp(ExperimentRegistrationTimestamp)}.
     */
    public ExperimentRegistrationTimestamp getExperimentRegistratorTimestamp();

    /**
     * Sets the experiment registration timestamp.
     */
    public void setExperimentRegistrationTimestamp(
            final ExperimentRegistrationTimestamp experimentRegistrationTimestamp);

    /**
     * Sets the measurement entity. Overwrites an already set or loaded value.
     */
    public void setSample(final Sample sample);

    /**
     * Returns the sample.
     * 
     * @throws DataStructureException if the sample hasn't be loaded nor hasn't be set by
     *             {@link #setSample(Sample)}.
     */
    public Sample getSample();
}
