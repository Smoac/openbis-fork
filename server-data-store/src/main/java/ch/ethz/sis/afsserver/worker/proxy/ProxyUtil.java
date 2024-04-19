package ch.ethz.sis.afsserver.worker.proxy;

import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;

public class ProxyUtil
{

    public static IPermIdHolder findOwner(IApplicationServerApi v3, String sessionToken, String owner)
    {
        Experiment foundExperiment = findExperiment(v3, sessionToken, owner);

        if (foundExperiment != null)
        {
            return foundExperiment;
        }

        Sample foundSample = ProxyUtil.findSample(v3, sessionToken, owner);

        if (foundSample != null)
        {
            return foundSample;
        }

        return ProxyUtil.findDataSet(v3, sessionToken, owner);
    }

    public static Experiment findExperiment(IApplicationServerApi v3, String sessionToken, String experimentPermIdOrIdentifier)
    {
        IExperimentId experimentId;

        if (experimentPermIdOrIdentifier.contains("/"))
        { // Is Identifier
            experimentId = new ExperimentIdentifier(experimentPermIdOrIdentifier);
        } else
        { // Is permId
            experimentId = new ExperimentPermId(experimentPermIdOrIdentifier);
        }

        Map<IExperimentId, Experiment> experiments = v3.getExperiments(sessionToken, List.of(experimentId), new ExperimentFetchOptions());

        if (!experiments.isEmpty())
        {
            return experiments.values().iterator().next();
        } else
        {
            return null;
        }
    }

    public static Sample findSample(IApplicationServerApi v3, String sessionToken, String samplePermIdOrIdentifier)
    {
        ISampleId sampleId;

        if (samplePermIdOrIdentifier.contains("/"))
        { // Is Identifier
            sampleId = new SampleIdentifier(samplePermIdOrIdentifier);
        } else
        { // Is permId
            sampleId = new SamplePermId(samplePermIdOrIdentifier);
        }

        Map<ISampleId, Sample> samples = v3.getSamples(sessionToken, List.of(sampleId), new SampleFetchOptions());

        if (!samples.isEmpty())
        {
            return samples.values().iterator().next();
        } else
        {
            return null;
        }
    }

    public static DataSet findDataSet(IApplicationServerApi v3, String sessionToken, String dataSetPermId)
    {
        IDataSetId dataSetId = new DataSetPermId(dataSetPermId);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        Map<IDataSetId, DataSet> dataSets = v3.getDataSets(sessionToken, List.of(dataSetId), fo);

        if (!dataSets.isEmpty())
        {
            return dataSets.values().iterator().next();
        } else
        {
            return null;
        }
    }

}
