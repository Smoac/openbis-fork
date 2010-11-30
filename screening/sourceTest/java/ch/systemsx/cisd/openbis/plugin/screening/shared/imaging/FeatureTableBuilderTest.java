/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.CodeAndLabelUtil;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.PlateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureValue;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.FeatureTableRow;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.IMetadataProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureVocabularyTermDTO;

/**
 * Tests of {@link FeatureVectorLoader}.
 * 
 * @author Franz-Josef Elmer
 */
public class FeatureTableBuilderTest extends AssertJUnit
{
    private static final int EXPERIMENT_ID = 42;

    private static final String DATA_SET_CODE1 = "ds1";

    private static final String DATA_SET_CODE2 = "ds2";

    private static final String DATA_SET_CODE3 = "ds3";

    private Mockery context;

    private IMetadataProvider service;

    private IImagingReadonlyQueryDAO dao;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IMetadataProvider.class);
        dao = context.mock(IImagingReadonlyQueryDAO.class);
    }

    @AfterMethod
    public final void tearDown()
    {
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testNoFiltering()
    {
        prepareAddFeatureVectors(1, null, "<A>a", "<B>b");
        prepareAddFeatureVectors(2, null, "<B>beta", "c");
        prepareAddFeatureVectors(3, null, "<B>b");

        FeatureVectorLoader builder = createBuilder();
        builder.addFeatureVectorsOfDataSetOrDie(DATA_SET_CODE1);
        builder.addFeatureVectorsOfDataSetOrDie(DATA_SET_CODE2);
        builder.addFeatureVectorsOfDataSetOrDie(DATA_SET_CODE3);
        List<CodeAndLabel> codesAndLabels = builder.getCodesAndLabels();
        List<FeatureTableRow> rows = builder.createFeatureTableRows();

        assertEquals("[<A> a, <B> b, <B> beta, <C> c]", codesAndLabels.toString());
        assertFeatureTableRow(DATA_SET_CODE1, "A1", "db:/s/S1", "1.5, 11.5, NaN, NaN", rows.get(0));
        assertFeatureTableRow(DATA_SET_CODE1, "A2", "db:/s/S1", "0.5, 10.5, NaN, NaN", rows.get(1));
        assertFeatureTableRow(DATA_SET_CODE2, "A1", "db:/s/S2", "NaN, NaN, 2.5, 12.5", rows.get(2));
        assertFeatureTableRow(DATA_SET_CODE2, "A2", "db:/s/S2", "NaN, NaN, 1.5, 11.5", rows.get(3));
        assertFeatureTableRow(DATA_SET_CODE3, "A1", "db:/s/S3", "NaN, 3.5, NaN, NaN", rows.get(4));
        assertFeatureTableRow(DATA_SET_CODE3, "A2", "db:/s/S3", "NaN, 2.5, NaN, NaN", rows.get(5));
        assertEquals(6, rows.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testFiltering()
    {
        prepareAddFeatureVectors(1, "B", "<A>a", "b");
        prepareAddFeatureVectors(2, "B", "<B>beta", "c");
        prepareAddFeatureVectors(3, "B", "b");

        FeatureVectorLoader builder = createBuilder("B");
        builder.addFeatureVectorsOfDataSetOrDie(DATA_SET_CODE1);
        builder.addFeatureVectorsOfDataSetOrDie(DATA_SET_CODE2);
        builder.addFeatureVectorsOfDataSetOrDie(DATA_SET_CODE3);
        List<CodeAndLabel> codesAndLabels = builder.getCodesAndLabels();
        List<FeatureTableRow> rows = builder.createFeatureTableRows();

        assertEquals("[<B> b, <B> beta]", codesAndLabels.toString());
        assertFeatureTableRow(DATA_SET_CODE1, "A1", "db:/s/S1", "11.5, NaN", rows.get(0));
        assertFeatureTableRow(DATA_SET_CODE1, "A2", "db:/s/S1", "10.5, NaN", rows.get(1));
        assertFeatureTableRow(DATA_SET_CODE2, "A1", "db:/s/S2", "NaN, 2.5", rows.get(2));
        assertFeatureTableRow(DATA_SET_CODE2, "A2", "db:/s/S2", "NaN, 1.5", rows.get(3));
        assertFeatureTableRow(DATA_SET_CODE3, "A1", "db:/s/S3", "3.5, NaN", rows.get(4));
        assertFeatureTableRow(DATA_SET_CODE3, "A2", "db:/s/S3", "2.5, NaN", rows.get(5));
        assertEquals(6, rows.size());
        context.assertIsSatisfied();
    }

    private void prepareAddFeatureVectors(final int dataSetID, final String filteredCodeOrNull,
            final String... featureCodesAndLabels)
    {
        context.checking(new Expectations()
            {
                {
                    String dataSetCode = "ds" + dataSetID;
                    one(dao).tryGetDatasetByPermId(dataSetCode);
                    int containerId = dataSetID + 100;
                    ImgDatasetDTO dataSet =
                            new ImgDatasetDTO(dataSetCode, null, null, containerId, false);
                    dataSet.setId(dataSetID);
                    will(returnValue(dataSet));

                    List<ImgFeatureDefDTO> defs = new ArrayList<ImgFeatureDefDTO>();
                    Geometry geometry = Geometry.createFromCartesianDimensions(2, 1);
                    for (int i = 0; i < featureCodesAndLabels.length; i++)
                    {
                        String codeAndLabels = featureCodesAndLabels[i];
                        CodeAndLabel codeAndTitle = CodeAndLabelUtil.create(codeAndLabels);
                        String title = codeAndTitle.getLabel();
                        String code = codeAndTitle.getCode();
                        if (filteredCodeOrNull == null || filteredCodeOrNull.equals(code))
                        {
                            ImgFeatureDefDTO def =
                                    new ImgFeatureDefDTO(title, code, title, dataSetID);
                            def.setId(2 * dataSetID);
                            defs.add(def);
                            one(dao).getFeatureValues(def);
                            PlateFeatureValues values = new PlateFeatureValues(geometry);
                            values.setForWellLocation(dataSetID + 10 * i + 0.5f, 1, 1);
                            values.setForWellLocation(dataSetID + 10 * i - 0.5f, 1, 2);
                            will(returnValue(Arrays.asList(new ImgFeatureValuesDTO(0.0, 0.0,
                                    values, def.getId()))));
                        }
                    }
                    one(dao).listFeatureDefsByDataSetId(dataSetID);
                    will(returnValue(defs));

                    one(dao).listFeatureVocabularyTermsByDataSetId(dataSetID);
                    will(returnValue(new ArrayList<ImgFeatureVocabularyTermDTO>()));

                    one(dao).getContainerById(containerId);
                    String samplePermID = "s" + containerId;
                    will(returnValue(new ImgContainerDTO(samplePermID, geometry.getNumberOfRows(),
                            geometry.getNumberOfColumns(), EXPERIMENT_ID)));

                    one(service).tryGetSampleIdentifier(samplePermID);
                    will(returnValue(new SampleIdentifier(new SpaceIdentifier("db", "s"), "S"
                            + dataSetID)));
                }
            });
    }

    private void assertFeatureTableRow(String expectedDataSetCode, String expectedWell,
            String expectedPlate, String expectedValues, FeatureTableRow row)
    {
        assertEquals(expectedDataSetCode, row.getDataSetCode());
        String rowLetter =
                PlateUtils.translateRowNumberIntoLetterCode(row.getWellLocation().getRow());
        assertEquals(expectedWell, rowLetter + row.getWellLocation().getColumn());
        assertEquals(expectedPlate, row.getPlateIdentifier().toString());
        assertEquals(expectedValues, render(row.getFeatureValues()));
    }

    private String render(FeatureValue[] values)
    {
        StringBuilder builder = new StringBuilder();
        for (FeatureValue value : values)
        {
            if (builder.length() > 0)
            {
                builder.append(", ");
            }
            builder.append(value);
        }
        return builder.toString();
    }

    private FeatureVectorLoader createBuilder(String... featureCodes)
    {
        return new FeatureVectorLoader(Arrays.asList(featureCodes), dao, service);
    }

}
