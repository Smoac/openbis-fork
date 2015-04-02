package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.attachment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.IPersonTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.attachment.AttachmentFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

@Component
public class AttachmentTranslator extends AbstractCachingTranslator<AttachmentPE, Attachment, AttachmentFetchOptions> implements
        IAttachmentTranslator
{

    @Autowired
    private IPersonTranslator personTranslator;

    @Override
    protected Attachment createObject(TranslationContext context, AttachmentPE attachment, AttachmentFetchOptions fetchOptions)
    {
        Attachment result = new Attachment();

        result.setDescription(attachment.getDescription());
        result.setTitle(attachment.getTitle());
        result.setFileName(attachment.getFileName());
        result.setRegistrationDate(attachment.getRegistrationDate());
        result.setVersion(attachment.getVersion());

        String baseIndexURL = context.getSession().getBaseIndexURL();
        result.setPermlink(createPermlink(attachment, baseIndexURL, false));
        result.setLatestVersionPermlink(createPermlink(attachment, baseIndexURL, true));

        return result;
    }

    private static String createPermlink(AttachmentPE attachment, String baseIndexURL,
            boolean latestVersionPermlink)
    {
        final AttachmentHolderPE holder = attachment.getParent();
        final String fileName = attachment.getFileName();
        final Integer versionOrNull = latestVersionPermlink ? null : attachment.getVersion();
        if (holder.getAttachmentHolderKind() == AttachmentHolderKind.PROJECT)
        {
            ProjectPE project = (ProjectPE) holder;
            return PermlinkUtilities.createProjectAttachmentPermlinkURL(baseIndexURL, fileName,
                    versionOrNull, project.getCode(), project.getSpace().getCode());
        } else
        {
            return PermlinkUtilities.createAttachmentPermlinkURL(baseIndexURL, fileName,
                    versionOrNull, holder.getAttachmentHolderKind(), holder.getPermId());
        }
    }

    @Override
    protected void updateObject(TranslationContext context, AttachmentPE attachment, Attachment result, Relations relations,
            AttachmentFetchOptions fetchOptions)
    {
        if (fetchOptions.hasRegistrator())
        {
            Person registrator = personTranslator.translate(context, attachment.getRegistrator(), fetchOptions.withRegistrator());
            result.setRegistrator(registrator);
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasContent())
        {
            result.setContent(attachment.getAttachmentContent().getValue());
        }
    }

    @Override
    public List<Attachment> translate(TranslationContext translationContext, AttachmentHolderPE attachmentHolder,
            AttachmentFetchOptions attachmentFetchOptions)
    {
        List<List<AttachmentPE>> attachmentsIntoVersionGroups =
                splitAttachmentsIntoVersionGroups(attachmentHolder.getAttachments());
        ArrayList<Attachment> attachments = new ArrayList<Attachment>();
        for (List<AttachmentPE> attachmentGroup : attachmentsIntoVersionGroups)
        {
            attachments.add(translate(translationContext, attachmentGroup, attachmentFetchOptions));
        }
        return attachments;
    }

    private Attachment translate(TranslationContext translationContext, List<AttachmentPE> group, AttachmentFetchOptions fetchOptions)
    {
        assert group.isEmpty() == false;

        Attachment attachment = translate(translationContext, group.get(0), fetchOptions);

        if (fetchOptions.hasPreviousVersion() && group.size() > 0)
        {
            Attachment previousVersion = translate(translationContext, group.subList(1, group.size()), fetchOptions.withPreviousVersion());
            attachment.setPreviousVersion(previousVersion);
        }

        attachment.setFetchOptions(fetchOptions);

        return attachment;
    }

    public static List<List<AttachmentPE>> splitAttachmentsIntoVersionGroups(Collection<AttachmentPE> attachments)
    {
        HashMap<String, List<AttachmentPE>> result = new HashMap<String, List<AttachmentPE>>();

        for (AttachmentPE attachment : attachments)
        {
            String fileName = attachment.getFileName();
            if (false == result.containsKey(fileName))
            {
                result.put(fileName, new LinkedList<AttachmentPE>());
            }
            result.get(fileName).add(attachment);
        }

        List<List<AttachmentPE>> sortedResults = new LinkedList<List<AttachmentPE>>();

        for (List<AttachmentPE> list : result.values())
        {
            Collections.sort(list, new Comparator<AttachmentPE>()
                {
                    @Override
                    public int compare(AttachmentPE o1, AttachmentPE o2)
                    {
                        return o2.getVersion() - o1.getVersion();
                    }
                });
            sortedResults.add(list);
        }
        return sortedResults;
    }
}
