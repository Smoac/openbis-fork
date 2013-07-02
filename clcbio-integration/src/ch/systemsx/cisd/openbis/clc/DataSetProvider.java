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

package ch.systemsx.cisd.openbis.clc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.clcbio.api.base.persistence.model.PersistenceContainer;
import com.clcbio.api.base.persistence.model.PersistenceModel;
import com.clcbio.api.base.persistence.model.PersistenceStructure;

import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;

/**
 * @author anttil
 */
public class DataSetProvider implements ContentProvider
{

    private IOpenbisServiceFacade openbis;

    private String space;

    private String project;

    private String experiment;

    public DataSetProvider(IOpenbisServiceFacade openbis, String space, String project, String experiment)
    {
        this.openbis = openbis;
        this.space = space;
        this.project = project;
        this.experiment = experiment;
    }

    @Override
    public Collection<PersistenceStructure> getContent(PersistenceContainer parent, PersistenceModel model)
    {
        List<PersistenceStructure> result = new ArrayList<PersistenceStructure>();
        for (DataSet dataset : openbis.listDataSetsForExperiments(Arrays.asList("/" + space + "/" + project + "/" + experiment)))
        {
            result.add(new Folder(dataset.getCode(), parent, model, new FileSystemProvider(openbis, dataset)));
        }
        return result;
    }
}
