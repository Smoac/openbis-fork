/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.common;

/**
 * Enum of different types of nodes in {@link ChooserTreeModel}
 *
 * @author Franz-Josef Elmer
 */
public enum ChooserTreeNodeType
{
    ROOT(""), SPACE(""), PROJECT("P"), EXPERIMENT("E"), SAMPLE("S"), DATA_SET("DS");

    private final String label;

    private ChooserTreeNodeType(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }
}
