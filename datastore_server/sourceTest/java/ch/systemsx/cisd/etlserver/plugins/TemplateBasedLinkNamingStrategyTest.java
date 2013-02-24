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

package ch.systemsx.cisd.etlserver.plugins;

import static ch.systemsx.cisd.etlserver.plugins.TemplateBasedLinkNamingStrategy.DEFAULT_LINK_TEMPLATE;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Test cases for {@link TemplateBasedLinkNamingStrategy}.
 * 
 * @author Kaloyan Enimanev
 */
public class TemplateBasedLinkNamingStrategyTest extends AbstractFileSystemTestCase
{

    public static final String LONG_LINK_TEMPLATE =
            "Instance_${instance}/Space_${space}/Project_${project}/Experiment_${experiment}/DataSetType_${dataSetType}/Sample_${sample}/Dataset_${dataSet}";

    private static final String DATASET_PATH_LONG =
            "Instance_DB-I/Space_GROUP-G/Project_PROJECT-P/Experiment_EXP-E/DataSetType_TYPE-T/Sample_SAMPLE-S/Dataset_DATASET-D";

    private static final String DATASET_PATH_DEFAULT =
            "GROUP-G/PROJECT-P/EXP-E/TYPE-T+SAMPLE-S+DATASET-D";

    private static final String SAMPLE = "SAMPLE-S";

    private static final String PROJECT = "PROJECT-P";

    private static final String GROUP = "GROUP-G";

    private static final String EXPERIMENT = "EXP-E";

    private static final String TYPE = "TYPE-T";

    private static final String LOCATION = "location/L";

    private static final String DATASET = "DATASET-D";

    private static final String DATABASE_INSTANCE = "DB-I";

    @Test
    public void testCreateDataSetPath() throws Exception
    {
        assertEquals(DATASET_PATH_DEFAULT, createPathFromTemplate(DEFAULT_LINK_TEMPLATE));
        assertEquals(DATASET_PATH_LONG, createPathFromTemplate(LONG_LINK_TEMPLATE));

    }

    private String createPathFromTemplate(String template)
    {
        SimpleDataSetInformationDTO dsInfo = createDataSetInfo();
        return new TemplateBasedLinkNamingStrategy(template)
                        .createHierarchicalPath(dsInfo);
    }

    private SimpleDataSetInformationDTO createDataSetInfo()
    {
        SimpleDataSetInformationDTO dsInfo = new SimpleDataSetInformationDTO();
        dsInfo.setDatabaseInstanceCode(DATABASE_INSTANCE);
        dsInfo.setDataSetCode(DATASET);
        dsInfo.setDataSetLocation(LOCATION);
        dsInfo.setDataSetType(TYPE);
        dsInfo.setExperimentCode(EXPERIMENT);
        dsInfo.setSpaceCode(GROUP);
        dsInfo.setProjectCode(PROJECT);
        dsInfo.setSampleCode(SAMPLE);
        return dsInfo;
    }
}
