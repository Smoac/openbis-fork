/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

/**
 * Configures what kind of associations should be retrieved for the sample.
 * 
 * @author Tomasz Pylak
 */
public class SampleRelationsDepthDTO
{
    private final int generatedFromHierarchyDepth;

    private final int partOfHierarchyDepth;

    public SampleRelationsDepthDTO(int generatedFromHierarchyDepth, int partOfHierarchyDepth)
    {
        this.generatedFromHierarchyDepth = generatedFromHierarchyDepth;
        this.partOfHierarchyDepth = partOfHierarchyDepth;
    }

    /** how many times should we go through generated-from relation retrieving connected samples */
    public int getGeneratedFromHierarchyDepth()
    {
        return generatedFromHierarchyDepth;
    }

    /** how many times should we go through part-of relation retrieving connected samples */
    public int getPartOfHierarchyDepth()
    {
        return partOfHierarchyDepth;
    }

}
