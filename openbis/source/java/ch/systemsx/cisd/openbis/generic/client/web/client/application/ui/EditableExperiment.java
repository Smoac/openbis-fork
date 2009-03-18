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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;

/**
 * @author Izabela Adamczyk
 */
public class EditableExperiment extends
        EditableEntity<ExperimentType, ExperimentTypePropertyType, ExperimentProperty>
{

    private final String project;

    private String code;

    public EditableExperiment(List<ExperimentTypePropertyType> etpts,
            List<ExperimentProperty> properties, ExperimentType type, String identifier, Long id,
            Date modificationDate, String project, String code)
    {
        super(EntityKind.EXPERIMENT, etpts, properties, type, identifier, id, modificationDate);
        this.project = project;
        this.code = code;
    }

    public String getProjectIdentifier()
    {
        return project;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public String getCode()
    {
        return code;
    }

}
