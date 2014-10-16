/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.sample;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.AbstractMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.sample.ListSampleByIdentifier;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.sample.ListSampleByPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class MapSampleByIdExecutor extends AbstractMapObjectByIdExecutor<ISampleId, SamplePE> implements IMapSampleByIdExecutor
{

    private ISpaceDAO spaceDAO;

    private ISampleDAO sampleDAO;

    @SuppressWarnings("unused")
    private MapSampleByIdExecutor()
    {
    }

    public MapSampleByIdExecutor(ISpaceDAO spaceDAO, ISampleDAO sampleDAO)
    {
        this.spaceDAO = spaceDAO;
        this.sampleDAO = sampleDAO;
    }

    @Override
    protected List<IListObjectById<? extends ISampleId, SamplePE>> createListers(IOperationContext context)
    {
        List<IListObjectById<? extends ISampleId, SamplePE>> listers =
                new LinkedList<IListObjectById<? extends ISampleId, SamplePE>>();
        listers.add(new ListSampleByPermId(sampleDAO));
        listers.add(new ListSampleByIdentifier(spaceDAO, sampleDAO, context.getSession().tryGetHomeGroup()));
        return listers;
    }

    @Autowired
    private void setDAOFactory(IDAOFactory daoFactory)
    {
        spaceDAO = daoFactory.getSpaceDAO();
        sampleDAO = daoFactory.getSampleDAO();
    }

}
