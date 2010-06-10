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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * A text area to specify samples for an experiment. Samples are specified by giving codes separated
 * by commas, spaces or new lines.
 * 
 * @author Tomasz Pylak
 */
final class ExperimentSamplesArea extends MultilineVarcharField
{

    public static final String ID_SUFFIX_SAMPLES = "_samples";

    public ExperimentSamplesArea(IMessageProvider messageProvider, String idPrefix)
    {
        super("", false);
        setLabelSeparator("");
        setEmptyText(messageProvider.getMessage(Dict.SAMPLES_LIST));
        setId(createId(idPrefix));
    }

    public static String createId(String idPrefix)
    {
        return idPrefix + ID_SUFFIX_SAMPLES;
    }

    // null if the area has not been modified, the list of all sample codes otherwise
    public final String[] tryGetSampleCodes()
    {
        return tryParseItemList();
    }

    public final void setSamples(List<Sample> samples)
    {
        setSampleCodes(extractCodes(samples));
    }

    private static String[] extractCodes(List<Sample> samples)
    {
        String[] codes = new String[samples.size()];
        int i = 0;
        for (Sample sample : samples)
        {
            codes[i] = sample.getCode();
            i++;
        }
        return codes;
    }

    public final void setSampleCodes(String[] samples)
    {
        String textValue = createTextValue(samples);
        setValue(textValue);
        setOriginalValue(textValue);
    }

    private static String createTextValue(String[] samples)
    {
        StringBuffer sb = new StringBuffer();
        for (String sample : samples)
        {
            if (sb.length() > 0)
            {
                sb.append(", ");
            }
            sb.append(sample);
        }
        return sb.toString();
    }
}
