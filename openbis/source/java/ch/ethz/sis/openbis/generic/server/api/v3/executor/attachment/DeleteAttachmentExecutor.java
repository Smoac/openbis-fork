/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.attachment;

import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.attachment.AttachmentFileName;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.attachment.IAttachmentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnsupportedObjectIdException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
@Component
public class DeleteAttachmentExecutor implements IDeleteAttachmentExecutor
{

    private IAttachmentDAO attachmentDAO;

    private IEventDAO eventDAO;

    @Override
    public void delete(IOperationContext context, AttachmentHolderPE attachmentHolder, Collection<? extends IAttachmentId> attachmentIds)
    {
        for (IAttachmentId attachmentId : attachmentIds)
        {
            if (attachmentId instanceof AttachmentFileName)
            {
                delete(context, attachmentHolder, ((AttachmentFileName) attachmentId).getFileName());
            } else
            {
                throw new UnsupportedObjectIdException(attachmentId);
            }
        }
    }

    private void delete(IOperationContext context, AttachmentHolderPE attachmentHolder, String fileName)
    {
        try
        {
            attachmentDAO.deleteByOwnerAndFileName(attachmentHolder, fileName);
            eventDAO.persist(createEvent(attachmentHolder, fileName, context.getSession().tryGetPerson()));
        } catch (final DataAccessException ex)
        {
            DataAccessExceptionTranslator.throwException(ex, "Attachment '" + fileName + "'", null);
        }
    }

    private EventPE createEvent(AttachmentHolderPE holder, String fileName,
            PersonPE registrator)
    {
        EventPE event = new EventPE();
        String identifier = String.format("%s/%s/%s", holder.getHolderName(), holder.getIdentifier(), fileName);
        event.setEventType(EventType.DELETION);
        event.setEntityType(EntityType.ATTACHMENT);
        event.setIdentifiers(Collections.singletonList(identifier));
        event.setDescription(identifier);
        event.setRegistrator(registrator);
        return event;
    }

    @Autowired
    public void setDaoFactory(IDAOFactory daoFactory)
    {
        attachmentDAO = daoFactory.getAttachmentDAO();
        eventDAO = daoFactory.getEventDAO();
    }

}
