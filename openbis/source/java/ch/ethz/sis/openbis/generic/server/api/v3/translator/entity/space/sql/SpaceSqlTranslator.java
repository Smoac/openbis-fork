/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.space.sql;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.space.ISpaceTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class SpaceSqlTranslator implements ISpaceSqlTranslator
{

    @Autowired
    private ISpaceTranslator spaceTranslator;

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    public Space translate(TranslationContext context, Long spaceId, SpaceFetchOptions fetchOptions)
    {
        return translate(context, Collections.singletonList(spaceId), fetchOptions).get(spaceId);
    }

    @Override
    public Map<Long, Space> translate(TranslationContext context, Collection<Long> spaceIds, SpaceFetchOptions fetchOptions)
    {
        List<SpacePE> spaces = daoFactory.getSpaceDAO().listByIDs(spaceIds);

        Map<SpacePE, Space> spacePeToSpace = spaceTranslator.translate(context, spaces, fetchOptions);
        Map<Long, Space> spaceIdToSpace = new HashMap<Long, Space>();

        for (SpacePE space : spaces)
        {
            spaceIdToSpace.put(space.getId(), spacePeToSpace.get(space));
        }

        return spaceIdToSpace;
    }

}
