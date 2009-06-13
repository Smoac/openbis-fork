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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Attachment;

/**
 * @author Piotr Buczek
 */
public enum AttachmentColDefKind implements IColumnDefinitionKind<Attachment>
{
    FILE_NAME(new AbstractColumnDefinitionKind<Attachment>(Dict.FILE_NAME, 200)
        {
            @Override
            public String tryGetValue(Attachment entity)
            {
                return entity.getFileName();
                // return LinkRenderer.renderAsLink(message);
            }
        }),

    REGISTRATOR(new AbstractColumnDefinitionKind<Attachment>(Dict.REGISTRATOR)
        {
            @Override
            public String tryGetValue(Attachment entity)
            {
                return renderRegistrator(entity);
            }
        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<Attachment>(Dict.REGISTRATION_DATE,
            AbstractColumnDefinitionKind.DATE_COLUMN_WIDTH)
        {
            @Override
            public String tryGetValue(Attachment entity)
            {
                return renderRegistrationDate(entity);
            }
        }),

    VERSIONS(new AbstractColumnDefinitionKind<Attachment>(Dict.VERSIONS, 180)
        {
            @Override
            public String tryGetValue(Attachment entity)
            {
                return String.valueOf(entity.getVersion());
                // final String message =
                // messageProvider.getMessage(Dict.VERSIONS_TEMPLATE, versions.size());
                // return LinkRenderer.renderAsLink(message);
            }
        });

    private final AbstractColumnDefinitionKind<Attachment> columnDefinitionKind;

    private AttachmentColDefKind(AbstractColumnDefinitionKind<Attachment> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<Attachment> getDescriptor()
    {
        return columnDefinitionKind;
    }
}
