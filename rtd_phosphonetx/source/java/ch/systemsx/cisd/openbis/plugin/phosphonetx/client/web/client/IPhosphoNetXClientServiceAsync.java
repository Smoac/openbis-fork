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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ProteinInfo;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface IPhosphoNetXClientServiceAsync extends IClientServiceAsync
{
    /** @see IPhosphoNetXClientService#listProteinsByExperiment(ListProteinByExperimentCriteria) */
    public void listProteinsByExperiment(ListProteinByExperimentCriteria criteria,
            AsyncCallback<ResultSet<ProteinInfo>> callback);
    

    /** @see IPhosphoNetXClientService#prepareExportProteins(TableExportCriteria) */
    public void prepareExportProteins(TableExportCriteria<ProteinInfo> exportCriteria,
            AsyncCallback<String> callback);

}