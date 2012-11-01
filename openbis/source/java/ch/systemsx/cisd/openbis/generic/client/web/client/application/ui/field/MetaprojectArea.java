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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;

/**
 * {@link MultilineItemsField} extension to specify metaprojects.
 * 
 * @author pkupczyk
 */
public class MetaprojectArea extends MultilineItemsField
{

    public static final String ID_SUFFIX = "_metaprojects";

    public MetaprojectArea(IMessageProvider messageProvider, String idPrefix)
    {
        super("", false);
        setFieldLabel(messageProvider.getMessage(Dict.METAPROJECTS));
        setEmptyText(messageProvider.getMessage(Dict.METAPROJECTS_HINT));
        setId(idPrefix + ID_SUFFIX);
    }

    public void setMetaprojects(Collection<Metaproject> metaprojects)
    {
        List<String> names = new ArrayList<String>();

        if (metaprojects != null)
        {
            for (Metaproject metaproject : metaprojects)
            {
                names.add(metaproject.getName());
            }
        }

        setMetaprojects(names);
    }

    public final void setMetaprojects(List<String> namesOrIdentifiers)
    {
        setItems(namesOrIdentifiers);
    }

    public final String[] tryGetMetaprojects()
    {
        return tryGetModifiedItemList();
    }

}
