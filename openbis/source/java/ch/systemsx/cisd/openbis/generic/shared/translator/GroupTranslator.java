/*
 * Copyright 2008 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * A {@link Space} &lt;---&gt; {@link GroupPE} translator.
 * 
 * @author Franz-Josef Elmer
 */
public final class GroupTranslator
{
    private GroupTranslator()
    {
        // Can not be instantiated.
    }

    public final static List<Space> translate(final List<GroupPE> groups)
    {
        final List<Space> result = new ArrayList<Space>();
        for (final GroupPE group : groups)
        {
            result.add(GroupTranslator.translate(group));
        }
        return result;
    }

    public static Space translate(final GroupPE group)
    {
        if (group == null)
        {
            return null;
        }
        final Space result = new Space();
        result.setId(HibernateUtils.getId(group));
        result.setCode(group.getCode());
        result.setDescription(group.getDescription());
        result.setInstance(DatabaseInstanceTranslator.translate(group.getDatabaseInstance()));
        result.setRegistrationDate(group.getRegistrationDate());
        result.setRegistrator(PersonTranslator.translate(group.getRegistrator()));
        result.setIdentifier(IdentifierHelper.createGroupIdentifier(group).toString());
        return result;
    }
}
