/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.authorization.predicate.experiment;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class ExperimentDB
{

    private static final Map<String, Map<String, Object>> EXPERIMENTS = new HashMap<String, Map<String, Object>>();

    public static final String ID = "id";

    public static final String PERM_ID = "permId";

    public static final String CODE = "code";

    public static final String TYPE_CODE = "typeCode";

    static
    {
        Map<String, Object> EXPERIMENT_111 = new HashMap<>();
        EXPERIMENT_111.put(ID, 26L);
        EXPERIMENT_111.put(PERM_ID, "201206190940555-1111");
        EXPERIMENT_111.put(CODE, "AUTH-EXPERIMENT-111");
        EXPERIMENT_111.put(TYPE_CODE, "SIRNA_HCS");

        Map<String, Object> EXPERIMENT_121 = new HashMap<>();
        EXPERIMENT_121.put(ID, 28L);
        EXPERIMENT_121.put(PERM_ID, "201206190940555-1121");
        EXPERIMENT_121.put(CODE, "AUTH-EXPERIMENT-121");
        EXPERIMENT_121.put(TYPE_CODE, "SIRNA_HCS");

        Map<String, Object> EXPERIMENT_211 = new HashMap<>();
        EXPERIMENT_211.put(ID, 30L);
        EXPERIMENT_211.put(PERM_ID, "201206190940555-1211");
        EXPERIMENT_211.put(CODE, "AUTH-EXPERIMENT-211");
        EXPERIMENT_211.put(TYPE_CODE, "SIRNA_HCS");

        Map<String, Object> EXPERIMENT_221 = new HashMap<>();
        EXPERIMENT_221.put(ID, 32L);
        EXPERIMENT_221.put(PERM_ID, "201206190940555-1221");
        EXPERIMENT_221.put(CODE, "AUTH-EXPERIMENT-221");
        EXPERIMENT_221.put(TYPE_CODE, "SIRNA_HCS");

        EXPERIMENTS.put("/AUTH-SPACE-1/AUTH-PROJECT-11", EXPERIMENT_111);
        EXPERIMENTS.put("/AUTH-SPACE-1/AUTH-PROJECT-12", EXPERIMENT_121);
        EXPERIMENTS.put("/AUTH-SPACE-2/AUTH-PROJECT-21", EXPERIMENT_211);
        EXPERIMENTS.put("/AUTH-SPACE-2/AUTH-PROJECT-22", EXPERIMENT_221);
    }

    public static Long getId(SpacePE spacePE, ProjectPE projectPE)
    {
        return getField(spacePE, projectPE, ID);
    }

    public static String getPermId(SpacePE spacePE, ProjectPE projectPE)
    {
        return getField(spacePE, projectPE, PERM_ID);
    }

    public static String getCode(SpacePE spacePE, ProjectPE projectPE)
    {
        return getField(spacePE, projectPE, CODE);
    }

    public static String getTypeCode(SpacePE spacePE, ProjectPE projectPE)
    {
        return getField(spacePE, projectPE, TYPE_CODE);
    }

    public static String getIdentifier(SpacePE spacePE, ProjectPE projectPE)
    {
        return "/" + spacePE.getCode() + "/" + projectPE.getCode() + "/" + getCode(spacePE, projectPE);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(SpacePE spacePE, ProjectPE projectPE, String fieldName)
    {
        Map<String, Object> experiment = EXPERIMENTS.get("/" + spacePE.getCode() + "/" + projectPE.getCode());

        if (experiment == null)
        {
            throw new RuntimeException("Couldn't find experiment for space: " + spacePE + " and project: " + projectPE);
        }

        if (false == experiment.containsKey(fieldName))
        {
            throw new RuntimeException("Couldn't find field: " + fieldName + " for experiment: " + experiment);
        }

        return (T) experiment.get(fieldName);
    }

}
