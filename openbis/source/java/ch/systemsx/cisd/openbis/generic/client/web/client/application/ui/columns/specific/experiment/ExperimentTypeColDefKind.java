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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.experiment;

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.EntityTypeColDefKindFactory.experimentTypeColDefKindFactory;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;

/**
 * Columns definition for browsing grid of {@link ExperimentType}s.
 * 
 * @author Piotr Buczek
 */
public enum ExperimentTypeColDefKind implements IColumnDefinitionKind<ExperimentType>
{
    // copy from EntityTypeColDefKind (cannot extend an enum)

    CODE(experimentTypeColDefKindFactory.createCodeColDefKind()),

    DESCRIPTION(experimentTypeColDefKindFactory.createDescriptionColDefKind()),

    DATABASE_INSTANCE(experimentTypeColDefKindFactory.createDatabaseInstanceColDefKind());

    // no specific Experiment Type columns

    private final AbstractColumnDefinitionKind<ExperimentType> columnDefinitionKind;

    private ExperimentTypeColDefKind(
            AbstractColumnDefinitionKind<ExperimentType> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<ExperimentType> getDescriptor()
    {
        return columnDefinitionKind;
    }

}
