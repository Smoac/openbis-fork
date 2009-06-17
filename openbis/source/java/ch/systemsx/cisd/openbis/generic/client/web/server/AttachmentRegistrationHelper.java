package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.IUncheckedMultipartFile;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentContentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;

public abstract class AttachmentRegistrationHelper
{

    abstract public void register(List<AttachmentPE> attachmentPEs);

    final public void process(String sessionKey, HttpSession httpSession,
            List<NewAttachment> attachments)
    {
        UploadedFilesBean uploadedFiles = null;
        try
        {
            uploadedFiles = (UploadedFilesBean) httpSession.getAttribute(sessionKey);
            Map<String, AttachmentContentPE> contents =
                    createUploadedFilesContentsMap(uploadedFiles);
            register(createAttachmentPEs(attachments, contents));
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

    private Map<String, AttachmentContentPE> createUploadedFilesContentsMap(
            UploadedFilesBean uploadedFiles)
    {
        // fileName -> content
        Map<String, AttachmentContentPE> result = new HashMap<String, AttachmentContentPE>();
        if (uploadedFiles != null)
        {
            for (final IUncheckedMultipartFile multipartFile : uploadedFiles.iterable())
            {
                String fileName = multipartFile.getOriginalFilename();
                byte[] content = multipartFile.getBytes();
                result.put(fileName, createContentPE(content));
            }
        }

        return result;
    }

    private final AttachmentContentPE createContentPE(final byte[] content)
    {
        final AttachmentContentPE attachmentContent = new AttachmentContentPE();
        attachmentContent.setValue(content);
        return attachmentContent;
    }

    private List<AttachmentPE> createAttachmentPEs(List<NewAttachment> attachments,
            Map<String, AttachmentContentPE> contents)
    {
        List<AttachmentPE> result = new ArrayList<AttachmentPE>();
        if (attachments == null)
        {
            assert contents.isEmpty() : "no attachments specified to fill with content";
        } else
        {
            assert attachments.size() == contents.size() : "data loss";

            for (NewAttachment attachment : attachments)
            {
                final String fileName = getFileName(attachment.getFilePath());
                final AttachmentContentPE content = contents.get(fileName);
                result.add(createAttachmentPE(attachment, fileName, content));
            }
        }
        return result;
    }

    private String getFileName(String filePath)
    {
        return new File(filePath).getName();
    }

    private final AttachmentPE createAttachmentPE(final NewAttachment attachment,
            final String fileName, final AttachmentContentPE content)
    {
        assert fileName != null : "file name not set";
        assert attachment != null : "attachment not set";
        assert content != null : "content not set";

        final AttachmentPE result = new AttachmentPE();
        result.setFileName(fileName);
        result.setDescription(attachment.getDescription());
        result.setTitle(attachment.getTitle());
        result.setAttachmentContent(content);
        return result;
    }
}
