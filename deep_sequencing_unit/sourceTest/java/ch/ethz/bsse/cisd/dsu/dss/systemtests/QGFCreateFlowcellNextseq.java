package ch.ethz.bsse.cisd.dsu.dss.systemtests;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

public class QGFCreateFlowcellNextseq extends DSUDropboxSystemTest
{

    @Override
    protected String getDropboxName()
    {
        return "create-flowcell-nextseq";
    }

    @Test
    public void testDropbox() throws Exception
    {
        importDataWithMarker("141212_NS500318_0033_AH16YMBGXX");
        waitUntilDataImported();
        waitUntilDataReindexed(SamplePE.class);

        String sessionToken = getGeneralInformationService().tryToAuthenticateForAllServices("kohleman", "password");

        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "H16YMBGXX"));

        List<Sample> samples = getGeneralInformationService().searchForSamples(sessionToken, criteria);
        Assert.assertEquals(samples.size(), 1);
        Sample flowCell = samples.get(0);
        Assert.assertEquals(flowCell.getProperties().get("ILLUMINA_PIPELINE_VERSION"), "2.1.3");
        
    }


}
