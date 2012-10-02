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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * Translator for {@link MetaprojectPE} into {@link Metaproject} and other way around.
 * 
 * @author Pawel Glyzewski
 */
public class MetaprojectTranslator
{
    public static List<Metaproject> translate(List<MetaprojectPE> metaprojectPEs)
    {
        List<Metaproject> metaprojects = new ArrayList<Metaproject>(metaprojectPEs.size());

        for (MetaprojectPE metaprojectPE : metaprojectPEs)
        {
            metaprojects.add(translate(metaprojectPE));
        }

        return metaprojects;
    }

    public static Metaproject translate(MetaprojectPE metaprojectPE)
    {
        Metaproject metaproject = new Metaproject();

        metaproject.setId(metaprojectPE.getId());
        metaproject.setName(metaprojectPE.getName());
        metaproject.setOwnerId(metaprojectPE.getOwner().getUserId());
        metaproject.setDescription(metaprojectPE.getDescription());
        metaproject.setPrivate(metaprojectPE.isPrivate());
        metaproject.setCreationDate(metaprojectPE.getCreationDate());

        return metaproject;
    }

    public static MetaprojectPE translate(Metaproject metaproject, MetaprojectPE metaprojectPE)
    {
        metaprojectPE.setName(metaproject.getName().toUpperCase());
        metaprojectPE.setDescription(metaproject.getDescription());
        metaprojectPE.setPrivate(true);

        return metaprojectPE;
    }

}
