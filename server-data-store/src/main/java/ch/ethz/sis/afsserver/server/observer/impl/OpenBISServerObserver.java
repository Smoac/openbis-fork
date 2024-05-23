package ch.ethz.sis.afsserver.server.observer.impl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.afs.api.dto.File;
import ch.ethz.sis.afs.dto.operation.Operation;
import ch.ethz.sis.afs.manager.TransactionConnection;
import ch.ethz.sis.afsserver.server.APIServer;
import ch.ethz.sis.afsserver.server.Worker;
import ch.ethz.sis.afsserver.server.observer.APIServerObserver;
import ch.ethz.sis.afsserver.server.observer.ServerObserver;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameterUtil;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.PhysicalDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ProprietaryStorageFormatPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.RelativeLocationLocatorTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.shared.startup.Configuration;

public class OpenBISServerObserver implements ServerObserver<TransactionConnection>, APIServerObserver<TransactionConnection>
{

    private String storageUuid;

    private IApplicationServerApi applicationServerApi;

    @Override
    public void init(Configuration initParameter) throws Exception
    {
        storageUuid = AtomicFileSystemServerParameterUtil.getStorageUuid(initParameter);
        applicationServerApi = AtomicFileSystemServerParameterUtil.getApplicationServerApi(initParameter);
    }

    @Override
    public void init(APIServer<TransactionConnection, ?, ?, ?> apiServer, Configuration configuration) throws Exception
    {

    }

    @Override
    public void beforeAPICall(Worker<TransactionConnection> worker, String method, Map<String, Object> params) throws Exception
    {

    }

    @Override
    public void afterAPICall(Worker<TransactionConnection> worker, String method, Map<String, Object> params) throws Exception
    {
        String owner = null;

        if (worker.isTransactionManagerMode())
        {
            // two-phase transaction (i.e. AS and AFS transaction with the AS coordinator)
            if (method.equals("prepare"))
            {
                List<Operation> operations = worker.getConnection().getTransaction().getOperations();

                for(Operation operation : operations){

                }
            } else
            {
                return;
            }
        } else
        {
            // one-phase transaction (i.e. AFS only transaction without the AS coordinator) or no transaction (i.e. single method call)
            switch (method)
            {
                case "write":
                case "create":
                    owner = (String) params.get("owner");
                    break;
                case "copy":
                case "move":
                    owner = (String) params.get("targetOwner");
                    break;
            }
        }

        if (owner == null || owner.isBlank())
        {
            return;
        }

        List<File> files = worker.getConnection().list(owner, false);

        if (files.isEmpty())
        {
            String sessionToken = (String) params.get("sessionToken");

            if (sessionToken == null || sessionToken.isBlank())
            {
                return;
            }

            Experiment foundExperiment = findExperiment(sessionToken, owner);

            if (foundExperiment != null)
            {
                createDataSet(sessionToken, owner, null);
            } else
            {
                Sample foundSample = findSample(sessionToken, owner);

                if (foundSample != null)
                {
                    createDataSet(sessionToken, null, owner);
                }
            }
        }
    }

    @Override
    public void beforeStartup() throws Exception
    {

    }

    @Override
    public void beforeShutdown() throws Exception
    {

    }

    private Experiment findExperiment(String sessionToken, String experimentPermId)
    {
        Map<IExperimentId, Experiment> experiments =
                applicationServerApi.getExperiments(sessionToken, List.of(new ExperimentPermId(experimentPermId)), new ExperimentFetchOptions());

        if (!experiments.isEmpty())
        {
            return experiments.values().iterator().next();
        } else
        {
            return null;
        }
    }

    private Sample findSample(String sessionToken, String samplePermId)
    {
        Map<ISampleId, Sample> samples =
                applicationServerApi.getSamples(sessionToken, List.of(new SamplePermId(samplePermId)), new SampleFetchOptions());

        if (!samples.isEmpty())
        {
            return samples.values().iterator().next();
        } else
        {
            return null;
        }
    }

    private void createDataSet(String sessionToken, String experimentPermId, String samplePermId)
    {
        PhysicalDataCreation physicalCreation = new PhysicalDataCreation();
        physicalCreation.setFileFormatTypeId(new FileFormatTypePermId("PROPRIETARY"));
        physicalCreation.setLocatorTypeId(new RelativeLocationLocatorTypePermId());
        physicalCreation.setStorageFormatId(new ProprietaryStorageFormatPermId());

        DataSetCreation creation = new DataSetCreation();
        creation.setDataSetKind(DataSetKind.PHYSICAL);
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setPhysicalData(physicalCreation);

        if (experimentPermId != null)
        {
            creation.setCode(experimentPermId);
            creation.setExperimentId(new ExperimentPermId(experimentPermId));
            physicalCreation.setLocation(createDataSetLocation(experimentPermId));
        } else if (samplePermId != null)
        {
            creation.setCode(samplePermId);
            creation.setSampleId(new SamplePermId(samplePermId));
            physicalCreation.setLocation(createDataSetLocation(samplePermId));
        }

        applicationServerApi.createDataSets(sessionToken, List.of(creation));
    }

    private String createDataSetLocation(String dataSetCode)
    {
        List<String> elements = new LinkedList<>(Arrays.asList(IOUtils.getShards(dataSetCode)));
        elements.add(dataSetCode);
        return IOUtils.getPath(storageUuid, elements.toArray(new String[] {}));
    }

}
