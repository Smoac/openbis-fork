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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.tag.TagAuthorization;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class CreateTagExecutor implements ICreateTagExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IGetTagIdentifierExecutor getTagIdentifierExecutor;

    @SuppressWarnings("unused")
    private CreateTagExecutor()
    {
    }

    public CreateTagExecutor(IDAOFactory daoFactory, IGetTagIdentifierExecutor getTagIdentifierExecutor)
    {
        this.daoFactory = daoFactory;
        this.getTagIdentifierExecutor = getTagIdentifierExecutor;
    }

    @Override
    public MetaprojectPE createTag(IOperationContext context, ITagId tagId)
    {
        MetaprojectPE tag = new MetaprojectPE();
        MetaprojectIdentifier identifier = getTagIdentifierExecutor.getIdentifier(context, tagId);

        tag.setName(identifier.getMetaprojectName());
        tag.setOwner(context.getSession().tryGetPerson());
        tag.setCreationDate(new Date());
        tag.setPrivate(true);

        daoFactory.getMetaprojectDAO().createOrUpdateMetaproject(tag, context.getSession().tryGetPerson());

        new TagAuthorization(context, daoFactory).checkAccess(tag);

        return tag;
    }

}
