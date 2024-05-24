package ch.ethz.sis.afsserver.server.observer.impl;

import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ch.ethz.sis.afs.dto.operation.CopyOperation;
import ch.ethz.sis.afs.dto.operation.CreateOperation;
import ch.ethz.sis.afs.dto.operation.MoveOperation;
import ch.ethz.sis.afs.dto.operation.Operation;
import ch.ethz.sis.afs.dto.operation.WriteOperation;
import ch.ethz.sis.afs.manager.TransactionConnection;
import ch.ethz.sis.afsapi.dto.File;
import ch.ethz.sis.afsserver.server.APIServer;
import ch.ethz.sis.afsserver.server.Request;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
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
    public void beforeStartup() throws Exception
    {

    }

    @Override
    public void beforeShutdown() throws Exception
    {

    }

    @Override
    public void beforeAPICall(Worker<TransactionConnection> worker, Request request) throws Exception
    {
        boolean isOnePhaseTransaction = worker.isInteractiveSessionMode();
        boolean isTwoPhaseTransaction = worker.isTransactionManagerMode();

        // handle only transactional calls
        if (!isOnePhaseTransaction && !isTwoPhaseTransaction)
        {
            return;
        }

        if ((isOnePhaseTransaction && request.getMethod().equals("commit")) || (isTwoPhaseTransaction && request.getMethod().equals("prepare")))
        {
            List<String> paths = new ArrayList<>();

            if (worker.getConnection().getTransaction().getOperations() != null)
            {
                for (Operation operation : worker.getConnection().getTransaction().getOperations())
                {
                    if (operation instanceof CreateOperation)
                    {
                        paths.add(((CreateOperation) operation).getSource());
                    } else if (operation instanceof WriteOperation)
                    {
                        paths.add(((WriteOperation) operation).getSource());
                    } else if (operation instanceof CopyOperation)
                    {
                        paths.add(((CopyOperation) operation).getTarget());
                    } else if (operation instanceof MoveOperation)
                    {
                        paths.add(((MoveOperation) operation).getTarget());
                    }
                }
            }

            List<String> owners = paths.stream().map(this::extractOwnerFromPath).filter(Objects::nonNull).collect(Collectors.toList());

            createDataSets(worker, request, owners);
        }
    }

    @Override
    public void afterAPICall(Worker<TransactionConnection> worker, Request request) throws Exception
    {
        boolean isOnePhaseTransaction = worker.isInteractiveSessionMode();
        boolean isTwoPhaseTransaction = worker.isTransactionManagerMode();

        // handle only non-transactional calls
        if (isOnePhaseTransaction || isTwoPhaseTransaction)
        {
            return;
        }

        List<String> owners = new ArrayList<>();

        switch (request.getMethod())
        {
            case "write":
            case "create":
                owners.add((String) request.getParams().get("owner"));
                break;
            case "copy":
            case "move":
                owners.add((String) request.getParams().get("targetOwner"));
                break;
        }

        createDataSets(worker, request, owners);
    }

    private void createDataSets(Worker<TransactionConnection> worker, Request request, List<String> owners) throws Exception
    {
        if (owners == null || owners.isEmpty())
        {
            return;
        }

        List<DataSetCreation> creations = new ArrayList<>();

        for (String owner : owners)
        {
            try
            {
                List<File> ownerFiles = worker.list(owner, "", false);

                if (!ownerFiles.isEmpty())
                {
                    continue;
                }
            } catch (NoSuchFileException e)
            {
                // good, the folder does not exist yet i.e. we should create a data set
            }

            Experiment foundExperiment = findExperiment(request.getSessionToken(), owner);

            if (foundExperiment != null)
            {
                creations.add(createDataSetCreation(request.getSessionToken(), owner, null));
            } else
            {
                Sample foundSample = findSample(request.getSessionToken(), owner);

                if (foundSample != null)
                {
                    creations.add(createDataSetCreation(request.getSessionToken(), null, owner));
                }
            }
        }

        if (!creations.isEmpty())
        {
            applicationServerApi.createDataSets(request.getSessionToken(), creations);
        }
    }

    private DataSetCreation createDataSetCreation(String sessionToken, String experimentPermId, String samplePermId)
    {
        PhysicalDataCreation physicalCreation = new PhysicalDataCreation();
        physicalCreation.setFileFormatTypeId(new FileFormatTypePermId("PROPRIETARY"));
        physicalCreation.setLocatorTypeId(new RelativeLocationLocatorTypePermId());
        physicalCreation.setStorageFormatId(new ProprietaryStorageFormatPermId());

        DataSetCreation creation = new DataSetCreation();
        creation.setDataStoreId(new DataStorePermId("AFS"));
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

        return creation;
    }

    private String createDataSetLocation(String dataSetCode)
    {
        List<String> elements = new LinkedList<>(Arrays.asList(IOUtils.getShards(dataSetCode)));
        elements.add(dataSetCode);
        return IOUtils.getPath(storageUuid, elements.toArray(new String[] {}));
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

    private String extractOwnerFromPath(String ownerPath)
    {
        Pattern compile = Pattern.compile("\\d+/.+/../../../(\\d+-\\d+)/.*");
        Matcher matcher = compile.matcher(ownerPath);

        if (matcher.matches())
        {
            return matcher.group();
        } else
        {
            return null;
        }
    }

}
