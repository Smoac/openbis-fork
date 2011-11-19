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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.ARCHIVING_STATUS;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.CONTAINER_DATASET;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.DATA_PRODUCER_CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.DATA_SET_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.DATA_STORE_CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.EXPERIMENT;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.EXPERIMENT_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.EXTERNAL_DATA_EXPERIMENT_IDENTIFIER;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.EXTERNAL_DATA_SAMPLE_IDENTIFIER;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.FILE_FORMAT_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.IS_COMPLETE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.IS_DELETED;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.LOCATION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.ORDER_IN_CONTAINER;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.PERM_ID;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.PRESENT_IN_ARCHIVE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.PRODUCTION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.PROJECT;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.PROPERTIES_PREFIX;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.REGISTRATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.REGISTRATOR;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.SAMPLE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.SAMPLE_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.SHOW_DETAILS_LINK;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs.SOURCE_TYPE;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.DeletionUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.IColumnGroup;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractExternalDataProvider extends
        AbstractCommonTableModelProvider<ExternalData>
{
    public AbstractExternalDataProvider(ICommonServer commonServer, String sessionToken)
    {
        super(commonServer, sessionToken);
    }

    @Override
    protected TypedTableModel<ExternalData> createTableModel()
    {
        List<ExternalData> dataSets = getDataSets();
        TypedTableModelBuilder<ExternalData> builder = new TypedTableModelBuilder<ExternalData>();
        builder.addColumn(CODE).withDefaultWidth(150);
        builder.addColumn(DATA_SET_TYPE).withDefaultWidth(200);
        builder.addColumn(CONTAINER_DATASET).withDefaultWidth(150).hideByDefault();
        builder.addColumn(ORDER_IN_CONTAINER).withDefaultWidth(100).hideByDefault();
        builder.addColumn(SAMPLE).withDefaultWidth(100).hideByDefault();
        builder.addColumn(EXTERNAL_DATA_SAMPLE_IDENTIFIER).withDefaultWidth(200);
        builder.addColumn(SAMPLE_TYPE);
        builder.addColumn(EXPERIMENT).withDefaultWidth(100).hideByDefault();
        builder.addColumn(EXTERNAL_DATA_EXPERIMENT_IDENTIFIER).withDefaultWidth(100)
                .hideByDefault();
        builder.addColumn(EXPERIMENT_TYPE).withDefaultWidth(120).hideByDefault();
        builder.addColumn(PROJECT);
        builder.addColumn(REGISTRATOR);
        builder.addColumn(REGISTRATION_DATE).withDefaultWidth(200);
        builder.addColumn(IS_DELETED).hideByDefault();
        builder.addColumn(SOURCE_TYPE).hideByDefault();
        builder.addColumn(IS_COMPLETE).hideByDefault();
        builder.addColumn(LOCATION).hideByDefault();
        builder.addColumn(ARCHIVING_STATUS).withDefaultWidth(200).hideByDefault();
        builder.addColumn(FILE_FORMAT_TYPE).hideByDefault();
        builder.addColumn(PRODUCTION_DATE).withDefaultWidth(200).hideByDefault();
        builder.addColumn(DATA_PRODUCER_CODE).hideByDefault();
        builder.addColumn(DATA_STORE_CODE).hideByDefault();
        builder.addColumn(PERM_ID).hideByDefault();
        builder.addColumn(SHOW_DETAILS_LINK).hideByDefault();
        for (ExternalData dataSet : dataSets)
        {
            builder.addRow(dataSet);
            builder.column(CODE).addEntityLink(dataSet, dataSet.getCode());
            builder.column(DATA_SET_TYPE).addString(dataSet.getDataSetType().getCode());
            ContainerDataSet container = dataSet.tryGetContainer();
            if (container != null)
            {
                builder.column(CONTAINER_DATASET).addEntityLink(container, container.getCode());
            }
            Integer orderInContainer = dataSet.getOrderInContainer();
            builder.column(ORDER_IN_CONTAINER).addString(
                    orderInContainer == null ? "" : orderInContainer.toString());
            Sample sample = dataSet.getSample();
            if (sample != null)
            {
                builder.column(SAMPLE).addEntityLink(sample, sample.getCode());
                builder.column(EXTERNAL_DATA_SAMPLE_IDENTIFIER).addEntityLink(sample,
                        sample.getIdentifier());
                SampleType sampleType = dataSet.getSampleType();
                builder.column(SAMPLE_TYPE).addString(sampleType.getCode());
            }
            Experiment experiment = dataSet.getExperiment();
            if (experiment != null)
            {
                builder.column(EXPERIMENT).addEntityLink(experiment, experiment.getCode());
                builder.column(EXTERNAL_DATA_EXPERIMENT_IDENTIFIER).addEntityLink(experiment,
                        experiment.getIdentifier());
                builder.column(EXPERIMENT_TYPE).addString(experiment.getEntityType().getCode());
                builder.column(PROJECT).addString(experiment.getProject().getCode());
            }
            builder.column(REGISTRATOR).addPerson(dataSet.getRegistrator());
            builder.column(REGISTRATION_DATE).addDate(dataSet.getRegistrationDate());
            builder.column(IS_DELETED).addString(
                    SimpleYesNoRenderer.render(DeletionUtils.isDeleted(dataSet)));
            builder.column(SOURCE_TYPE).addString(dataSet.getSourceType());
            if (dataSet instanceof DataSet)
            {
                DataSet realDataSet = (DataSet) dataSet;
                Boolean complete = realDataSet.getComplete();
                builder.column(IS_COMPLETE).addString(
                        complete == null ? "?" : SimpleYesNoRenderer.render(complete));
                builder.column(LOCATION).addString(realDataSet.getFullLocation());
                builder.column(ARCHIVING_STATUS)
                        .addString(realDataSet.getStatus().getDescription());
                builder.column(PRESENT_IN_ARCHIVE).addString(
                        SimpleYesNoRenderer.render(realDataSet.isPresentInArchive()));
                FileFormatType fileFormatType = realDataSet.getFileFormatType();
                builder.column(FILE_FORMAT_TYPE).addString(
                        fileFormatType == null ? "" : fileFormatType.getCode());
            }
            builder.column(PRODUCTION_DATE).addDate(dataSet.getProductionDate());
            builder.column(DATA_PRODUCER_CODE).addString(dataSet.getDataProducerCode());
            builder.column(DATA_STORE_CODE).addString(dataSet.getDataStore().getCode());
            builder.column(PERM_ID).addString(dataSet.getPermId());
            builder.column(SHOW_DETAILS_LINK).addString(dataSet.getPermlink());
            IColumnGroup columnGroup = builder.columnGroup(PROPERTIES_PREFIX);
            DataSetType dataSetType = dataSet.getDataSetType();
            if (dataSetType != null)
            {
                columnGroup.addColumnsForAssignedProperties(dataSetType);
            }
            columnGroup.addProperties(dataSet.getProperties());

        }
        return builder.getModel();
    }

    protected abstract List<ExternalData> getDataSets();

}
