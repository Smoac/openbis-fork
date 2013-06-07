/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.knime.core.node.NodeLogger;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.generic.shared.util.IRowBuilder;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.knime.server.Constants;
import ch.systemsx.cisd.openbis.knime.server.FieldType;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.translator.QueryTableModelTranslator;

/**
 * @author Franz-Josef Elmer
 */
public class UtilTest extends AssertJUnit
{
    private BufferedAppender logRecorder;
    
    private Mockery context;

    private IQueryApiFacade facade;

    private NodeLogger logger;

    private AggregatedDataImportDescription description;

    @BeforeMethod
    public void beforeMethod()
    {
        LogInitializer.init();
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
        context = new Mockery();
        facade = context.mock(IQueryApiFacade.class);
        logger = NodeLogger.getLogger("test");
        AggregationServiceDescription desc = new AggregationServiceDescription();
        desc.setServiceKey(AggregatedDataImportDescription.PREFIX + "as");
        ArrayList<AggregatedDataImportDescription> descriptions = new ArrayList<AggregatedDataImportDescription>();
        AggregatedDataImportDescription.addDescriptionIfDataTable(descriptions, desc);
        description = descriptions.get(0);
    }

    @AfterMethod
    public void afterMethod()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testGetFieldDescriptionsWithEmptyTable()
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        prepareCreateReport(builder);

        assertGetFieldDescriptionsFailed("0 columns instead of 2");

        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetFieldDescriptionsWithOneColumn()
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        builder.addHeader(Constants.PARAMETER_DESCRIPTION_NAME_COLUMN);
        prepareCreateReport(builder);
        
        assertGetFieldDescriptionsFailed("1 columns instead of 2");
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetFieldDescriptionsWithWrongColumns()
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        builder.addHeader(Constants.PARAMETER_DESCRIPTION_NAME_COLUMN);
        builder.addHeader("blabla");
        prepareCreateReport(builder);
        
        assertGetFieldDescriptionsFailed("2. column is 'blabla' instead of 'type'.");
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetFieldDescriptionsWithUnspecifiedName()
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addHeader(Constants.PARAMETER_DESCRIPTION_NAME_COLUMN);
        builder.addHeader(Constants.PARAMETER_DESCRIPTION_TYPE_COLUMN);
        builder.addRow();
        prepareCreateReport(builder);
        
        assertGetFieldDescriptionsFailed("Unspecified parameter name.");
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetFieldDescriptionsWithUnspecifiedType()
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addHeader(Constants.PARAMETER_DESCRIPTION_NAME_COLUMN);
        builder.addHeader(Constants.PARAMETER_DESCRIPTION_TYPE_COLUMN);
        builder.addRow().setCell(Constants.PARAMETER_DESCRIPTION_NAME_COLUMN, "Description");
        IRowBuilder row = builder.addRow();
        row.setCell(Constants.PARAMETER_DESCRIPTION_NAME_COLUMN, "Greetings");
        row.setCell(Constants.PARAMETER_DESCRIPTION_TYPE_COLUMN, FieldType.VOCABULARY + ":Hi, Hello");
        prepareCreateReport(builder);
        
        List<FieldDescription> fieldDescriptions = Util.getFieldDescriptions(facade, description, logger);
        
        assertEquals("[Description:VARCHAR, Greetings:VOCABULARY[Hi, Hello]]", fieldDescriptions.toString());
        assertEquals("WARN  test - Unknown field type '' using VARCHAR instead.", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }
    
    private void assertGetFieldDescriptionsFailed(String expectedMessage)
    {
        try
        {
            Util.getFieldDescriptions(facade, description, logger);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ex)
        {
            String serviceKey = description.getAggregationServiceDescription().getServiceKey();
            assertEquals("Invalid response of aggregation service '" + serviceKey + "' when invoked with parameter "
                    + Constants.REQUEST_KEY + " = " + Constants.GET_PARAMETER_DESCRIPTIONS_REQUEST + ":\n" +
                    expectedMessage, ex.getMessage());
        }
    }
    

    private void prepareCreateReport(SimpleTableModelBuilder builder)
    {
        final QueryTableModel tableModel = new QueryTableModelTranslator(builder.getTableModel()).translate();
        context.checking(new Expectations()
            {
                {
                    Map<String, Object> parameters = new HashMap<String, Object>();
                    parameters.put(Constants.REQUEST_KEY, Constants.GET_PARAMETER_DESCRIPTIONS_REQUEST);

                    one(facade).createReportFromAggregationService(description.getAggregationServiceDescription(), parameters);
                    will(returnValue(tableModel));
                }
            });
    }

}
