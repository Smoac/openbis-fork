/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.gui;

import ch.systemsx.cisd.openbis.uitest.functionality.AbstractExecution;
import ch.systemsx.cisd.openbis.uitest.functionality.CreatePropertyType;
import ch.systemsx.cisd.openbis.uitest.layout.AddPropertyTypeLocation;
import ch.systemsx.cisd.openbis.uitest.page.AddPropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;

/**
 * @author anttil
 */
public class CreatePropertyTypeGui extends AbstractExecution<CreatePropertyType, PropertyType>
{

    @Override
    public PropertyType run(CreatePropertyType request)
    {
        PropertyType propertyType = request.getType();
        AddPropertyType dialog = browseTo(new AddPropertyTypeLocation());
        dialog.fillWith(propertyType);
        dialog.save();
        return propertyType;
    }

}
