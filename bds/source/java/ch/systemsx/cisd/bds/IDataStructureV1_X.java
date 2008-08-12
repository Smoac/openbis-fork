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

package ch.systemsx.cisd.bds;

import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * An <code>IDataStructure</code> extension for version 1.x.
 * 
 * @author Christian Ribeaud
 */
public interface IDataStructureV1_X extends IDataStructure
{

    /**
     * Return the original data directory.
     */
    public IDirectory getOriginalData();

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
    public IFormattedData getFormattedData();

    /**
     * Adds a format parameter to this data structure.
     */
    public void addFormatParameter(final FormatParameter formatParameter);

    /**
     * Sets the annotations for this data structure.
     */
    public void setAnnotations(final IAnnotations imageAnnotations);

    /**
     * Add a {@link Reference}.
     */
    public void addReference(final Reference reference);

    /**
     * Sets the experiment identifier.
     */
    public void setExperimentIdentifier(final ExperimentIdentifier experimentIdentifier);

    /**
     * Sets the experiment registration timestamp.
     */
    public void setExperimentRegistrationTimestamp(
            final ExperimentRegistrationTimestamp experimentRegistrationTimestamp);

    /**
     * Sets the sample.
     */
    public void setSample(final Sample sample);

}
