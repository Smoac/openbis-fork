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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import ch.systemsx.cisd.common.annotation.BeanProperty;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class UpdatedBasicExperiment extends NewBasicExperiment
{
    public static final String EXPERIMENT_UPDATE_TEMPLATE_COMMENT =
            "# All columns except \"identifier\" can be removed from the file.\n"
                    + "# If a column is removed from the file or a cell in a column is left empty the corresponding values of updated expriments will be preserved.\n"
                    + "# To delete a value/connection from openBIS one needs to put \"--DELETE--\" into the corresponding cell\n"
                    + "# The \"identifier\" column should contain experiment identifiers, e.g. /SPACE/PROJECT/EXPERIMENT_1,\n"
                    + "# The \"project\" column (if not removed) should contain project identifiers, e.g. /SPACE/PROJECT\n";

    public static final String PROJECT = "project";

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String newProjectIdentifierOrNull;

    private ExperimentBatchUpdateDetails batchUpdateDetails;

    public UpdatedBasicExperiment()
    {
    }

    public UpdatedBasicExperiment(String experimentId, String newProjectIdentifierOrNull,
            ExperimentBatchUpdateDetails batchUpdateDetails)
    {
        super(experimentId);
        setNewProjectIdentifierOrNull(newProjectIdentifierOrNull);
        this.batchUpdateDetails = batchUpdateDetails;
    }

    public String getNewProjectIdentifierOrNull()
    {
        return newProjectIdentifierOrNull;
    }

    @BeanProperty(label = PROJECT, optional = true)
    public void setNewProjectIdentifierOrNull(String newProjectIdentifierOrNull)
    {
        this.newProjectIdentifierOrNull = newProjectIdentifierOrNull;
    }

    public ExperimentBatchUpdateDetails getBatchUpdateDetails()
    {
        return batchUpdateDetails;
    }

    public void setBatchUpdateDetails(ExperimentBatchUpdateDetails batchUpdateDetails)
    {
        this.batchUpdateDetails = batchUpdateDetails;
    }
}
