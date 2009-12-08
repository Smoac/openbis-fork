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

/**
 * A sample to update.
 * 
 * @author Piotr Buczek
 */
public final class UpdatedSample extends NewSample
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

	// TODO 2009-08-12, Piotr Buczek: modify and use in template
    public static final String SAMPLE_UPDATE_TEMPLATE_COMMENT =
            "# The \"container\" and \"parent\" columns are optional, only one should be specified. They should contain a sample identifier, e.g. /GROUP/SAMPLE_1\n"
                    + "# If \"container\" sample is provided, the registered sample will become a \"component\" of it.\n"
                    + "# If \"parent\" sample is provided, the registered sample will become a \"child\" of it.\n"
                    + "# The \"experiment\" column is optional, cannot be specified for shared samples and should contain experiment identifier, e.g. /GROUP/PROJECT/EXP_1\n";

    private SampleBatchUpdateDetails batchUpdateDetails;

    public UpdatedSample(NewSample newSample, SampleBatchUpdateDetails batchUpdateDetails)
    {
        super(newSample.getIdentifier(), newSample.getSampleType(),
                newSample.getParentIdentifier(), newSample.getContainerIdentifier(), newSample
                        .getExperimentIdentifier(), newSample.getProperties(), newSample
                        .getAttachments());
        this.batchUpdateDetails = batchUpdateDetails;
    }

    public SampleBatchUpdateDetails getBatchUpdateDetails()
    {
        return batchUpdateDetails;
    }

    public void setBatchUpdateDetails(SampleBatchUpdateDetails batchUpdateDetails)
    {
        this.batchUpdateDetails = batchUpdateDetails;
    }

}
