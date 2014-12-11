/*
 * Copyright 2014 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ethz.bsse.cisd.dsu.dss.systemtests;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.GenericDropboxSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * @author pkupczyk
 */
public class DemoDropboxTest extends GenericDropboxSystemTest
{

    @Override
    protected String getDropboxName()
    {
        return "demo-dropbox";
    }

    @Override
    protected String getDropboxIncomingDirectoryName()
    {
        return "incoming-demo-dropbox";
    }

    @Test
    public void testDropbox1() throws Exception
    {
        importData("demo-data");
        waitUntilDataImported();
        waitUntilDataReindexed(ExperimentPE.class);

        String sessionToken = getGeneralInformationService().tryToAuthenticateForAllServices("test", "password");

        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "DEMO-EXPERIMENT*"));

        List<Experiment> experiments = getGeneralInformationService().searchForExperiments(sessionToken, criteria);
        Assert.assertEquals(experiments.size(), 2);
    }

    @Test(enabled = false)
    public void testDropbox2() throws Exception
    {
        // WARNING: this test fails and shows that transaction is not rolled back after testDropbox1 is executed
        // (the newly created experiments still exist)

        String sessionToken = getGeneralInformationService().tryToAuthenticateForAllServices("test", "password");

        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "DEMO-EXPERIMENT*"));

        List<Experiment> experiments = getGeneralInformationService().searchForExperiments(sessionToken, criteria);
        Assert.assertEquals(experiments.size(), 0);
    }

}