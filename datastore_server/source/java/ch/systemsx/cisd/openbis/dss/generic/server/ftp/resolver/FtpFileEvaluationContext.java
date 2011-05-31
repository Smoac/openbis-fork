/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.common.io.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

/**
 * An object holding templates evaluation result data.
 * 
 * @author Kaloyan Enimanev
 */
public class FtpFileEvaluationContext
{

    static class EvaluatedElement
    {
        ExternalData dataSet;

        // will only be filled when the ${fileName} variable
        // is used in the template
        String pathInDataSet = StringUtils.EMPTY;

        String evaluatedTemplate;

        IHierarchicalContentNode contentNode;
    }

    private Map<String /* dataset code */, IHierarchicalContent> contents =
            new HashMap<String, IHierarchicalContent>();

    private List<EvaluatedElement> evaluatedPaths = new ArrayList<EvaluatedElement>();

    /**
     * @return the evaluation result.
     */
    public List<EvaluatedElement> getEvalElements()
    {
        return Collections.unmodifiableList(evaluatedPaths);

    }

    /**
     * Adds a collection of {@link EvaluatedElement} to the results.
     */
    public void addEvaluatedElements(Collection<EvaluatedElement> evaluatedPath)
    {
        evaluatedPaths.addAll(evaluatedPath);
    }

    public IHierarchicalContent getHierarchicalContent(ExternalData dataSet)
    {
        String dataSetCode = dataSet.getCode();
        IHierarchicalContent result = contents.get(dataSetCode);
        if (result == null)
        {
            result = createHierarchicalContent(dataSet);
            contents.put(dataSetCode, result);
        }
        return result;
    }

    /**
     * closes the evaluation context and frees all associated resources.
     */
    public void close()
    {
        for (IHierarchicalContent content : contents.values())
        {
            content.close();
        }
        contents.clear();
    }

    private IHierarchicalContent createHierarchicalContent(ExternalData dataSet)
    {
        IHierarchicalContentProvider provider = ServiceProvider.getHierarchicalContentProvider();
        return provider.asContent(dataSet);
    }

}
