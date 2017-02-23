package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ContentCopy;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.ContentCopyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDmsAddressType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

public class CreateLinkDataSetTest extends AbstractLinkDataSetTest
{

    @Test
    void copiesInOpenBISWork()
    {
        ExternalDmsPermId openbis = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));
        ContentCopyCreation creation = copyAt(openbis).build();
        DataSetPermId id = create(linkDataSet().with(creation));
        List<ContentCopy> contentCopies = get(id).getLinkedData().getContentCopies();

        assertThat(contentCopies.size(), is(1));

        ContentCopy copy = contentCopies.get(0);
        assertThat(copy.getExternalCode(), is(creation.getExternalId()));
        assertThat(copy.getPath(), is(creation.getPath()));
        assertThat(copy.getGitCommitHash(), is(creation.getGitCommitHash()));
        assertThat(copy.getExternalDms(), isSimilarTo(get(openbis)));
    }

    // this goes to GetLinkDataSetTest, look for problems in legacy fields
    // @Test
    // void copiesInOpenBISWork()
    // {
    // ExternalDmsPermId openbis = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));
    // DataSetPermId id = create(linkDataSet().with(copyAt(openbis)));
    // DataSet dataSet = get(id);
    // assertThat(dataSet.getLinkedData().getExternalDms(), isSimilarTo(get(openbis)));
    // }

    @DataProvider(name = "InvalidLocationCombinations")
    public static Object[][] invalidLocationCombinations()
    {
        return new Object[][] {
                { ExternalDmsAddressType.OPENBIS, null, null, null },
                { ExternalDmsAddressType.OPENBIS, null, "/path", null },
                { ExternalDmsAddressType.OPENBIS, null, null, "hash" },
                { ExternalDmsAddressType.OPENBIS, null, "/path", "hash" },
                { ExternalDmsAddressType.OPENBIS, "code", "/path", null },
                { ExternalDmsAddressType.OPENBIS, "code", null, "hash" },
                { ExternalDmsAddressType.OPENBIS, "code", "/path", "hash" },

                { ExternalDmsAddressType.URL, null, null, null },
                { ExternalDmsAddressType.URL, null, "/path", null },
                { ExternalDmsAddressType.URL, null, null, "hash" },
                { ExternalDmsAddressType.URL, null, "/path", "hash" },
                { ExternalDmsAddressType.URL, "code", "/path", null },
                { ExternalDmsAddressType.URL, "code", null, "hash" },
                { ExternalDmsAddressType.URL, "code", "/path", "hash" },

                { ExternalDmsAddressType.FILE_SYSTEM, null, null, null },
                { ExternalDmsAddressType.FILE_SYSTEM, "code", null, null },
                { ExternalDmsAddressType.FILE_SYSTEM, "code", "/path", "hash" },
                { ExternalDmsAddressType.FILE_SYSTEM, "code", null, "hash" },
                { ExternalDmsAddressType.FILE_SYSTEM, null, null, "hash" }
        };
    }

    @Test(dataProvider = "InvalidLocationCombinations", expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Invalid arguments.*")
    void cannotLinkToExternalDmsOfWrongType(ExternalDmsAddressType dmsType, String externalCode, String path, String gitCommitHash)
    {
        ExternalDmsPermId edms = create(externalDms().withType(dmsType));
        create(linkDataSet().with(copyAt(edms)
                .withExternalCode(externalCode)
                .withPath(path)
                .withGitCommitHash(gitCommitHash)));
    }

}
