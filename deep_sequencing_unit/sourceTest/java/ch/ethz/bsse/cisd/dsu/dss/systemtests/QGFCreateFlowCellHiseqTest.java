package ch.ethz.bsse.cisd.dsu.dss.systemtests;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

public class QGFCreateFlowCellHiseqTest extends DSUDropboxSystemTest
{

    @Override
    protected String getDropboxName()
    {
        return "create-flowcell-hiseq";
    }

    protected String login(String userID, String userPassword)
    {
        return getGeneralInformationService().tryToAuthenticateForAllServices(userID, userPassword);
    }

    @Test
    public void testQGFCreateFlowCellHiseq() throws Exception
    {
        importDataWithMarker("141212_D00535_0038_BC6A12ANXX");
        waitUntilDataImported();
        waitUntilDataReindexed(SamplePE.class);

        String sessionToken = login("kohleman", "password");

        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "141212_D00535_0038_BC6A12ANXX"));

        List<Sample> samples = getGeneralInformationService().searchForSamples(sessionToken, criteria);
        Assert.assertEquals(samples.size(), 1);
        Sample flowCell = samples.get(0);
        Assert.assertEquals(flowCell.getProperties().get("ILLUMINA_PIPELINE_VERSION"), "1.18.61");
        Assert.assertEquals(flowCell.getProperties().get("FLOWCELLTYPE"), "HiSeq Flow Cell v4");

    }

    @Test
    public void testQGFCreateFlowCellHiSeqNonmatchingFolderName() throws Exception
    {

        String folderName = "141212_D00535_0038_BC6A12ANXX_NonmatchingFolderName";

        importDataWithMarker(folderName);
        waitUntilDataImportFails("Flowcell names do not match between directory name " + folderName
                + " and RunInfo.xml property file: 141212_D00535_0038_BC6A12ANXX");
    }
    
     @Test
     public void testQGFCreateFlowCellHiSeqMissingFile() throws Exception{
    
         String folderName = "141212_D00535_0037_AC5UMNANXX";
         String missingFile = "runParameters.xml";
         
         importDataWithMarker(folderName);
         waitUntilDataImportFails("File not found: " + missingFile);
     }
    
     @Test
     public void testQGFCreateFlowCellHiSeqAlreadyExisting() throws Exception {

         String folderName = "141212_D00535_0038_BC6A12ANXX";
         importDataWithMarker(folderName);
         waitUntilDataImported();
         waitUntilDataReindexed(SamplePE.class);

         importDataWithMarker(folderName);
         waitUntilDataImported();
         waitUntilDataReindexed(SamplePE.class);

         String sessionToken = login("kohleman", "password");

         SearchCriteria criteria = new SearchCriteria();
         criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, folderName));

         List<Sample> samples = getGeneralInformationService().searchForSamples(sessionToken, criteria);
         Assert.assertEquals(samples.size(), 1);
     }

}
