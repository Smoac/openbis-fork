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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentListPredicate.IExperimentToSpaceQuery;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment.ExperimentInitializer;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class ExperimentListPredicateTest extends CommonCollectionPredicateTest<Experiment>
{

    private static String EXPERIMENT_TYPE_CODE = "TEST_TYPE";

    private static Experiment EXPERIMENT_IN_SPACE_PROJECT;

    private static Experiment EXPERIMENT_IN_SPACE_ANOTHER_PROJECT;

    private static Experiment EXPERIMENT_IN_ANOTHER_SPACE_PROJECT;

    private static Experiment EXPERIMENT_IN_ANOTHER_SPACE_ANOTHER_PROJECT;

    private static Experiment EXPERIMENT_NON_EXISTENT;

    private IExperimentToSpaceQuery experimentToSpaceQuery;

    static
    {
        ExperimentInitializer sp = new Experiment.ExperimentInitializer();
        sp.setId(SPACE_PROJECT_EXPERIMENT_PE.getId());
        sp.setPermId("perm1");
        sp.setCode("E1");
        sp.setExperimentTypeCode(EXPERIMENT_TYPE_CODE);
        sp.setRegistrationDetails(new EntityRegistrationDetails(new EntityRegistrationDetailsInitializer()));
        sp.setIdentifier("/" + SPACE_CODE + "/" + SPACE_PROJECT_CODE + "/" + sp.getCode());

        EXPERIMENT_IN_SPACE_PROJECT = new Experiment(sp);

        ExperimentInitializer sap = new Experiment.ExperimentInitializer();
        sap.setId(SPACE_ANOTHER_PROJECT_EXPERIMENT_PE.getId());
        sap.setPermId("perm2");
        sap.setCode("E2");
        sap.setExperimentTypeCode(EXPERIMENT_TYPE_CODE);
        sap.setRegistrationDetails(new EntityRegistrationDetails(new EntityRegistrationDetailsInitializer()));
        sap.setIdentifier("/" + SPACE_CODE + "/" + SPACE_ANOTHER_PROJECT_CODE + "/" + sap.getCode());

        EXPERIMENT_IN_SPACE_ANOTHER_PROJECT = new Experiment(sap);

        ExperimentInitializer asp = new Experiment.ExperimentInitializer();
        asp.setId(ANOTHER_SPACE_PROJECT_EXPERIMENT_PE.getId());
        asp.setPermId("perm3");
        asp.setCode("E3");
        asp.setExperimentTypeCode(EXPERIMENT_TYPE_CODE);
        asp.setRegistrationDetails(new EntityRegistrationDetails(new EntityRegistrationDetailsInitializer()));
        asp.setIdentifier("/" + ANOTHER_SPACE_CODE + "/" + ANOTHER_SPACE_PROJECT_CODE + "/" + asp.getCode());

        EXPERIMENT_IN_ANOTHER_SPACE_PROJECT = new Experiment(asp);

        ExperimentInitializer asap = new Experiment.ExperimentInitializer();
        asap.setId(ANOTHER_SPACE_ANOTHER_PROJECT_EXPERIMENT_PE.getId());
        asap.setPermId("perm4");
        asap.setCode("E4");
        asap.setExperimentTypeCode(EXPERIMENT_TYPE_CODE);
        asap.setRegistrationDetails(new EntityRegistrationDetails(new EntityRegistrationDetailsInitializer()));
        asap.setIdentifier("/" + ANOTHER_SPACE_CODE + "/" + ANOTHER_SPACE_ANOTHER_PROJECT_CODE + "/" + asap.getCode());

        EXPERIMENT_IN_ANOTHER_SPACE_ANOTHER_PROJECT = new Experiment(asap);

        ExperimentInitializer ne = new Experiment.ExperimentInitializer();
        ne.setId(NON_EXISTENT_SPACE_PE.getId());
        ne.setPermId("perm5");
        ne.setCode("E5");
        ne.setExperimentTypeCode(EXPERIMENT_TYPE_CODE);
        ne.setRegistrationDetails(new EntityRegistrationDetails(new EntityRegistrationDetailsInitializer()));
        ne.setIdentifier("/" + NON_EXISTENT_SPACE_CODE + "/" + NON_EXISTENT_SPACE_PROJECT_CODE + "/" + ne.getCode());

        EXPERIMENT_NON_EXISTENT = new Experiment(ne);
    }

    @Override
    @BeforeMethod
    public void setUp()
    {
        super.setUp();
        experimentToSpaceQuery = context.mock(IExperimentToSpaceQuery.class);
    }

    @Override
    protected Experiment createObject(SpacePE spacePE, ProjectPE projectPE)
    {
        if (SPACE_PE.equals(spacePE))
        {
            if (SPACE_PROJECT_PE.equals(projectPE))
            {
                return EXPERIMENT_IN_SPACE_PROJECT;
            } else if (SPACE_ANOTHER_PROJECT_PE.equals(projectPE))
            {
                return EXPERIMENT_IN_SPACE_ANOTHER_PROJECT;
            }
        } else if (ANOTHER_SPACE_PE.equals(spacePE))
        {
            if (ANOTHER_SPACE_PROJECT_PE.equals(projectPE))
            {
                return EXPERIMENT_IN_ANOTHER_SPACE_PROJECT;
            } else if (ANOTHER_SPACE_ANOTHER_PROJECT_PE.equals(projectPE))
            {
                return EXPERIMENT_IN_ANOTHER_SPACE_ANOTHER_PROJECT;
            }
        } else if (NON_EXISTENT_SPACE_PE.equals(spacePE))
        {
            return EXPERIMENT_NON_EXISTENT;
        }

        throw new RuntimeException();
    }

    @Override
    protected Status evaluateObjects(List<Experiment> object, RoleWithIdentifier... roles)
    {
        ExperimentListPredicate predicate = new ExperimentListPredicate();
        predicate.init(provider);
        predicate.setExperimentToSpaceQuery(experimentToSpaceQuery);
        return predicate.evaluate(PERSON_PE, Arrays.asList(roles), object);
    }

    @Override
    protected void expectWithAll(final IAuthorizationConfig config, final List<Experiment> objects)
    {
        prepareProvider(ALL_SPACES_PE);
        expectAuthorizationConfig(config);

        context.checking(new Expectations()
            {
                {
                    List<Long> experimentIds = new ArrayList<Long>();
                    List<String> experimentPermIds = new ArrayList<String>();
                    final Map<TechId, ExperimentPE> experimentMap = new HashMap<TechId, ExperimentPE>();
                    List<Long> spaceIds = new ArrayList<Long>();

                    if (objects != null)
                    {
                        for (Experiment object : objects)
                        {
                            if (object != null)
                            {
                                experimentIds.add(object.getId());
                                experimentPermIds.add(object.getPermId());

                                if (EXPERIMENT_IN_SPACE_PROJECT.equals(object))
                                {
                                    spaceIds.add(SPACE_PE.getId());
                                    experimentMap.put(new TechId(SPACE_PROJECT_EXPERIMENT_PE.getId()), SPACE_PROJECT_EXPERIMENT_PE);
                                } else if (EXPERIMENT_IN_SPACE_ANOTHER_PROJECT.equals(object))
                                {
                                    spaceIds.add(SPACE_PE.getId());
                                    experimentMap.put(new TechId(SPACE_ANOTHER_PROJECT_EXPERIMENT_PE.getId()), SPACE_ANOTHER_PROJECT_EXPERIMENT_PE);
                                } else if (EXPERIMENT_IN_ANOTHER_SPACE_PROJECT.equals(object))
                                {
                                    spaceIds.add(ANOTHER_SPACE_PE.getId());
                                    experimentMap.put(new TechId(ANOTHER_SPACE_PROJECT_EXPERIMENT_PE.getId()), ANOTHER_SPACE_PROJECT_EXPERIMENT_PE);
                                } else if (EXPERIMENT_IN_ANOTHER_SPACE_ANOTHER_PROJECT.equals(object))
                                {
                                    spaceIds.add(ANOTHER_SPACE_PE.getId());
                                    experimentMap.put(new TechId(ANOTHER_SPACE_ANOTHER_PROJECT_EXPERIMENT_PE.getId()),
                                            ANOTHER_SPACE_ANOTHER_PROJECT_EXPERIMENT_PE);
                                }
                            }
                        }
                    }

                    if (config.isProjectLevelEnabled())
                    {
                        allowing(provider).tryGetExperimentsByTechIds(TechId.createList(experimentIds));
                        will(returnValue(experimentMap));
                    }

                    allowing(experimentToSpaceQuery).getExperimentSpaceIds(with(any(long[].class)), with(any(String[].class)));

                    will(new CustomAction("getExperimentSpaceIds")
                        {

                            @SuppressWarnings("hiding")
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                long[] experimentIds = (long[]) invocation.getParameter(0);
                                List<Long> spaceIds = new ArrayList<Long>();

                                for (long experimentId : experimentIds)
                                {
                                    ExperimentPE experiment = experimentMap.get(new TechId(experimentId));
                                    spaceIds.add(experiment.getProject().getSpace().getId());
                                }

                                return spaceIds;
                            }
                        });
                }
            });
    }

    @Override
    protected void assertWithNull(IAuthorizationConfig config, Status result, Throwable t)
    {
        assertNull(result);
        assertException(t, NullPointerException.class, null);
    }

    @Override
    protected void assertWithNullCollection(IAuthorizationConfig config, Status result, Throwable t)
    {
        assertException(t, UserFailureException.class, "No experiment specified.");
    }

}