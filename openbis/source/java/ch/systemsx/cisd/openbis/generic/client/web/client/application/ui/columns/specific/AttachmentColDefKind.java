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
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentVersions;

/**
 * @author Piotr Buczek
 */
public enum AttachmentColDefKind implements IColumnDefinitionKind<AttachmentVersions>
{
    FILE_NAME(new AbstractColumnDefinitionKind<AttachmentVersions>(Dict.FILE_NAME, 200)
        {
            @Override
            public String tryGetValue(AttachmentVersions entity)
            {
                return entity.getCurrent().getFileName();
            }
        }),

    PERMLINK(new AbstractColumnDefinitionKind<AttachmentVersions>(Dict.PERMLINK, true)
        {
            @Override
            public String tryGetValue(AttachmentVersions entity)
            {
                return entity.getPermlink();
            }
        }),

    VERSION(new AbstractColumnDefinitionKind<AttachmentVersions>(Dict.VERSION)
        {
            @Override
            public String tryGetValue(AttachmentVersions entity)
            {
                return String.valueOf(entity.getCurrent().getVersion());
            }
        }),

    TITLE(new AbstractColumnDefinitionKind<AttachmentVersions>(Dict.TITLE, 200)
        {
            @Override
            public String tryGetValue(AttachmentVersions entity)
            {
                return entity.getCurrent().getTitle();
            }
        }),

    DESCRIPTION(new AbstractColumnDefinitionKind<AttachmentVersions>(Dict.DESCRIPTION, 300)
        {
            @Override
            public String tryGetValue(AttachmentVersions entity)
            {
                return entity.getCurrent().getDescription();
            }
        }),

    REGISTRATOR(new AbstractColumnDefinitionKind<AttachmentVersions>(Dict.REGISTRATOR)
        {
            @Override
            public String tryGetValue(AttachmentVersions entity)
            {
                return renderRegistrator(entity.getCurrent());
            }
        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<AttachmentVersions>(Dict.REGISTRATION_DATE,
            AbstractColumnDefinitionKind.DATE_COLUMN_WIDTH)
        {
            @Override
            public String tryGetValue(AttachmentVersions entity)
            {
                return renderRegistrationDate(entity.getCurrent());
            }
        });

    private final AbstractColumnDefinitionKind<AttachmentVersions> columnDefinitionKind;

    private AttachmentColDefKind(
            AbstractColumnDefinitionKind<AttachmentVersions> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<AttachmentVersions> getDescriptor()
    {
        return columnDefinitionKind;
    }
}
