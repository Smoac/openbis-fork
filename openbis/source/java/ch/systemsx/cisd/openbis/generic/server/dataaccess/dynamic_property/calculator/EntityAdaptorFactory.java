/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * Factory of adaptors implementing {@link IEntityAdaptor}.
 * 
 * @author Piotr Buczek
 */
public class EntityAdaptorFactory
{
    /** Returns an adaptor for specified entity based on its kind. */
    public static IEntityAdaptor create(IEntityInformationWithPropertiesHolder entity,
            IDynamicPropertyEvaluator evaluator)
    {
        switch (entity.getEntityKind())
        {
            case SAMPLE:
                return new SampleAdaptor((SamplePE) entity, evaluator);
            case EXPERIMENT:
                return new ExperimentAdaptor((ExperimentPE) entity, evaluator);
            case DATA_SET:
                return new ExternalDataAdaptor((ExternalDataPE) entity, evaluator);
            case MATERIAL:
                return new MaterialAdaptor((MaterialPE) entity, evaluator);
            default:
                throw new UnsupportedOperationException(""); // can't happen
        }
    }
}
