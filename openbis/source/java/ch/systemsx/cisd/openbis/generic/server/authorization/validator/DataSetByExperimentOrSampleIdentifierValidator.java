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

package ch.systemsx.cisd.openbis.generic.server.authorization.validator;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Validator based on the experiment or sample identifier of a {@link DataSet}.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetByExperimentOrSampleIdentifierValidator extends AbstractValidator<DataSet>
{
    private final ExperimentByIdentiferValidator experimentValidator = new ExperimentByIdentiferValidator();
    private final SampleByIdentiferValidator sampleValidator = new SampleByIdentiferValidator();

    @Override
    public boolean doValidation(PersonPE person, final DataSet dataSet)
    {
        if (StorageConfirmedForAdminValidator.isValid(person, dataSet.isStorageConfirmed()) == false)
        {
            return false;
        }
        if (dataSet.getExperimentIdentifier() != null)
        {
            return experimentValidator.isValid(person, new IIdentifierHolder()
                {
                    @Override
                    public String getIdentifier()
                    {
                        return dataSet.getExperimentIdentifier();
                    }
                });
        }
        return sampleValidator.isValid(person, new IIdentifierHolder()
            {
                @Override
                public String getIdentifier()
                {
                    return dataSet.getSampleIdentifierOrNull();
                }
            });
    }

}
