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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author Franz-Josef Elmer
 */
public interface IExternalDataBO extends IEntityBusinessObject
{
    /**
     * Returns the external data item which has been created by
     * {@link #define(ExternalData, SamplePE, SourceType)}.
     */
    public ExternalDataPE getExternalData();

    /**
     * Defines a new external data item. After invocation of this method
     * {@link IExperimentBO#save()} should be invoked to store the new external data item in the
     * Data Access Layer.
     */
    public void define(ExternalData externalData, SamplePE sample, SourceType sourceType);

    /**
     * Changes given data set. Currently allowed changes: properties, sample.
     */
    public void update(TechId datasetId, SampleIdentifier sampleIdentifier,
            List<IEntityProperty> properties, Date version);

    /**
     * Loads the external data item with specified code.
     */
    public void loadByCode(String dataSetCode);

    /**
     * Enrich external data with parents and experiment.
     */
    public void enrichWithParentsAndExperiment();

    /**
     * Enrich external data with children and experiment.
     */
    public void enrichWithChildren();

    /**
     * Enrich external data with properties.
     */
    public void enrichWithProperties();
}
