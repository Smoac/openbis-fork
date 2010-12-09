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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.CONTAINER_SAMPLE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.DATABASE_INSTANCE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.EXPERIMENT;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.EXPERIMENT_IDENTFIER;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.IS_INSTANCE_SAMPLE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.IS_IVALID;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.PARENTS;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.PERM_ID;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.PROJECT;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.REGISTRATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.REGISTRATOR;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.SAMPLE_IDENTIFIER;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.SAMPLE_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.SHOW_DETAILS_LINK_COLUMN_NAME;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.SPACE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs.SUBCODE;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria2;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SampleProvider extends AbstractCommonTableModelProvider<Sample>
{
    private final ListSampleDisplayCriteria2 criteria;

    public SampleProvider(ICommonServer commonServer, String sessionToken, ListSampleDisplayCriteria2 criteria)
    {
        super(commonServer, sessionToken);
        this.criteria = criteria;
    }

    @Override
    protected TypedTableModel<Sample> createTableModel()
    {
        List<Sample> samples = getSamples();
        TypedTableModelBuilder<Sample> builder = new TypedTableModelBuilder<Sample>();
        builder.addColumn(CODE);
        builder.addColumn(SUBCODE).hideByDefault();
        builder.addColumn(DATABASE_INSTANCE).hideByDefault();
        builder.addColumn(SPACE).hideByDefault();
        builder.addColumn(SAMPLE_IDENTIFIER).withDefaultWidth(150).hideByDefault();
        builder.addColumn(SAMPLE_TYPE).withDefaultWidth(150).hideByDefault();
        builder.addColumn(IS_INSTANCE_SAMPLE).hideByDefault();
        builder.addColumn(IS_IVALID).hideByDefault();
        builder.addColumn(REGISTRATOR).withDefaultWidth(200);
        builder.addColumn(REGISTRATION_DATE).withDefaultWidth(300).hideByDefault();
        builder.addColumn(EXPERIMENT);
        builder.addColumn(EXPERIMENT_IDENTFIER).withDefaultWidth(200).hideByDefault();
        builder.addColumn(PROJECT);
        builder.addColumn(PERM_ID).hideByDefault();
        builder.addColumn(SHOW_DETAILS_LINK_COLUMN_NAME).hideByDefault();
        builder.addColumn(PARENTS);
        builder.addColumn(CONTAINER_SAMPLE);
        for (Sample sample : samples)
        {
            builder.addRow(sample);
            builder.column(CODE).addString(sample.getCode());
            builder.column(SUBCODE).addString(sample.getSubCode());
            builder.column(DATABASE_INSTANCE).addString(getDatabaseInstance(sample).getCode());
            builder.column(SPACE).addString(sample.getSpace() == null ? "" : sample.getSpace().getCode());
            builder.column(SAMPLE_IDENTIFIER).addString(sample.getIdentifier());
            builder.column(SAMPLE_TYPE).addString(sample.getSampleType().getCode());
            builder.column(IS_INSTANCE_SAMPLE).addString(SimpleYesNoRenderer.render(sample.getDatabaseInstance() != null));
            builder.column(IS_IVALID).addString(SimpleYesNoRenderer.render(sample.getInvalidation() != null));
            builder.column(REGISTRATOR).addPerson(sample.getRegistrator());
            builder.column(REGISTRATION_DATE).addDate(sample.getRegistrationDate());
            builder.column(EXPERIMENT).addString(getExperimentCode(sample));
            builder.column(EXPERIMENT_IDENTFIER).addString(getExperimentIdentifier(sample));
            builder.column(PROJECT).addString(getProjectCode(sample));
            builder.column(PERM_ID).addString(sample.getPermId());
            builder.column(SHOW_DETAILS_LINK_COLUMN_NAME).addString(sample.getPermlink());
            builder.column(PARENTS).addString(getParents(sample));
            builder.column(CONTAINER_SAMPLE).addString(getContainer(sample));
            builder.columnGroup("property-").addProperties(sample.getProperties());
        }
        return builder.getModel();
    }

    private String getParents(Sample sample)
    {
        Sample generatedFrom = sample.getGeneratedFrom();
        return generatedFrom == null ? "" : generatedFrom.getIdentifier();
    }

    private String getContainer(Sample sample)
    {
        Sample container = sample.getContainer();
        return container == null ? "" : container.getIdentifier();
    }
    
    private String getProjectCode(Sample sample)
    {
        Experiment experiment = sample.getExperiment();
        return experiment == null ? "" : experiment.getProject().getCode();
    }

    private String getExperimentCode(Sample sample)
    {
        Experiment experiment = sample.getExperiment();
        return experiment == null ? "" : experiment.getCode();
    }
    
    private String getExperimentIdentifier(Sample sample)
    {
        Experiment experiment = sample.getExperiment();
        return experiment == null ? "" : experiment.getIdentifier();
    }
    
    private DatabaseInstance getDatabaseInstance(Sample sample)
    {
        DatabaseInstance databaseInstance = sample.getDatabaseInstance();
        if (databaseInstance == null)
        {
            databaseInstance = sample.getSpace().getInstance();
        }
        return databaseInstance;
    }

    private final List<Sample> getSamples()
    {
        switch (criteria.getCriteriaKind())
        {
            case BROWSE:
                return commonServer.listSamples(sessionToken, criteria.getBrowseCriteria());
            case SEARCH:
                return commonServer.searchForSamples(sessionToken, criteria.getSearchCriteria());
        }
        return null; // not possible
    }
}
