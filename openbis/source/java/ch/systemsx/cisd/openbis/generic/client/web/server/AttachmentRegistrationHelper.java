package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.IUncheckedMultipartFile;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentContentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;

public abstract class AttachmentRegistrationHelper
{

    abstract public void register(List<AttachmentPE> attachments);

    final public void process(String sessionKey, HttpSession httpSession)
    {
        UploadedFilesBean uploadedFiles = null;
        try
        {
            uploadedFiles = (UploadedFilesBean) httpSession.getAttribute(sessionKey);
            List<AttachmentPE> attachments = new ArrayList<AttachmentPE>();
            if (uploadedFiles != null)
            {
                for (final IUncheckedMultipartFile multipartFile : uploadedFiles.iterable())
                {
                    String fileName = multipartFile.getOriginalFilename();
                    byte[] content = multipartFile.getBytes();
                    attachments.add(createAttachment(fileName, content));
                }
            }
            register(attachments);
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

    private final AttachmentPE createAttachment(String fileName, final byte[] content)
    {
        final AttachmentPE attachment = new AttachmentPE();
        attachment.setFileName(fileName);
        final AttachmentContentPE attachmentContent = new AttachmentContentPE();
        attachmentContent.setValue(content);
        attachment.setAttachmentContent(attachmentContent);
        return attachment;
    }
}
