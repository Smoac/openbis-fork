/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.query;

import java.util.Arrays;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jmock.Expectations;
import org.jmock.Mockery;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.DataSetInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TestReportNodeDialog
{
    private static final class Dialog extends ReportNodeDialog
    {
        private final IQueryApiFacade queryApiFacade;

        Dialog()
        {
            Mockery context = new Mockery();
            final IQueryApiFacade facade = context.mock(IQueryApiFacade.class);
            final IGeneralInformationService service =
                    context.mock(IGeneralInformationService.class);
            context.checking(new Expectations()
                {
                    {
                        allowing(facade).getGeneralInformationService();
                        will(returnValue(service));
                        
                        allowing(facade).getSessionToken();
                        will(returnValue("session-42"));
                        
                        allowing(facade).listTableReportDescriptions();
                        ReportDescription reportDescription = new ReportDescription();
                        reportDescription.setLabel("report");
                        reportDescription.setDataSetTypes(Arrays.asList("ABC", "FGH"));
                        will(returnValue(Arrays.asList(reportDescription)));

                        allowing(service).searchForDataSets(with(any(String.class)),
                                with(any(SearchCriteria.class)));
                        will(returnValue(Arrays.asList(create("12345-678", "ABC"),
                                create("6753-987", "DEF"), create("456753-475", "FGH"))));
                    }

                    private DataSet create(String code, String type)
                    {
                        DataSetInitializer initializer = new DataSetInitializer();
                        initializer.setCode(code);
                        initializer.setDataSetTypeCode(type);
                        initializer.setExperimentIdentifier("/S/P/EXP1");
                        EntityRegistrationDetails.EntityRegistrationDetailsInitializer detailsInitializer =
                                new EntityRegistrationDetails.EntityRegistrationDetailsInitializer();
                        detailsInitializer.setUserId("test");
                        detailsInitializer.setRegistrationDate(new Date());
                        initializer.setRegistrationDetails(new EntityRegistrationDetails(
                                detailsInitializer));
                        return new DataSet(initializer);
                    }
                });
            queryApiFacade = facade;
        }

        @Override
        protected IQueryApiFacade createFacade()
        {
            return queryApiFacade;
        }
        
    }

    public static void main(String[] args)
    {
        
        JFrame frame = new JFrame("test");
        JPanel panel = new JPanel();
        frame.getContentPane().add(panel);
        panel.add(new Dialog().getPanel());
        frame.setSize(600, 600);
        frame.setVisible(true);
    }
    

}
