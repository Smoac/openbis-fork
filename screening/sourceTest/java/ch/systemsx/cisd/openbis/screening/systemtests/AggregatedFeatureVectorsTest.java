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

package ch.systemsx.cisd.openbis.screening.systemtests;

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.FeatureVectorSummaryGridColumnIDs.EXPERIMENT_PERM_ID;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.FeatureVectorSummaryGridColumnIDs.MATERIAL_ID;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.AnalysisSettings;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaFeatureSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaFeatureSummaryResult;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialFeaturesOneExpCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.FeatureVectorSummaryGridColumnIDs;

/**
 * @author Kaloyan Enimanev
 */
@Test(groups =
    { "slow", "systemtest" })
public class AggregatedFeatureVectorsTest extends AbstractScreeningSystemTestCase
{
    private static final DefaultResultSetConfig<String, TableModelRowWithObject<MaterialFeatureVectorSummary>> RESULT_SET_CONFIG =
            new DefaultResultSetConfig<String, TableModelRowWithObject<MaterialFeatureVectorSummary>>();

    @BeforeTest
    public void dropAnExampleDataSet() throws IOException, Exception
    {
        File exampleDataSet = createExampleIncoming();
        moveFileToIncoming(exampleDataSet);
        waitUntilDataSetImported();
    }
    
    @AfterMethod
    public void tearDown()
    {
        analysisSettingServer.setAnalysisSettings(new AnalysisSettings(new Properties()));
        File[] files = getIncomingDirectory().listFiles();
        for (File file : files)
        {
            FileUtilities.deleteRecursively(file);
        }
    }

    @Test
    public void testGetMaterialFeatureVectorSummary() throws Exception
    {
        Material geneG = commonServer.getMaterialInfo(sessionToken, new MaterialIdentifier("G", "GENE"));
        Experiment experiment =
                commonServer.getExperimentInfo(sessionToken, ExperimentIdentifierFactory
                        .parse("/TEST/TEST-PROJECT/AGGREGATED_FEATURES_EXP"));

        MaterialReplicaFeatureSummaryResult summaryResult =
                screeningServer.getMaterialFeatureVectorSummary(
                sessionToken,
                new MaterialFeaturesOneExpCriteria(new TechId(geneG), AnalysisProcedureCriteria
                        .createAllProcedures(), new TechId(experiment)));

        assertFeatureSummary("X", 3.5, summaryResult);
        assertFeatureSummary("Y", 2.5, summaryResult);
        assertFeatureSummary("A", 15.0, summaryResult);
        assertFeatureSummary("B", 2.0, summaryResult);
    }

    @Test
    public void testGetExperimentFeatureVectorSummary() throws Exception
    {
        Experiment experiment =
                commonServer.getExperimentInfo(sessionToken, ExperimentIdentifierFactory
                        .parse("/TEST/TEST-PROJECT/AGGREGATED_FEATURES_EXP"));
        
        ExperimentFeatureVectorSummary expFeatureSummary =
                screeningServer.getExperimentFeatureVectorSummary(sessionToken, new TechId(
                        experiment), AnalysisProcedureCriteria.createAllProcedures());
        
        assertEquals(null, expFeatureSummary.getTableModelOrNull());
        MaterialFeatureVectorSummary materialFeatureSummary =
                expFeatureSummary.getMaterialsSummary().get(0);
        assertEquals("G (GENE)", materialFeatureSummary.getMaterial().toString());
        assertEquals(1, materialFeatureSummary.getNumberOfMaterialsInExperiment());
        List<CodeAndLabel> featureDescriptions = expFeatureSummary.getFeatureDescriptions();
        List<String> featureCodes = new ArrayList<String>();
        for (CodeAndLabel codeAndLabel : featureDescriptions)
        {
            featureCodes.add(codeAndLabel.getCode());
        }
        assertRanks("1, 1, 1, 1", materialFeatureSummary);
        assertSummaries("[A=15.0, B=2.0, X=3.5, Y=2.5]", featureCodes, materialFeatureSummary);
        assertEquals(1, expFeatureSummary.getMaterialsSummary().size());
    }
    
    @Test
    public void testListExperimentFeatureVectorSummaryCalculated() throws Exception
    {
        Experiment experiment =
                commonServer.getExperimentInfo(sessionToken, ExperimentIdentifierFactory
                        .parse("/TEST/TEST-PROJECT/AGGREGATED_FEATURES_EXP"));
        
        ResultSet<TableModelRowWithObject<MaterialFeatureVectorSummary>> resultSet =
                screeningClientService
                .listExperimentFeatureVectorSummary(
                        RESULT_SET_CONFIG,
                        new TechId(experiment),
                        AnalysisProcedureCriteria.createAllProcedures()).getResultSet();
        
        GridRowModels<TableModelRowWithObject<MaterialFeatureVectorSummary>> list =
                resultSet.getList();
        List<TableModelColumnHeader> columnHeaders = list.getColumnHeaders();
        List<String> featureCodes = new ArrayList<String>();
        for (TableModelColumnHeader tableModelColumnHeader : columnHeaders)
        {
            String title = tableModelColumnHeader.getTitle();
            if (title != null && title.equals("Rank") == false)
            {
                featureCodes.add(title);
            }
        }
        TableModelRowWithObject<MaterialFeatureVectorSummary> row = list.get(0).getOriginalObject();
        MaterialFeatureVectorSummary materialFeatureSummary = row.getObjectOrNull();
        assertEquals("G (GENE)", materialFeatureSummary.getMaterial().toString());
        assertEquals(1, materialFeatureSummary.getNumberOfMaterialsInExperiment());
        assertRanks("1, 1, 1, 1", materialFeatureSummary);
        assertSummaries("[A=15.0, B=2.0, X=3.5, Y=2.5]", featureCodes, materialFeatureSummary);
        assertEquals("15.0", getValue("FEATURE_VALUE-A", columnHeaders, row).toString());
        assertEquals("1", getValue("RANKA", columnHeaders, row).toString());
        assertEquals("2.0", getValue("FEATURE_VALUE-B", columnHeaders, row).toString());
        assertEquals("1", getValue("RANKB", columnHeaders, row).toString());
        assertEquals("3.5", getValue("FEATURE_VALUE-X", columnHeaders, row).toString());
        assertEquals("1", getValue("RANKX", columnHeaders, row).toString());
        assertEquals("2.5", getValue("FEATURE_VALUE-Y", columnHeaders, row).toString());
        assertEquals("1", getValue("RANKY", columnHeaders, row).toString());
        assertEquals(experiment.getPermId(), getValue(EXPERIMENT_PERM_ID, columnHeaders, row).toString());
        assertEquals("G", getValue(MATERIAL_ID, columnHeaders, row).toString());
        assertEquals(1, list.size());
    }
    
    private ISerializableComparable getValue(String column,
            List<TableModelColumnHeader> columnHeaders,
            TableModelRowWithObject<MaterialFeatureVectorSummary> row)
    {
        List<String> columns = new ArrayList<String>();
        for (TableModelColumnHeader header : columnHeaders)
        {
            columns.add(header.getId());
            if (header.getId().equals(column))
            {
                return row.getValues().get(header.getIndex());
            }
        }
        throw new AssertionError("Unknown column '" + column + "': " + columns);
    }
    
    @Test
    public void testListExperimentFeatureVectorSummaryFromFile() throws Exception
    {
        Experiment experiment =
                commonServer.getExperimentInfo(sessionToken, ExperimentIdentifierFactory
                        .parse("/TEST/TEST-PROJECT/AGGREGATED_FEATURES_EXP"));
        prepareForGettingAnalysisSummaryFromFile();
        
        ResultSet<TableModelRowWithObject<MaterialFeatureVectorSummary>> resultSet =
                screeningClientService.listExperimentFeatureVectorSummary(RESULT_SET_CONFIG,
                        new TechId(experiment), AnalysisProcedureCriteria.createFromCode("p1"))
                        .getResultSet();
        
        GridRowModels<TableModelRowWithObject<MaterialFeatureVectorSummary>> list =
                resultSet.getList();
        List<TableModelColumnHeader> headers = list.getColumnHeaders();
        assertEquals("[geneId, feature]", headers.toString());
        assertEquals(FeatureVectorSummaryGridColumnIDs.MATERIAL_ID, headers.get(0).getId());
        assertEquals("FEATURE", headers.get(1).getId());
        assertEquals(DataTypeCode.REAL, headers.get(1).getDataType());
        assertEquals(EntityKind.MATERIAL, headers.get(0).tryGetEntityKind());
        TableModelRowWithObject<MaterialFeatureVectorSummary> row = list.get(0).getOriginalObject();
        assertEquals("[G, 42.5]", row.getValues().toString());
        assertEquals(1, list.size());
        assertEquals("G (GENE)", row.getObjectOrNull().getMaterial().toString());
    }

    @Test
    public void testListExperimentFeatureVectorSummaryFromFileButTwoDataSets() throws Exception
    {
        Experiment experiment =
                commonServer.getExperimentInfo(sessionToken, ExperimentIdentifierFactory
                        .parse("/TEST/TEST-PROJECT/AGGREGATED_FEATURES_EXP"));
        prepareForGettingAnalysisSummaryFromFile();

        ResultSet<TableModelRowWithObject<MaterialFeatureVectorSummary>> resultSet =
                screeningClientService.listExperimentFeatureVectorSummary(RESULT_SET_CONFIG,
                        new TechId(experiment), AnalysisProcedureCriteria.createAllProcedures())
                        .getResultSet();

        GridRowModels<TableModelRowWithObject<MaterialFeatureVectorSummary>> list =
                resultSet.getList();
        assertEquals(0, list.size());
    }

    @Test
    public void testListExperimentFeatureVectorSummaryFromFileButNoDataSets() throws Exception
    {
        Experiment experiment =
                commonServer.getExperimentInfo(sessionToken, ExperimentIdentifierFactory
                        .parse("/TEST/TEST-PROJECT/AGGREGATED_FEATURES_EXP"));
        prepareForGettingAnalysisSummaryFromFile();
        
        ResultSet<TableModelRowWithObject<MaterialFeatureVectorSummary>> resultSet =
                screeningClientService.listExperimentFeatureVectorSummary(RESULT_SET_CONFIG,
                        new TechId(experiment), AnalysisProcedureCriteria.createNoProcedures())
                        .getResultSet();
        
        GridRowModels<TableModelRowWithObject<MaterialFeatureVectorSummary>> list =
                resultSet.getList();
        assertEquals(0, list.size());
    }
    
    private void prepareForGettingAnalysisSummaryFromFile()
    {
        Properties properties = new Properties();
        properties.setProperty(AnalysisSettings.KEY,
                ScreeningConstants.DEFAULT_ANALYSIS_WELL_CONTAINER_DATASET_TYPE + ":"
                        + getClass().getSimpleName() + "-viewer");
        analysisSettingServer.setAnalysisSettings(new AnalysisSettings(properties));
        List<DataSetType> dataSetTypes = commonServer.listDataSetTypes(sessionToken);
        for (DataSetType dataSetType : dataSetTypes)
        {
            if (dataSetType.getCode().equals(
                    ScreeningConstants.DEFAULT_ANALYSIS_WELL_CONTAINER_DATASET_TYPE))
            {
                dataSetType.setMainDataSetPattern(".*csv");
                commonServer.updateDataSetType(sessionToken, dataSetType);
                break;
            }
        }
    }
    
    private void assertRanks(String expectedRanks, MaterialFeatureVectorSummary materialFeatureSummary)
    {
        StringBuilder builder = new StringBuilder();
        int[] featureVectorRanks = materialFeatureSummary.getFeatureVectorRanks();
        for (int rank : featureVectorRanks)
        {
            if (builder.length() > 0)
            {
                builder.append(", ");
            }
            builder.append(rank);
        }
        assertEquals(expectedRanks, builder.toString());
    }

    private void assertSummaries(String expectedSummaries, List<String> featureCodes,
            MaterialFeatureVectorSummary materialFeatureSummary)
    {
        List<String> result = new ArrayList<String>();
        float[] summaries = materialFeatureSummary.getFeatureVectorSummary();
        for (int i = 0; i < summaries.length; i++)
        {
            result.add(featureCodes.get(i) + "=" + summaries[i]);
        }
        Collections.sort(result);
        assertEquals(expectedSummaries, result.toString());
    }
    
    private void assertFeatureSummary(String feature, double featureMedianValue,
            MaterialReplicaFeatureSummaryResult summaryResult)
    {
        MaterialReplicaFeatureSummary featureSummary = getFeatureSummary(feature, summaryResult);
        assertNotNull("No feature with name '" + feature + "' found in summary.", featureSummary);
        assertEquals(featureMedianValue, featureSummary.getFeatureVectorSummary());
    }

    protected MaterialReplicaFeatureSummary getFeatureSummary(String feature,
            MaterialReplicaFeatureSummaryResult summaryResult)
    {
        for (MaterialReplicaFeatureSummary summary : summaryResult.getFeatureSummaries())
        {
            if (summary.getFeatureDescription().getCode().equals(feature))
            {
                return summary;
            }
        }
        return null;
    }

    private File createExampleIncoming() throws IOException
    {
        File exampleDataSet = new File(workingDirectory, "test-data");
        exampleDataSet.mkdirs();
        FileUtilities.writeToFile(new File(exampleDataSet, "data-set-1.csv"), "geneId,feature\nG,42.5\n");
        FileUtilities.writeToFile(new File(exampleDataSet, "data-set-2.file"), "dummy2");
        return exampleDataSet;
    }

    @Override
    protected int dataSetImportWaitDurationInSeconds()
    {
        return 60;
    }
}
