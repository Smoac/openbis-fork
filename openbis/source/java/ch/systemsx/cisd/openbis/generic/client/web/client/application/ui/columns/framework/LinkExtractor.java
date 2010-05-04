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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.MaterialLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ProjectLocatorResolver;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;

/**
 * Defines the ways links are created for objects of selected types.
 * 
 * @author Izabela Adamczyk
 */
public class LinkExtractor
{

    public static String tryExtract(IEntityInformationHolderWithIdentifier e)
    {
        if (e == null)
        {
            return null;
        }
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY, e.getEntityKind().name());
        url.addParameter(PermlinkUtilities.PERM_ID_PARAMETER_KEY, e.getPermId());
        return print(url);
    }

    public static final String tryExtract(Project p)
    {
        if (p == null)
        {
            return null;
        }
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY,
                ProjectLocatorResolver.PROJECT);
        url.addParameter(ProjectLocatorResolver.CODE_PARAMETER_KEY, p.getCode());
        url.addParameter(ProjectLocatorResolver.SPACE_PARAMETER_KEY, p.getSpace().getCode());
        return print(url);
    }

    public static final String tryExtract(Material material)
    {
        if (material == null)
        {
            return null;
        }
        return tryCreateMaterialLink(material.getCode(), material.getMaterialType().getCode());
    }

    public static final String tryExtract(MaterialIdentifier identifier)
    {
        if (identifier == null)
        {
            return null;
        }
        return tryCreateMaterialLink(identifier.getCode(), identifier.getTypeCode());
    }

    private static final String tryCreateMaterialLink(String materialCode, String materialTypeCode)
    {
        if (materialCode == null || materialTypeCode == null)
        {
            return null;
        }
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY, EntityKind.MATERIAL.name());
        url.addParameter(MaterialLocatorResolver.CODE_PARAMETER_KEY, materialCode);
        url.addParameter(MaterialLocatorResolver.TYPE_PARAMETER_KEY, materialTypeCode);
        return print(url);
    }

    private static String print(URLMethodWithParameters url)
    {
        return url.toString().substring(1);
    }
}
