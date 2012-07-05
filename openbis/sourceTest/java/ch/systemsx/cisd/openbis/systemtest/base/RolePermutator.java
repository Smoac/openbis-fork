/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;

/**
 * @author anttil
 */
public class RolePermutator
{

    public static Collection<RoleWithHierarchy> allInstanceRoles = new HashSet<RoleWithHierarchy>();

    public static Collection<RoleWithHierarchy> allSpaceRoles = new HashSet<RoleWithHierarchy>();

    private final int numSpaces;

    private final RoleWithHierarchy[] limits;

    {
        allInstanceRoles.add(RoleWithHierarchy.INSTANCE_ADMIN);
        allInstanceRoles.add(RoleWithHierarchy.INSTANCE_ETL_SERVER);
        allInstanceRoles.add(RoleWithHierarchy.INSTANCE_OBSERVER);
        allInstanceRoles.add(null);

        allSpaceRoles.add(RoleWithHierarchy.SPACE_ADMIN);
        allSpaceRoles.add(RoleWithHierarchy.SPACE_ETL_SERVER);
        allSpaceRoles.add(RoleWithHierarchy.SPACE_POWER_USER);
        allSpaceRoles.add(RoleWithHierarchy.SPACE_USER);
        allSpaceRoles.add(RoleWithHierarchy.SPACE_OBSERVER);
        allSpaceRoles.add(null);
    }

    public RolePermutator(int numSpaces, RoleWithHierarchy... limits)
    {
        this.numSpaces = numSpaces;
        this.limits = limits;

    }

    public RoleWithHierarchy[][] getAcceptedPermutations()
    {
        return toNestedArray(acceptedRoles(numSpaces, limits));
    }

    public RoleWithHierarchy[][] getRejectedPermutations()
    {
        return toNestedArray(rejectedRoles(numSpaces, limits));
    }

    public static RoleWithHierarchy[][] toNestedArray(Collection<List<RoleWithHierarchy>> input)
    {
        RoleWithHierarchy[][] result = new RoleWithHierarchy[input.size()][];
        int index = 0;
        for (List<RoleWithHierarchy> roles : input)
        {
            result[index] = roles.toArray(new RoleWithHierarchy[0]);
            index++;
        }
        return result;
    }

    public static Collection<List<RoleWithHierarchy>> acceptedRoles(int numSpaceRoles,
            RoleWithHierarchy... limits)
    {
        Collection<RoleWithHierarchy> acceptedRoles = new HashSet<RoleWithHierarchy>();
        for (RoleWithHierarchy limit : limits)
        {
            acceptedRoles.addAll(limit.getRoles());
        }

        Collection<List<RoleWithHierarchy>> results = new HashSet<List<RoleWithHierarchy>>();
        for (RoleWithHierarchy instanceRole : allInstanceRoles)
        {
            for (List<RoleWithHierarchy> spaceRoles : getSpaceRoleCombinations(numSpaceRoles))
            {
                if (acceptedRoles.contains(instanceRole) || acceptedRoles.containsAll(spaceRoles))
                {
                    List<RoleWithHierarchy> result = new ArrayList<RoleWithHierarchy>();
                    for (RoleWithHierarchy spaceRole : spaceRoles)
                    {
                        result.add(spaceRole);
                    }
                    result.add(instanceRole);
                    results.add(result);
                }
            }
        }
        return results;
    }

    public static Collection<List<RoleWithHierarchy>> rejectedRoles(int numSpaceRoles,
            RoleWithHierarchy... limits)
    {
        Collection<List<RoleWithHierarchy>> allCombinations = getAllRoleCombinations(numSpaceRoles);
        Collection<List<RoleWithHierarchy>> acceptedCombinations =
                acceptedRoles(numSpaceRoles, limits);
        allCombinations.removeAll(acceptedCombinations);
        return allCombinations;
    }

    public static Collection<List<RoleWithHierarchy>> getSpaceRoleCombinations(int num)
    {
        Collection<List<RoleWithHierarchy>> result = new HashSet<List<RoleWithHierarchy>>();
        if (num == 0)
        {
            return result;
        }

        Collection<List<RoleWithHierarchy>> subLists = getSpaceRoleCombinations(num - 1);

        for (RoleWithHierarchy role : allSpaceRoles)
        {
            if (subLists.size() == 0)
            {
                List<RoleWithHierarchy> list = new ArrayList<RoleWithHierarchy>();
                list.add(role);
                result.add(list);
            } else
            {
                for (List<RoleWithHierarchy> subList : subLists)
                {
                    List<RoleWithHierarchy> list = new ArrayList<RoleWithHierarchy>();
                    list.add(role);
                    list.addAll(subList);
                    result.add(list);
                }
            }
        }
        return result;
    }

    public static Collection<List<RoleWithHierarchy>> getAllRoleCombinations(int numSpaceRoles)
    {
        Collection<List<RoleWithHierarchy>> result = new HashSet<List<RoleWithHierarchy>>();

        for (List<RoleWithHierarchy> spaceRoles : getSpaceRoleCombinations(numSpaceRoles))
        {
            for (RoleWithHierarchy instanceRole : allInstanceRoles)
            {
                List<RoleWithHierarchy> list = new ArrayList<RoleWithHierarchy>();
                list.addAll(spaceRoles);
                list.add(instanceRole);

                if (containsOnlyNulls(list))
                {
                    continue;
                }

                result.add(list);
            }
        }
        return result;
    }

    public static boolean containsOnlyNulls(Collection<?> collection)
    {
        for (Object o : collection)
        {
            if (o != null)
            {
                return false;
            }
        }
        return true;
    }

}
