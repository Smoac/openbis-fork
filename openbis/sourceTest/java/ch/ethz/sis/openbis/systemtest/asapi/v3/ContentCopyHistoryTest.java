package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Objects;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ContentCopyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDmsAddressType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetHistoryPE;

public class ContentCopyHistoryTest extends AbstractLinkDataSetTest
{

    @Test
    void currentValuesAreNotVisibleInHistory()
    {
        ExternalDmsPermId edms = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));
        ContentCopyCreationBuilder copy = copyAt(edms);
        DataSetPermId id = create(linkDataSet().with(copy.build()));

        assertThat(historyOf(id).size(), is(0));
    }

    @Test
    void copiesInOpenBISCreateCorrectHistoryEntry()
    {
        ExternalDmsPermId edms = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));
        ContentCopyCreationBuilder copy = copyAt(edms);
        DataSetPermId id = create(linkDataSet().with(copy.build()));

        ContentCopyPermId copyId = get(id).getLinkedData().getContentCopies().get(0).getId();
        update(dataset(id).without(copyId));

        assertThat(historyOf(id), hasItem(representing(copy)));
    }

    @Test
    void copiesInURLCreateCorrectHistoryEntry()
    {
        ExternalDmsPermId edms = create(externalDms().withType(ExternalDmsAddressType.URL));
        ContentCopyCreationBuilder copy = copyAt(edms);
        DataSetPermId id = create(linkDataSet().with(copy.build()));

        ContentCopyPermId copyId = get(id).getLinkedData().getContentCopies().get(0).getId();
        update(dataset(id).without(copyId));

        assertThat(historyOf(id), hasItem(representing(copy)));
    }

    @Test
    void copiesInPlainFileSystemCreateCorrectHistoryEntry()
    {
        ExternalDmsPermId edms = create(externalDms().withType(ExternalDmsAddressType.FILE_SYSTEM));
        ContentCopyCreationBuilder copy = copyAt(edms);
        DataSetPermId id = create(linkDataSet().with(copy.build()));

        ContentCopyPermId copyId = get(id).getLinkedData().getContentCopies().get(0).getId();
        update(dataset(id).without(copyId));

        assertThat(historyOf(id), hasItem(representing(copy)));
    }

    @Test
    void copiesInGitCreateCorrectHistoryEntry()
    {
        ExternalDmsPermId edms = create(externalDms().withType(ExternalDmsAddressType.FILE_SYSTEM));
        ContentCopyCreationBuilder copy = copyAt(edms).withGitCommitHash(uuid());
        DataSetPermId id = create(linkDataSet().with(copy.build()));

        ContentCopyPermId copyId = get(id).getLinkedData().getContentCopies().get(0).getId();
        update(dataset(id).without(copyId));

        assertThat(historyOf(id), hasItem(representing(copy)));
    }

    @Test
    void updatingExternalDmsThroughLegacyMethodDeletesOldCopy()
    {
        ExternalDmsPermId dms1 = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));
        ExternalDmsPermId dms2 = create(externalDms().withType(ExternalDmsAddressType.URL));
        ContentCopyCreationBuilder copy = copyAt(dms1);
        DataSetPermId id = create(linkDataSet().with(copy.build()));

        update(dataset(id).withExternalDms(dms2));

        assertThat(historyOf(id), hasItem(representing(copy)));
    }

    @Test
    void updatingExternalCodeThroughLegacyMethodDeletesOldCopy()
    {
        ExternalDmsPermId dms = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));
        ContentCopyCreationBuilder copy = copyAt(dms);
        DataSetPermId id = create(linkDataSet().with(copy.build()));
        String code = uuid();

        update(dataset(id).withExternalCode(code));

        assertThat(historyOf(id), hasItem(representing(copy)));
    }

    @Test
    void updatingExternalCodeToSameValueThroughLegacyMethodWorks()
    {
        ExternalDmsPermId dms = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));
        ContentCopyCreationBuilder copy = copyAt(dms).withExternalCode("a code");
        DataSetPermId id = create(linkDataSet().with(copy.build()));

        update(dataset(id).withExternalCode("a code"));

        assertThat(historyOf(id), hasItem(representing(copy)));
    }

    private String describe(DataSetHistoryPE entry)
    {
        return new ToStringBuilder(entry)
                .append("external code", entry.getExternalCode())
                .append("path", entry.getPath())
                .append("get commit hash", entry.getGitCommitHash())
                .append("external dms id", entry.getExternalDms().getPermId())
                .append("valid from", entry.getValidFromDate())
                .append("valid until", entry.getValidUntilDate())
                .toString();
    }

    private TypeSafeDiagnosingMatcher<DataSetHistoryPE> representing(final ContentCopyCreationBuilder copy)
    {
        return new TypeSafeDiagnosingMatcher<DataSetHistoryPE>()
            {

                @Override
                public void describeTo(Description desc)
                {
                    String description = new ToStringBuilder(copy)
                            .append("external code", copy.getExternalCode())
                            .append("path", copy.getPath())
                            .append("get commit hash", copy.getGitCommitHash())
                            .append("external dms id", copy.getEdmsId().getPermId())
                            .toString();
                    desc.appendText(description);
                }

                @Override
                protected boolean matchesSafely(DataSetHistoryPE entry, Description desc)
                {
                    desc.appendText(describe(entry));
                    return Objects.equals(copy.getExternalCode(), entry.getExternalCode()) &&
                            Objects.equals(copy.getPath(), entry.getPath()) &&
                            Objects.equals(copy.getGitCommitHash(), entry.getGitCommitHash()) &&
                            Objects.equals(copy.getEdmsId().getPermId(), entry.getExternalDms().getPermId());
                }
            };
    }
}
