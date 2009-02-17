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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.columns;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.columns.AbstractPropertyColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * @author Tomasz Pylak
 */
public class PropertyExperimentColDef extends AbstractPropertyColDef<Experiment> implements
        IsSerializable
{
    // GWT only
    public PropertyExperimentColDef()
    {
        super(null, false, 0, false, null, null);
    }

    public PropertyExperimentColDef(PropertyType propertyType)
    {
        super(propertyType, true);
    }

    @Override
    protected List<? extends EntityProperty<?, ?>> getProperties(Experiment entity)
    {
        return entity.getProperties();
    }
}