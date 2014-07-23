package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.attachment;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;

public class AttachmentTranslatorTest extends AssertJUnit
{
    @Test
    public void testSplitAttachmentsIntoVersionGroupsForEmptyCollection()
    {
        List<List<AttachmentPE>> result = AttachmentTranslator.splitAttachmentsIntoVersionGroups(
                Arrays.<AttachmentPE> asList());

        assertEquals(0, result.size());
    }

    @Test
    public void testSplitAttachmentsIntoVersionGroups()
    {
        List<List<AttachmentPE>> result = AttachmentTranslator.splitAttachmentsIntoVersionGroups(
                Arrays.asList(attachment("abc", 2), attachment("cde", 1),
                        attachment("abc", 4)));

        Collections.sort(result, new Comparator<List<AttachmentPE>>()
            {
                @Override
                public int compare(List<AttachmentPE> l1, List<AttachmentPE> l2)
                {
                    return l1.size() - l2.size();
                }
            });
        assertEquals("[AttachmentPE{fileName=cde,version=1,parent=<null>}]",
                result.get(0).toString());
        assertEquals("[AttachmentPE{fileName=abc,version=4,parent=<null>}, " +
                "AttachmentPE{fileName=abc,version=2,parent=<null>}]",
                result.get(1).toString());
        assertEquals(2, result.size());
    }

    private AttachmentPE attachment(String fileName, int version)
    {
        AttachmentPE attachmentPE = new AttachmentPE();
        attachmentPE.setFileName(fileName);
        attachmentPE.setVersion(version);
        return attachmentPE;
    }

}
