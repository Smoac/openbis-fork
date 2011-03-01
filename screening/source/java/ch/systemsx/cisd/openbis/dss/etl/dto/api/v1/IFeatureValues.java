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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

/**
 * An interface which allows to define values of one feature.
 * 
 * @author Tomasz Pylak
 */
public interface IFeatureValues
{
    /**
     * Relevant only in cases where feature values for different timepoints and/or depth-scans are
     * available. In other cases one does not have to call this method at all.
     * <p>
     * Sets the timepoint and/or the depth-scan values which will be used in all subsequent calls to
     * {@link #addValue} until this method will be called again.
     * </p>
     */
    void setSeries(Double timeOrNull, Double depthOrNull);

    /**
     * @param well code of the well, e.g. A1
     * @param value value of the feature in the specified well
     */
    void addValue(String well, String value);

    /**
     * @param wellRow row coordinate of the well, top-left well has (1,1) coordinates.
     * @param wellColumn column coordinate of the well, top-left well has (1,1) coordinates.
     * @param value value of the feature in the specified well
     */
    void addValue(int wellRow, int wellColumn, String value);
}
