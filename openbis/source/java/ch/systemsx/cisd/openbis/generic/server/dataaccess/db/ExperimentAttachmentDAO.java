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

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * Implementation of {@link IExperimentAttachmentDAO} for data bases.
 * 
 * @author Franz-Josef Elmer
 * @author Tomasz Pylak
 */
final class ExperimentAttachmentDAO extends AbstractDAO implements IExperimentAttachmentDAO
{
    private final static Class<AttachmentPE> ATTACHMENT_CLASS = AttachmentPE.class;

    private final static String TABLE_NAME = ATTACHMENT_CLASS.getSimpleName();

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ExperimentAttachmentDAO.class);

    ExperimentAttachmentDAO(final SessionFactory sessionFactory,
            final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
    }

    private final int findLastVersion(final AttachmentPE fileAttachment, final ExperimentPE owner)
    {
        final String query = createFindLastVersionQuery();
        final List<Integer> versions =
                cast(getHibernateTemplate().find(query,
                        toArray(owner, fileAttachment.getFileName())));
        final Integer lastVersion = getEntity(versions);
        return lastVersion == null ? 0 : lastVersion.intValue();
    }

    private final static String createFindLastVersionQuery()
    {
        return String
                .format("select max(version) from %s where parentInternal = ? and fileName = ?",
                        TABLE_NAME);
    }

    //
    // IExperimentAttachmentDAO
    //

    public final void createExperimentAttachment(final AttachmentPE attachment,
            final ExperimentPE owner) throws DataAccessException
    {
        assert attachment != null : "Unspecified attachment";
        assert attachment.getAttachmentContent() != null : "Unspecified attachment content.";
        validatePE(attachment.getAttachmentContent());

        final int version = findLastVersion(attachment, owner) + 1;
        attachment.setVersion(version);
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

    public final List<AttachmentPE> listExperimentAttachments(final ExperimentPE experiment)
            throws DataAccessException
    {
        assert experiment != null : "Unspecified parent experiment.";

        final String query = String.format("from %s where parentInternal = ?", TABLE_NAME);
        final List<AttachmentPE> result =
                cast(getHibernateTemplate().find(query, toArray(experiment)));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d attachment(s) found for experiment '%s'.", result
                    .size(), experiment));
        }
        return result;
    }

    public final AttachmentPE tryFindExpAttachmentByExpAndFileName(final ExperimentPE experiment,
            final String fileName) throws DataAccessException
    {
        assert fileName != null : "Unspecified file name.";
        assert experiment != null : "Unspecified parent experiment.";

        final String query =
                String.format("from %s where parentInternal = ? and fileName = ? and version = ("
                        + createFindLastVersionQuery() + ")", TABLE_NAME);
        final List<AttachmentPE> result =
                cast(getHibernateTemplate().find(query,
                        toArray(experiment, fileName, experiment, fileName)));
        final AttachmentPE attachment = tryFindEntity(result, "attachment", experiment, fileName);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s found for experiment '%s' and file name '%s'.",
                    attachment == null ? "No attachment" : "Attachment '" + attachment + "'",
                    experiment, fileName));
        }
        return attachment;
    }

    public final AttachmentPE tryFindExpAttachmentByExpAndFileNameAndVersion(
            final ExperimentPE experiment, final String fileName, final int version)
            throws DataAccessException
    {
        assert experiment != null : "Unspecified experiment.";
        assert fileName != null : "Unspecified file name.";
        assert version > 0 : "Version must be > 0.";

        final String query =
                String.format("from %s where parentInternal = ? and fileName = ? and version = ?",
                        TABLE_NAME);
        final List<AttachmentPE> result =
                cast(getHibernateTemplate().find(query, toArray(experiment, fileName, version)));
        final AttachmentPE attachment =
                tryFindEntity(result, "attachment", experiment, fileName, version);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%s found for experiment '%s', file name '%s' and version %d.",
                    attachment == null ? "No attachment" : "Attachment '" + attachment + "'",
                    experiment, fileName, version));
        }
        return attachment;
    }
}
