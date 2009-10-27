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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer;

import com.google.gwt.i18n.client.NumberFormat;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.EntityPropertyColDef;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;

/**
 * An {@link AbstractPropertyColRenderer} which renders Real values.
 * 
 * @author Izabela Adamczyk
 */
class RealPropertyColRenderer<T extends IEntityPropertiesHolder> extends
        AbstractPropertyColRenderer<T>
{

    private static final int MAX_DIGITAL_FORMAT_LENGTH = 10;
    private static final String SCIENTIFIC_FORMAT = "0.0000E00";
    private static final String DIGITAL_FORMAT = "0.0000";

    public RealPropertyColRenderer(EntityPropertyColDef<T> colDef)
    {
        super(colDef);
    }

    @Override
    protected String renderValue(GridRowModel<T> entity)
    {
        String value = colDef.getValue(entity);
        double doubleValue = Double.parseDouble(value);
        String formattedValue = NumberFormat.getFormat(DIGITAL_FORMAT).format(doubleValue);
        if (formattedValue.length() > MAX_DIGITAL_FORMAT_LENGTH)
        {
            formattedValue = NumberFormat.getFormat(SCIENTIFIC_FORMAT).format(doubleValue);
        }
        return formattedValue;
    }

}
