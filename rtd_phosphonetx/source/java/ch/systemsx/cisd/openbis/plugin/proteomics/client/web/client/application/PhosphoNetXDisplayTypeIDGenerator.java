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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;

/**
 * @author Franz-Josef Elmer
 */
public enum PhosphoNetXDisplayTypeIDGenerator implements IDisplayTypeIDGenerator
{
    PROTEIN_BY_EXPERIMENT_BROWSER_GRID("protein-by-experiment-browser-grid"),

    PROTEIN_SEQUENCE_BROWSER_GRID("protein-sequence-browser-grid"),

    PROTEIN_SUMMARY_BROWSER_GRID("protein-summary-browser-grid"),

    DATA_SET_PROTEIN_BROWSER_GRID("data-set-protein-browser-grid"),

    RAW_DATA_SAMPLE_BROWSER_GRID("raw-data-sample-browser-grid"),

    PARENT_LESS_MS_INJECTION_SAMPLE_BROWSER_GRID("parent-less-ms-injection-sample-browser-grid"),

    BIOLOGICAL_SAMPLE_BROWSER_GRID("biological-sample-browser-grid"), ;

    private final String genericNameOrPrefix;

    private PhosphoNetXDisplayTypeIDGenerator(String genericNameOrPrefix)
    {
        this.genericNameOrPrefix = genericNameOrPrefix;
    }

    @Override
    public String createID()
    {
        return genericNameOrPrefix;
    }

    @Override
    public String createID(String suffix)
    {
        return genericNameOrPrefix + suffix;
    }

}
