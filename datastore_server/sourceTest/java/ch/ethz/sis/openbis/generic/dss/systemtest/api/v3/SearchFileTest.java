package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.dss.api.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.entity.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dss.api.v3.dto.search.FileSearchCriterion;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.SystemTestCase;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;

public class SearchFileTest extends SystemTestCase
{

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-simple-dataset-test");
    }

    @Override
    protected int dataSetImportWaitDurationInSeconds()
    {
        return 280;
    }

    @Test
    public void allFilesOfGivenDatasetsAreReturned() throws Exception
    {
        IDataStoreServerApi dss = (IDataStoreServerApi) ServiceProvider.getDssServiceV3().getService();

        String dataSetCode = UUID.randomUUID().toString().toUpperCase();

        File dataSetDir = new File(workingDirectory, dataSetCode);

        Set<String> directories = new HashSet<>(Arrays.asList("subdir1", "subdir1/subdir2", "subdir3"));
        createDirectories(dataSetDir, directories);

        Set<String> files =
                new HashSet<>(Arrays.asList("file1.txt", "file2.txt", "subdir1/file3.txt", "subdir1/file4.txt", "subdir1/subdir2/file5.txt",
                        "subdir3/file6.txt"));
        createFiles(dataSetDir, files);

        moveFileToIncoming(dataSetDir);
        waitUntilDataSetImported();
        waitUntilIndexUpdaterIsIdle();
        waitUntilDataSetPostRegistrationCompleted(dataSetCode);

        IGeneralInformationService generalInformationService = ServiceProvider.getGeneralInformationService();
        String sessionToken = generalInformationService.tryToAuthenticateForAllServices("test", "test");

        FileSearchCriterion sc = new FileSearchCriterion();
        sc.withDataSet().withPermId().thatEquals(dataSetCode);

        List<DataSetFile> searchFiles = dss.searchFiles(sessionToken, sc);

        files.addAll(directories);
        assertThat(searchFiles, containsAll(files));
        /* directory structure [dataset id]/original/[root folder] */
        assertThat(searchFiles.size(), is(files.size() + 3));
    }

    private void createDirectories(File dataSetDir, Set<String> directories)
    {
        for (String dir : directories)
        {
            new File(dataSetDir, dir).mkdirs();
        }
    }

    private void createFiles(File dataSetDir, Collection<String> paths)
    {
        for (String path : paths)
        {
            File f = new File(dataSetDir, path);
            FileUtilities.writeToFile(f, "file content of " + f.getPath());
        }
    }

    public static Matcher<Collection<DataSetFile>> containsAll(Collection<String> files)
    {
        return new CollectionContainsFilesMatcher(files);
    }

    public static class CollectionContainsFilesMatcher extends TypeSafeMatcher<Collection<DataSetFile>>
    {

        private final Collection<String> files;

        public CollectionContainsFilesMatcher(Collection<String> files)
        {
            this.files = files;
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("A collection containing at least these elements:");
            for (String file : files)
            {
                description.appendText(file + " ");
            }
        }

        @Override
        protected boolean matchesSafely(Collection<DataSetFile> elements)
        {
            for (String file : files)
            {
                if (fileInElements(file, elements) == false)
                {
                    return false;
                }
            }
            return true;
        }

        private boolean fileInElements(String file, Collection<DataSetFile> elements)
        {
            for (DataSetFile dsf : elements)
            {
                if (dsf.getFileName().endsWith(file))
                {
                    return true;
                }
            }
            return false;
        }
    }

}