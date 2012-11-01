package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.common.spring.IUncheckedMultipartFile;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;

public abstract class AttachmentRegistrationHelper
{
    private static final long MAX_ATTACHMENT_SIZE = 256 * FileUtils.ONE_MB;

    abstract public void register(Collection<NewAttachment> attachmentPEs);

    final public void process(String sessionKey, HttpSession httpSession,
            List<NewAttachment> attachmentsOrNull)
    {
        UploadedFilesBean uploadedFiles = null;
        try
        {
            uploadedFiles = (UploadedFilesBean) httpSession.getAttribute(sessionKey);
            abortIfMaxSizeExceeded(uploadedFiles);
            final Map<String, NewAttachment> attachmentMap = createAttachmentMap(attachmentsOrNull);
            fillContent(uploadedFiles, attachmentMap);
            register(attachmentMap.values());
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        } finally
        {
            if (uploadedFiles != null)
            {
                uploadedFiles.deleteTransferredFiles();
            }
            if (httpSession != null)
            {
                httpSession.removeAttribute(sessionKey);
            }
        }
    }

    private Map<String, NewAttachment> createAttachmentMap(
            final List<NewAttachment> attachmentsOrNull)
    {
        // fileName -> attachment
        Map<String, NewAttachment> result = new HashMap<String, NewAttachment>();
        if (attachmentsOrNull != null)
        {
            for (NewAttachment attachment : attachmentsOrNull)
            {
                result.put(attachment.getFileName(), attachment);
            }
        }
        return result;
    }

    private void abortIfMaxSizeExceeded(UploadedFilesBean uploadedFiles)
    {
        if (uploadedFiles != null)
        {
            for (final IUncheckedMultipartFile multipartFile : uploadedFiles.iterable())
            {
                long fileSize = multipartFile.getSize();
                if (fileSize > MAX_ATTACHMENT_SIZE)
                {
                    String maxSizeString =
                            FileUtilities.byteCountToDisplaySize(MAX_ATTACHMENT_SIZE);
                    String fileSizeString = FileUtilities.byteCountToDisplaySize(fileSize);
                    String errorMessage =
                            String.format(
                                    "The file %s(%s) is larger than the configured maximum (%s).",
                                    multipartFile.getOriginalFilename(), fileSizeString,
                                    maxSizeString);
                    throw new UserFailureException(errorMessage);
                }
            }
        }

    }

    private void fillContent(UploadedFilesBean uploadedFiles, Map<String, NewAttachment> attachments)
    {
        if (uploadedFiles != null)
        {
            for (final IUncheckedMultipartFile multipartFile : uploadedFiles.iterable())
            {
                final String fileName = multipartFile.getOriginalFilename();
                // NOTE: this will load the entire attachments in memory
                final byte[] content = multipartFile.getBytes();
                final NewAttachment attachmentOrNull = attachments.get(fileName);
                if (attachmentOrNull != null)
                {
                    attachmentOrNull.setContent(content);
                }
            }
        }
    }

}
