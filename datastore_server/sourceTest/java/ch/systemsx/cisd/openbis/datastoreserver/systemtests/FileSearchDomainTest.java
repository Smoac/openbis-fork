package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchResult;

public class FileSearchDomainTest extends GenericSystemTest
{

    @Test
    public void test() throws Exception
    {
        System.out.println("HERE");
        IGeneralInformationService gis = getGeneralInformationService();
        String sessionToken = gis.tryToAuthenticateForAllServices("test", "test");

        List<SearchDomainSearchResult> results = gis.searchOnSearchDomain(sessionToken, "File", "", null);
        
        System.out.println("RESULT: " + results);
        gis.logout(sessionToken);
    }
}