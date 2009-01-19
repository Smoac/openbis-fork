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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import com.extjs.gxt.ui.client.data.BaseModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.InvalidableWithCodeRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.CommonColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CodeWithRegistration;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IInvalidationProvider;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class AbstractEntityModel<T extends CodeWithRegistration<T>> extends BaseModelData
{
    private static final long serialVersionUID = 1L;

    protected String renderColumnValue(final T entity, IColumnDefinition<T> column)
    {
        String value = column.getValue(entity);
        if (column instanceof CommonColumnDefinition)
        {
            IColumnDefinitionKind<?> columnKind =
                    ((CommonColumnDefinition<?>) column).getColumnDefinitionKind();
            String headerMsgKey = columnKind.getHeaderMsgKey();
            if (headerMsgKey.equals(Dict.REGISTRATOR))
            {
                value = PersonRenderer.createPersonAnchor(entity.getRegistrator(), value);
            } else if (headerMsgKey.equals(Dict.CODE) && entity instanceof IInvalidationProvider)
            {
                value = InvalidableWithCodeRenderer.render((IInvalidationProvider) entity, value);
            }
        }
        return value;
    }


}
