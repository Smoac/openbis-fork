/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.common.ssl.SslCertificateHelper;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
public class V3Utils
{
    public static final int TIMEOUT = 100000;

    private final IDataStoreServerApi dss;
    private final IApplicationServerApi as;

    public static V3Utils create(String asUrl, String dssUrl)
    {
        return new V3Utils(asUrl, dssUrl, TIMEOUT);
    }

    private V3Utils (String asUrl, String dssUrl, int timeout)
    {
        SslCertificateHelper.trustAnyCertificate(asUrl);
        SslCertificateHelper.trustAnyCertificate(dssUrl);

        this.dss = HttpInvokerUtils.createStreamSupportingServiceStub(IDataStoreServerApi.class, dssUrl + IDataStoreServerApi.SERVICE_URL, timeout);
        this.as = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, asUrl + IApplicationServerApi.SERVICE_URL, timeout);    
     }

    public SearchResult<DataSetFile> searchFiles(String sessionToken, DataSetFileSearchCriteria criteria, DataSetFileFetchOptions dsFileFetchOptions)
    {
        return dss.searchFiles(sessionToken, criteria, dsFileFetchOptions);
    }

    public SearchResult<DataSetFile> searchWithDataSetCode(String sessionToken, String dataSetCode, DataSetFileFetchOptions dsFileFetchOptions)
    {
        DataSetFileSearchCriteria criteria = new DataSetFileSearchCriteria();
        criteria.withDataSet().withCode().thatEquals(dataSetCode);
        return searchFiles(sessionToken, criteria, dsFileFetchOptions);
    }

    public List<Attachment> getExperimentAttachments(String sessionToken, IExperimentId experimentId)
    {
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withAttachments().withContent();
        fetchOptions.withAttachments().withPreviousVersion().withPreviousVersionUsing(fetchOptions.withAttachments());
        fetchOptions.withAttachments().withPreviousVersion().withContentUsing(fetchOptions.withAttachments().withContent());
        Map<IExperimentId, Experiment> experiments = as.getExperiments(sessionToken, Arrays.asList(experimentId), fetchOptions);
        if(experiments.size() == 1) {
            Experiment experiment = experiments.get(experimentId);
            return experiment.getAttachments();
        }
        return null;
    }

    public List<Attachment> getSampleAttachments(String sessionToken, ISampleId sampleId)
    {
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withAttachments().withContent();
        fetchOptions.withAttachments().withPreviousVersion().withPreviousVersionUsing(fetchOptions.withAttachments());
        fetchOptions.withAttachments().withPreviousVersion().withContentUsing(fetchOptions.withAttachments().withContent());
        Map<ISampleId, Sample> samples = as.getSamples(sessionToken, Arrays.asList(sampleId), fetchOptions);
        if (samples.size() == 1)
        {
            Sample sample = samples.get(sampleId);
            return sample.getAttachments();
        }
        return null;
    }

    public List<Attachment> getProjectAttachments(String sessionToken, IProjectId projectId)
    {
        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withAttachments().withContent();
        fetchOptions.withAttachments().withPreviousVersion().withPreviousVersionUsing(fetchOptions.withAttachments());
        fetchOptions.withAttachments().withPreviousVersion().withContentUsing(fetchOptions.withAttachments().withContent());
        Map<IProjectId, Project> projects = as.getProjects(sessionToken, Arrays.asList(projectId), fetchOptions);
        if (projects.size() == 1)
        {
            Project project = projects.get(projectId);
            return project.getAttachments();
        }
        return null;
    }

    public InputStream downloadFiles(String sessionToken, List<IDataSetFileId> fileIds, DataSetFileDownloadOptions options)
    {
        return dss.downloadFiles(sessionToken, fileIds, options);
    }

    public String login(String loginUser, String loginPass)
    {
        return as.login(loginUser, loginPass);
    }
}
