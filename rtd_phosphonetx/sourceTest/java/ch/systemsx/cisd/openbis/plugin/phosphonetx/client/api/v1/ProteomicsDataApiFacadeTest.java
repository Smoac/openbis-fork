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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.api.v1;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.IProteomicsDataService;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.DataStoreServerProcessingPluginInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.MsInjectionDataInfo;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ProteomicsDataApiFacadeTest extends AssertJUnit
{
    private static final String SESSION_TOKEN = "Session-42";
    
    private Mockery context;
    private IProteomicsDataService proteomicsDataService;
    private IGeneralInformationService generalInfoService;
    private IProteomicsDataApiFacade facade;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        proteomicsDataService = context.mock(IProteomicsDataService.class);
        generalInfoService = context.mock(IGeneralInformationService.class);
        facade = new ProteomicsDataApiFacade(proteomicsDataService, generalInfoService, SESSION_TOKEN);
    }
    
    @AfterMethod
    public void afterMethod()
    {
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetSessionToken()
    {
        assertEquals(SESSION_TOKEN, facade.getSessionToken());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testLogout()
    {
        context.checking(new Expectations()
            {
                {
                    one(generalInfoService).logout(SESSION_TOKEN);
                }
            });
        
        facade.logout();
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testListDataStoreServerProcessingPluginInfos()
    {
        final List<DataStoreServerProcessingPluginInfo> result =
                new LinkedList<DataStoreServerProcessingPluginInfo>();
        context.checking(new Expectations()
            {
                {
                    one(proteomicsDataService).listDataStoreServerProcessingPluginInfos(SESSION_TOKEN);
                    will(returnValue(result));
                }
            });
        
        assertSame(result, facade.listDataStoreServerProcessingPluginInfos());
        context.assertIsSatisfied();
    }

    @Test
    public void testListRawDataSamples()
    {
        final List<MsInjectionDataInfo> result = new LinkedList<MsInjectionDataInfo>();
        context.checking(new Expectations()
            {
                {
                    one(proteomicsDataService).listRawDataSamples(SESSION_TOKEN, "user1");
                    will(returnValue(result));
                }
            });

        assertSame(result, facade.listRawDataSamples("user1"));
        context.assertIsSatisfied();
    }
    
    @Test
    public void testProcessingRawData()
    {
        context.checking(new Expectations()
            {
                {
                    one(proteomicsDataService).processingRawData(SESSION_TOKEN, "user1", "key",
                            new long[42], "type");
                }
            });

        facade.processingRawData("user1", "key", new long[42], "type");
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testListProjectsButNoUserRoles()
    {
        context.checking(new Expectations()
            {
                {
                    one(generalInfoService).listNamedRoleSets(SESSION_TOKEN);
                    will(returnValue(new HashMap<String, Set<Role>>()));
                }
            });
        
        try
        {
            facade.listProjects("user1");
            fail("IllegalStateException expected");
        } catch (IllegalStateException ex)
        {
            assertEquals("Role set SPACE_USER not known.", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testListProjects()
    {
        context.checking(new Expectations()
        {
            {
                one(generalInfoService).listNamedRoleSets(SESSION_TOKEN);
                HashMap<String, Set<Role>> sets = new HashMap<String, Set<Role>>();
                HashSet<Role> set = new HashSet<Role>();
                set.add(new Role("R1", true));
                set.add(new Role("R2", false));
                sets.put("SPACE_USER", set);
                will(returnValue(sets));
                
                one(generalInfoService).listSpacesWithProjectsAndRoleAssignments(SESSION_TOKEN, null);
                SpaceWithProjectsAndRoleAssignments a = createSpace("A", "alpha", "beta");
                a.add("user1", new Role("R1", true));
                a.add("user1", new Role("R3", false));
                SpaceWithProjectsAndRoleAssignments b = createSpace("B", "gamma");
                b.add("user1", new Role("R2", true));
                will(returnValue(Arrays.asList(a, b)));
            }
        });
        
        List<Project> projects = facade.listProjects("user1");
        
        assertEquals("[/A/alpha, /A/beta]", projects.toString());
        context.assertIsSatisfied();
    }
    
    private SpaceWithProjectsAndRoleAssignments createSpace(String spaceCode, String... projects)
    {
        SpaceWithProjectsAndRoleAssignments space = new SpaceWithProjectsAndRoleAssignments(spaceCode);
        for (String project : projects)
        {
            space.add(new Project(spaceCode, project));
        }
        return space;
    }
}
