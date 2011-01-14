/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * Implementation of {@link IAttachmentDAO} for data bases.
 * 
 * @author Franz-Josef Elmer
 * @author Tomasz Pylak
 */
final class AttachmentDAO extends AbstractGenericEntityDAO<AttachmentPE> implements IAttachmentDAO
{

    private final static Class<AttachmentPE> ATTACHMENT_CLASS = AttachmentPE.class;

    private final static String TABLE_NAME = ATTACHMENT_CLASS.getSimpleName();

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AttachmentDAO.class);

    AttachmentDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, ATTACHMENT_CLASS);
    }

    private final static String createFindLastVersionQuery(AttachmentHolderPE owner)
    {
        return createFindLastVersionQuery(owner, "?");
    }

    private final static String createFindLastVersionQuery(AttachmentHolderPE owner, String fileName)
    {
        String ownerAsParent = getParentName(owner);
        return String.format("select max(version) from %s where " + ownerAsParent
                + " = ? and fileName = %s", TABLE_NAME, fileName);
    }

    private static String getParentName(AttachmentHolderPE owner)
    {
        String ownerAsParent = owner.getHolderName() + "ParentInternal";
        return ownerAsParent;
    }

    //
    // IAttachmentDAO
    //

    public final void createAttachment(final AttachmentPE attachment, final AttachmentHolderPE owner)
            throws DataAccessException
    {
        assert attachment != null : "Unspecified attachment";
        assert attachment.getAttachmentContent() != null : "Unspecified attachment content.";
        validatePE(attachment.getAttachmentContent());

        final AttachmentPE previousAttachmentVersionOrNull =
                tryFindAttachmentByOwnerAndFileName(owner, attachment.getFileName());
        fillAttachmentData(attachment, previousAttachmentVersionOrNull);

        owner.addAttachment(attachment);
        validatePE(attachment);

        final HibernateTemplate template = getHibernateTemplate();
        template.save(attachment);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: file attachment '%s'.", attachment));
        }
    }

    private void fillAttachmentData(AttachmentPE attachment,
            AttachmentPE previousAttachmentVersionOrNull)
    {
        int previousVersion = 0;
        if (previousAttachmentVersionOrNull != null)
        {
            previousVersion = previousAttachmentVersionOrNull.getVersion();
            if (StringUtils.isBlank(attachment.getTitle()))
            {
                attachment.setTitle(previousAttachmentVersionOrNull.getTitle());
            }
            if (StringUtils.isBlank(attachment.getDescription()))
            {
                attachment.setDescription(previousAttachmentVersionOrNull.getDescription());
            }
        }
        attachment.setVersion(previousVersion + 1);
    }

    public final List<AttachmentPE> listAttachments(final AttachmentHolderPE owner)
            throws DataAccessException
    {
        assert owner != null : "Unspecified attachment holder.";

        final String query =
                String.format("from %s where " + getParentName(owner) + " = ?", TABLE_NAME);
        final List<AttachmentPE> result = cast(getHibernateTemplate().find(query, toArray(owner)));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d attachment(s) found for " + owner.getHolderName()
                    + " '%s'.", result.size(), owner));
        }
        return result;
    }

    public final AttachmentPE tryFindAttachmentByOwnerAndFileName(final AttachmentHolderPE owner,
            final String fileName) throws DataAccessException
    {
        assert fileName != null : "Unspecified file name.";
        assert owner != null : "Unspecified parent attachment holder.";

        final String query =
                String.format("from %s where " + getParentName(owner)
                        + " = ? and fileName = ? and version = ("
                        + createFindLastVersionQuery(owner) + ")", TABLE_NAME);
        final List<AttachmentPE> result =
                cast(getHibernateTemplate().find(query, toArray(owner, fileName, owner, fileName)));
        final AttachmentPE attachment = tryFindEntity(result, "attachment", owner, fileName);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s found for " + owner.getHolderName()
                    + " '%s' and file name '%s'.", attachment == null ? "No attachment"
                    : "Attachment '" + attachment + "'", owner, fileName));
        }
        return attachment;
    }

    public final AttachmentPE tryFindAttachmentByOwnerAndFileNameAndVersion(
            final AttachmentHolderPE owner, final String fileName, final int version)
            throws DataAccessException
    {
        assert owner != null : "Unspecified attachment holder.";
        assert fileName != null : "Unspecified file name.";
        assert version > 0 : "Version must be > 0.";

        final String query =
                String.format("from %s where " + getParentName(owner)
                        + " = ? and fileName = ? and version = ?", TABLE_NAME);
        final List<AttachmentPE> result =
                cast(getHibernateTemplate().find(query, toArray(owner, fileName, version)));
        final AttachmentPE attachment =
                tryFindEntity(result, "attachment", owner, fileName, version);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s found for " + owner.getHolderName()
                    + " '%s', file name '%s' and version %d.", attachment == null ? "No attachment"
                    : "Attachment '" + attachment + "'", owner, fileName, version));
        }
        return attachment;
    }

    public int deleteByOwnerAndFileName(final AttachmentHolderPE owner, final String fileName)
            throws DataAccessException
    {
        assert owner != null : "Unspecified attachment holder.";
        assert fileName != null : "Unspecified file name.";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();

        int deletedRows = 0;
        for (AttachmentPE att : owner.getAttachments())
        {
            if (fileName.equals(att.getFileName()))
            {
                deletedRows++;
                hibernateTemplate.delete(att);
            }
        }

        hibernateTemplate.flush();

        if (operationLog.isInfoEnabled())
        {
            operationLog.debug(String.format(
                    "%s attachment(s) deleted for %s '%s' and file name '%s'.", deletedRows,
                    owner.getHolderName(), owner, fileName));
        }

        return deletedRows;
    }

}
