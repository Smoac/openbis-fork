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

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * Defines the ways links are created for objects of selected types.
 * 
 * @author Izabela Adamczyk
 */
public class LinkExtractor
{

    public static final String tryExtract(IEntityInformationHolder o)
    {
        if (o == null)
        {
            return null;
        }
        switch (o.getEntityKind())
        {
            case EXPERIMENT:
                return tryExtract((Experiment) o);
            case SAMPLE:
                return tryExtract((Sample) o);
            case DATA_SET:
                return tryExtract((ExternalData) o);
            case MATERIAL:
                return tryExtract((Material) o);
            default:
                return null;
        }
    }

    public static final String tryExtract(Material m)
    {
        if (m == null)
        {
            return null;
        }
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter("ACTION", "VIEW");
        url.addParameter(PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY, m.getEntityKind().name());
        url.addParameter("code", m.getCode());
        url.addParameter("type", m.getMaterialType().getCode());// FIXME: move to common
        return print(url);
    }

    // TODO 2010-04-21, IA: To be removed after there are click handler->href conversion is
    // finnished
    private static String print(URLMethodWithParameters url)
    {
        return url.toString().substring(1);
    }

    public static final String tryExtract(Sample s)
    {
        if (s == null)
        {
            return null;
        }
        return createPermLink(s.getEntityKind(), s.getPermId());
    }

    public static final String tryExtract(Experiment e)
    {
        if (e == null)
        {
            return null;
        }
        return createPermLink(e.getEntityKind(), e.getPermId());
    }

    public static String tryExtract(ExternalData e)
    {
        if (e == null)
        {
            return null;
        }
        return createPermLink(e.getEntityKind(), e.getPermId());
    }

    private static String createPermLink(EntityKind kind, String permId)
    {
        if (permId == null)
        {
            return null;
        }
        URLMethodWithParameters url = new URLMethodWithParameters("");
        url.addParameter(PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY, kind.name());
        url.addParameter(PermlinkUtilities.PERM_ID_PARAMETER_KEY, permId);
        return print(url);
    }

}
