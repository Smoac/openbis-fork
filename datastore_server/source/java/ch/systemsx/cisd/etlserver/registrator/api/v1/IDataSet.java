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

package ch.systemsx.cisd.etlserver.registrator.api.v1;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDataSet
{
    /**
     * Get the data set code of the data set
     * 
     * @return The code of this data set.
     */
    public String getDataSetCode();

    /**
     * Get the experiment for this data set. This can only be null at initialization time, and will
     * be non-null for a valid data set.
     * 
     * @return The experiment for this data set (will be non-null for a valid data set)
     */
    IExperimentImmutable getExperiment();

    /**
     * Set the experiment for this data set. The experiment may also be set by setting the sample.
     * 
     * @param experiment The experiment for this data set. Need not actually be immutable, but the
     *            immutable one is the supertype.
     */
    void setExperiment(IExperimentImmutable experiment);

    /**
     * Get the sample for this data set, if there is one.
     * 
     * @return A sample or null.
     */
    ISampleImmutable getSample();

    /**
     * Set the sample for this data set. Will also set the experiment, since the sample must have an
     * experiment.
     * 
     * @param sampleOrNull The sample to use. Need not actually be immutable, but the immutable one
     *            is the supertype.
     */
    void setSample(ISampleImmutable sampleOrNull);

    /**
     * The file format type of the data set. Defaults to the default specified in
     * {@link FileFormatType}.
     * 
     * @return The code of the {@link FileFormatType} for this data set.
     */
    public String getFileFormatType();

    /**
     * Set the file format type.
     * 
     * @param fileFormatTypeCode The code of the desired {@link FileFormatType}.
     */
    public void setFileFormatType(String fileFormatTypeCode);

    /**
     * Return true if the data set is measured data. Defaults to true.
     * 
     * @return True if the data set is measured data, false otherwise.
     */
    public boolean isMeasuredData();

    /**
     * Set whether the data is measured or not.
     */
    public void setMeasuredData(boolean measuredData);

    /**
     * Get the data set type. This is only null during initialization and is non-null for a valid
     * data set.
     */
    public String getDataSetType();

    /**
     * Set the data set type.
     */
    public void setDataSetType(String dataSetTypeCode);

    /**
     * Returns the speed hint. If it hasn't been set by {@link #setSpeedHint(int)} the default value
     * {@link Constants#DEFAULT_SPEED_HINT} will be returned.
     */
    public int getSpeedHint();

    /**
     * Sets the speed hint for the data set. The speed hint is a negative or positive number with an
     * absolute value less than or equal {@link Constants#MAX_SPEED}.
     * <p>
     * A positive value means that the data set should be stored in a storage with speed &gt;=
     * <code>speedHint</code>. A negative value means that the data set should be stored in a
     * storage with speed &lt;= <code>abs(speedHint)</code>. The speed hint might be ignored.
     */
    public void setSpeedHint(int speedHint);

    /**
     * Get the value for a property.
     */
    public String getPropertyValue(String propertyCode);

    /**
     * Set the value for a property.
     */
    public void setPropertyValue(String propertyCode, String propertyValue);

    /** Sets the parents of the dataset. */
    public void setParentDatasets(List<String> parentDatasetCodes);

    /** Gets the parents of the dataset. */
    public List<String> getParentDatasets();

    // Methods relating to container data sets which contain other data sets
    /** Return true if this data set contains other data sets. */
    public boolean isContainerDataSet();

    /**
     * Get the codes for contained data sets. This is empty if {@link #isContainerDataSet()} returns
     * false.
     */
    public List<String> getContainedDataSetCodes();

    /** Set the codes for contained data sets. */
    public void setContainedDataSetCodes(List<String> containedDataSetCodes);
}
