package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.ExternalDmsSearchCriteria;

public class SearchLinkDataSetTest extends AbstractLinkDataSetTest
{

    @Test
    void searchWithLegacyExternalCode()
    {
        DataSetSearchCriteria dc = new DataSetSearchCriteria();
        dc.withLinkedData().withExternalCode().thatEquals("CODE3");

        SearchResult<DataSet> result = searchWith(dc);

        assertThat(result.getTotalCount(), is(1));
        assertThat(result.getObjects().get(0).getCode(), is("20120628092259000-25"));
    }

    @Test
    void searchWithLegacyExternalDms()
    {
        DataSetSearchCriteria dc = new DataSetSearchCriteria().withAndOperator();
        ExternalDmsSearchCriteria externalDms = dc.withLinkedData().withExternalDms();
        externalDms.withCode().thatEquals("DMS_2");
        externalDms.withLabel().thatEquals("\"Test External openBIS instance\"");

        SearchResult<DataSet> result = searchWith(dc);

        assertThat(result.getTotalCount(), is(1));
        assertThat(result.getObjects().get(0).getCode(), is("20120628092259000-25"));
    }

    @Test
    void searchWithExternalDmsOfCopy()
    {
        DataSetSearchCriteria dc = new DataSetSearchCriteria().withAndOperator();
        ExternalDmsSearchCriteria externalDms = dc.withLinkedData().withCopy().withExternalDms();
        externalDms.withCode().thatEquals("DMS_2");
        externalDms.withLabel().thatEquals("\"Test External openBIS instance\"");

        SearchResult<DataSet> result = searchWith(dc);

        assertThat(result.getTotalCount(), is(1));
        assertThat(result.getObjects().get(0).getCode(), is("20120628092259000-25"));
    }

    @Test
    void searchWithExternalCodeOfCopy()
    {
        DataSetSearchCriteria dc = new DataSetSearchCriteria();
        dc.withLinkedData().withCopy().withExternalCode().thatEquals("CODE3");

        SearchResult<DataSet> result = searchWith(dc);

        assertThat(result.getTotalCount(), is(1));
        assertThat(result.getObjects().get(0).getCode(), is("20120628092259000-25"));
    }

    @Test
    void searchWithPathOfCopy()
    {
        DataSetSearchCriteria dc = new DataSetSearchCriteria();
        dc.withLinkedData().withCopy().withPath().thatEquals("/path/to/20120628092259000-41");

        SearchResult<DataSet> result = searchWith(dc);

        assertThat(result.getTotalCount(), is(1));
        assertThat(result.getObjects().get(0).getCode(), is("20120628092259000-41"));
    }

    @Test
    void searchWithCommitHashOfCopy()
    {
        DataSetSearchCriteria dc = new DataSetSearchCriteria();
        dc.withLinkedData().withCopy().withGitCommitHash().thatEquals("abcdefg12345678");

        SearchResult<DataSet> result = searchWith(dc);

        assertThat(result.getTotalCount(), is(1));
        assertThat(result.getObjects().get(0).getCode(), is("20120628092259000-41"));
    }

    private SearchResult<DataSet> searchWith(DataSetSearchCriteria criteria)
    {
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        return v3api.searchDataSets(session, criteria, fetchOptions);
    }
}
