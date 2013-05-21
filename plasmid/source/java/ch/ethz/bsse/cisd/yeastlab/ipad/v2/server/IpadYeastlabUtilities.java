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

package ch.ethz.bsse.cisd.yeastlab.ipad.v2.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.etlserver.registrator.api.v1.ISample;
import ch.systemsx.cisd.openbis.ipad.v2.server.IpadServiceUtilities;

/**
 * @author cramakri
 */
public class IpadYeastlabUtilities
{

    /**
     * Return a collection of properties, ordered by display ordering, skipping propsToIgnore.
     */
    public static List<String> orderedPropertiesForSampleIgnoring(ISample sample,
            List<String> propsToIgnore)
    {
        return null;
    }

    /**
     * Convert a sample to a dictionary, ignoring the specified properties.
     * <p>
     * <ul>
     * <li>Uses the NAME property to construct the summary.</li>
     * <li>Returns empty children.</li>
     * <li>Callers may need to modify the summary and children as well</li>
     * </ul>
     */
    public static Map<String, Object> samplToDictWithPropsIgnoring(ISample sample,
            boolean wantProps, List<String> propsToIgnore)
    {
        HashMap<String, Object> sampleDict = new HashMap<String, Object>();
        sampleDict.put("SUMMARY_HEADER", sample.getCode());
        String name = sample.getPropertyValue("NAME");
        String summary = (null != name) ? "Name: " + name : "??";
        sampleDict.put("SUMMARY", summary);
        sampleDict.put("IDENTIFIER", sample.getSampleIdentifier());
        sampleDict.put("PERM_ID", sample.getPermId());

        HashMap<String, String> refconSample = new HashMap<String, String>();
        refconSample.put("code", sample.getCode());
        refconSample.put("entityKind", "SAMPLE");
        refconSample.put("entityType", sample.getSampleType());
        sampleDict.put("REFCON", IpadServiceUtilities.jsonEncodedValue(refconSample));
        sampleDict.put("CATEGORY", sample.getSampleType());
        sampleDict.put("CHILDREN", IpadServiceUtilities.jsonEncodedValue(Collections.emptyList()));

        if (wantProps)
        {
            List<String> sampleProperties =
                    orderedPropertiesForSampleIgnoring(sample, propsToIgnore);
            sampleDict.put("PROPERTIES", IpadServiceUtilities.jsonEncodedValue(sampleProperties));
        }

        sampleDict.put("ROOT_LEVEL", false);
        return sampleDict;
    }
}
