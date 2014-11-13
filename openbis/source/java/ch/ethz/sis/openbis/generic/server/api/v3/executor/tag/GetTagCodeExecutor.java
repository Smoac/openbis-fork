/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.tag;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagCode;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectIdentifier;

/**
 * @author pkupczyk
 */
@Component
public class GetTagCodeExecutor implements IGetTagCodeExecutor
{

    public GetTagCodeExecutor()
    {
    }

    @Override
    public String getTagCode(IOperationContext context, ITagId tagId)
    {
        if (tagId instanceof TagCode)
        {
            return ((TagCode) tagId).getCode();
        }
        if (tagId instanceof TagPermId)
        {
            TagPermId tagPermId = (TagPermId) tagId;
            MetaprojectIdentifier tagIdentifier =
                    MetaprojectIdentifier.parse(tagPermId.getPermId());
            String name = tagIdentifier.getMetaprojectName();
            String ownerId = tagIdentifier.getMetaprojectOwnerId();
            String userId = context.getSession().tryGetPerson().getUserId();
            if (ownerId.equals(userId) == false)
            {
                throw new UnauthorizedObjectAccessException(tagId);
            }
            return name;
        }
        throw new NotImplementedException("Tag id [" + tagId + "] is of unknown type: "
                + tagId.getClass().getName());
    }

}
