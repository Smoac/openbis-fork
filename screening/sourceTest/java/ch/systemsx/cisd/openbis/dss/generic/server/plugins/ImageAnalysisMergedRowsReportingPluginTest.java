/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.mdarray.MDDoubleArray;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;

/**
 * Test cases for the {@link ImageAnalysisMergedRowsReportingPlugin}.
 * 
 * @author Tomasz Pylak
 */
public class ImageAnalysisMergedRowsReportingPluginTest extends AssertJUnit
{
    private Mockery context;
    private IEncapsulatedOpenBISService service;
    private IImagingQueryDAO dao;
    private ImageAnalysisMergedRowsReportingPlugin plugin;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        dao = context.mock(IImagingQueryDAO.class);
        plugin = new ImageAnalysisMergedRowsReportingPlugin(new Properties(), new File("."), service, dao);
    }

    @AfterMethod
    public final void tearDown()
    {
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }
    
    @Test
    public final void test()
    {
        final DatasetDescription ds1 = new DatasetDescription();
        ds1.setDatasetCode("ds1");
        final ImgContainerDTO p1 = new ImgContainerDTO("p1", 3, 2, 0);
        final SampleIdentifier p1Identifier = new SampleIdentifier(new SpaceIdentifier("1", "S"), "P1");
        final DatasetDescription ds2 = new DatasetDescription();
        ds2.setDatasetCode("ds2");
        final ImgContainerDTO p2 = new ImgContainerDTO("p2", 2, 1, 0);
        final SampleIdentifier p2Identifier = new SampleIdentifier(new SpaceIdentifier("1", "S"), "P2");
        final ImgFeatureDefDTO ds1f1 = new ImgFeatureDefDTO("f1", "", 1);
        final ImgFeatureDefDTO ds1f2 = new ImgFeatureDefDTO("f2", "", 1);
        final ImgFeatureDefDTO ds2f2 = new ImgFeatureDefDTO("f2", "", 2);
        final ImgFeatureDefDTO ds2f3 = new ImgFeatureDefDTO("f3", "", 2);
        final ImgFeatureValuesDTO ds1f1Values = createFeatureValues("12, 2.5", "24, 3.25", "-1.5, 42");
        final ImgFeatureValuesDTO ds1f2Values = createFeatureValues("-3.5, 12.5", "-2, 1", "5, 4.25");
        final ImgFeatureValuesDTO ds2f2Values = createFeatureValues("23", "5.75");
        final ImgFeatureValuesDTO ds2f3Values = createFeatureValues("-9", "44.125");
        context.checking(new Expectations()
            {
                {
                    one(dao).tryGetDatasetByPermId(ds1.getDatasetCode());
                    will(returnValue(createDataSet(1)));
                    
                    one(dao).listFeatureDefsByDataSetId(1);
                    will(returnValue(Arrays.asList(ds1f1, ds1f2)));
                    
                    one(dao).getContainerById(101);
                    will(returnValue(p1));
                    
                    one(service).tryToGetSampleIdentifier(p1.getPermId());
                    will(returnValue(p1Identifier));
                    
                    one(dao).getFeatureValues(ds1f1);
                    will(returnValue(Arrays.asList(ds1f1Values)));
                    
                    one(dao).getFeatureValues(ds1f2);
                    will(returnValue(Arrays.asList(ds1f2Values)));
                    
                    one(dao).tryGetDatasetByPermId(ds2.getDatasetCode());
                    will(returnValue(createDataSet(2)));
                    
                    one(dao).listFeatureDefsByDataSetId(2);
                    will(returnValue(Arrays.asList(ds2f2, ds2f3)));
                    
                    one(dao).getContainerById(102);
                    will(returnValue(p2));
                    
                    one(service).tryToGetSampleIdentifier(p2.getPermId());
                    will(returnValue(p2Identifier));
                    
                    one(dao).getFeatureValues(ds2f2);
                    will(returnValue(Arrays.asList(ds2f2Values)));
                    
                    one(dao).getFeatureValues(ds2f3);
                    will(returnValue(Arrays.asList(ds2f3Values)));
                    
                }
            });
        
        TableModel tableModel = plugin.createReport(Arrays.asList(ds1, ds2));
        
        List<TableModelColumnHeader> headers = tableModel.getHeader();
        assertEquals("[Data Set Code, Plate Identifier, Row, Column, f1, f2, f3]", headers.toString());
        List<TableModelRow> rows = tableModel.getRows();
        String prefix = "[ds1, 1:/S/P1, ";
        assertEquals(prefix + "A, 1, 12.0, -3.5, ]", rows.get(0).getValues().toString());
        assertEquals(prefix + "A, 2, 2.5, 12.5, ]", rows.get(1).getValues().toString());
        assertEquals(prefix + "B, 1, 24.0, -2.0, ]", rows.get(2).getValues().toString());
        assertEquals(prefix + "B, 2, 3.25, 1.0, ]", rows.get(3).getValues().toString());
        assertEquals(prefix + "C, 1, -1.5, 5.0, ]", rows.get(4).getValues().toString());
        assertEquals(prefix + "C, 2, 42.0, 4.25, ]", rows.get(5).getValues().toString());
        prefix = "[ds2, 1:/S/P2, ";
        assertEquals(prefix + "A, 1, , 23.0, -9.0]", rows.get(6).getValues().toString());
        assertEquals(prefix + "B, 1, , 5.75, 44.125]", rows.get(7).getValues().toString());
        assertEquals(8, rows.size());
        context.assertIsSatisfied();
    }
    
    private ImgDatasetDTO createDataSet(long id)
    {
        ImgDatasetDTO datasetDTO = new ImgDatasetDTO("ds" + id, null, null, 100 + id);
        datasetDTO.setId(id);
        return datasetDTO;
    }
    
    private ImgFeatureValuesDTO createFeatureValues(String... rows)
    {
        double[][] matrix = new double[rows.length][];
        for (int i = 0; i < rows.length; i++)
        {
            String row = rows[i];
            String[] cells = row.split(",");
            matrix[i] = new double[cells.length];
            for (int j = 0; j < cells.length; j++)
            {
                matrix[i][j] = Double.parseDouble(cells[j]);
            }
        }
        MDDoubleArray array = new MDDoubleArray(matrix);
        return new ImgFeatureValuesDTO(0.0, 0.0, array, 0L);
    }
}
