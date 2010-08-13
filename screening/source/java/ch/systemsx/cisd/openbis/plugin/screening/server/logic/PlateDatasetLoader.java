package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import static ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.SpaceCodeHelper;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * A helper class for retrieving data sets associated with plates.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class PlateDatasetLoader
{
    // TODO 2010-05-27, CR : This class and its subclasses should be refactored. The subclasses
    // should become independent and use the dataset loader, instead of being dataset loaders.

    // Infrastructure state
    protected final Session session;

    protected final IScreeningBusinessObjectFactory businessObjectFactory;

    // Parameter state
    private final Collection<? extends PlateIdentifier> plates;

    private final String[] datasetTypeCodes;

    private String homeSpaceOrNull;

    // Running state
    private List<Sample> samples;

    private List<ExternalData> datasets;

    private HashMap<SampleIdentifier, Sample> samplesByIdentifier;

    private HashMap<Long, Sample> samplesById;

    private boolean loaded = false;

    PlateDatasetLoader(Session session, IScreeningBusinessObjectFactory businessObjectFactory,
            String homeSpaceOrNull, Collection<? extends PlateIdentifier> plates,
            String... datasetTypeCodes)
    {
        this.session = session;
        this.businessObjectFactory = businessObjectFactory;
        this.homeSpaceOrNull = (homeSpaceOrNull != null) ? ("/" + homeSpaceOrNull + "/") : null;
        this.plates = plates;
        this.datasetTypeCodes = datasetTypeCodes;
    }

    protected List<ExternalData> getDatasets()
    {
        return datasets;
    }

    /**
     * Loads the samples and datasets. Call the method before calling {@link #getDatasets()}.
     */
    protected void load()
    {
        if (loaded == false)
        {
            loadSamples();
            loadDatasets();
            loaded = true;
        }
    }

    private void loadSamples()
    {
        // NOTE: plate identifier can use augmented codes or/and permIds. That is why we have to use
        // both when fetching plates.
        // It can result in fetching the same sample twice, but it does not hurt.
        String[] sampleCodesArray = extractSampleCodes();
        String[] samplePermIdArray = extractPermIds();
        ListOrSearchSampleCriteria criteria =
                new ListOrSearchSampleCriteria(sampleCodesArray, samplePermIdArray);
        ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        samples = sampleLister.list(criteria);
        // After loading the samples, we need to initialize the maps because other methods rely
        // on this.
        initializeSampleMaps();
        filterSamplesByPlateIdentifiers();
    }

    private void loadDatasets()
    {
        List<Long> sampleIds = extractSampleIds();
        IDatasetLister datasetLister = businessObjectFactory.createDatasetLister(session);
        datasets = datasetLister.listBySampleIds(sampleIds);
        datasets = ScreeningUtils.filterExternalDataByType(datasets, datasetTypeCodes);
    }

    private void initializeSampleMaps()
    {
        samplesByIdentifier = new HashMap<SampleIdentifier, Sample>();
        samplesById = new HashMap<Long, Sample>();
        for (Sample sample : samples)
        {
            samplesByIdentifier.put(createSampleIdentifier(sample), sample);
            samplesById.put(sample.getId(), sample);
        }
    }

    /**
     * Filter the list of samples, which were selected by code, to those that exactly match the
     * identifiers in the plates
     */
    private void filterSamplesByPlateIdentifiers()
    {
        final Set<String> augmentedCodeSet = new HashSet<String>(plates.size());
        final Set<String> permIdSet = new HashSet<String>(plates.size());
        for (PlateIdentifier plate : plates)
        {
            if (plate.getPermId() != null)
            {
                permIdSet.add(plate.getPermId());
            } else
            {
                if (SpaceCodeHelper.isHomeSpace(plate.tryGetSpaceCode()))
                {
                    if (homeSpaceOrNull == null)
                    {
                        throw UserFailureException.fromTemplate(
                                "Plate '%s' is in home space, but user has no home space defined.",
                                plate);
                    }
                    augmentedCodeSet.add(homeSpaceOrNull + plate.getAugmentedCode());
                } else
                {
                    augmentedCodeSet.add(plate.getAugmentedCode());
                }
            }
        }
        for (Iterator<Sample> it = samples.iterator(); it.hasNext(); /**/)
        {
            final Sample sample = it.next();
            final String sampleSpaceCodeOrNull =
                    (null == sample.getSpace()) ? null : sample.getSpace().getCode();
            final String augmentedCode =
                    (sampleSpaceCodeOrNull == null) ? ("/" + sample.getCode()) : ("/"
                            + sampleSpaceCodeOrNull + "/" + sample.getCode());
            if (permIdSet.contains(sample.getPermId()) == false
                    && augmentedCodeSet.contains(augmentedCode) == false)
            {
                it.remove();
                samplesByIdentifier.remove(createSampleIdentifier(sample));
                samplesById.remove(sample.getId());
            }
        }
    }

    protected PlateIdentifier createPlateIdentifier(ExternalData parentDataset)
    {
        final Sample sample = getSample(parentDataset);
        final String plateCode = sample.getCode();
        final Space space = sample.getSpace();
        final String spaceCodeOrNull = (space != null) ? space.getCode() : null;
        return new PlateIdentifier(plateCode, spaceCodeOrNull, sample.getPermId());
    }

    protected ExperimentIdentifier createExperimentIdentifier(ExternalData parentDataset)
    {
        return asExperimentIdentifier(parentDataset.getExperiment());
    }

    private static ExperimentIdentifier asExperimentIdentifier(Experiment experiment)
    {
        final ExperimentIdentifier experimentId =
                new ExperimentIdentifier(experiment.getCode(), experiment.getProject().getCode(),
                        experiment.getProject().getSpace().getCode(), experiment.getPermId());
        return experimentId;
    }

    protected Geometry extractPlateGeometry(ExternalData dataSet)
    {
        Sample sample = getSample(dataSet);
        List<IEntityProperty> properties = sample.getProperties();
        for (IEntityProperty property : properties)
        {
            PropertyType propertyType = property.getPropertyType();
            if (propertyType.getCode().equals(ScreeningConstants.PLATE_GEOMETRY))
            {
                final String code = property.getVocabularyTerm().getCode();
                try
                {
                    return Geometry.createFromPlateGeometryString(code);
                } catch (IllegalArgumentException ex)
                {
                    throw new UserFailureException("Invalid property "
                            + ScreeningConstants.PLATE_GEOMETRY + ": " + code);
                }

            }
        }
        throw new UserFailureException("Sample '" + sample.getIdentifier() + "' has no property "
                + ScreeningConstants.PLATE_GEOMETRY);
    }

    private Sample getSample(ExternalData dataset)
    {
        // Sample may be NULL even though the selector does not begin with try
        Sample sample = dataset.getSample();
        assert sample != null : "dataset not connected to a sample: " + dataset;

        // The dataset's reference to the sample is not complete, get the one from the map
        return samplesById.get(sample.getId());
    }

    private String[] extractSampleCodes()
    {
        ArrayList<String> sampleCodes = new ArrayList<String>();
        for (PlateIdentifier plate : plates)
        {
            if (plate.getPlateCode() != null && plate.getPermId() == null)
            {
                sampleCodes.add(plate.getPlateCode());
            }
        }

        String[] sampleCodesArray = new String[sampleCodes.size()];
        sampleCodes.toArray(sampleCodesArray);

        return sampleCodesArray;
    }

    private String[] extractPermIds()
    {
        ArrayList<String> samplePermIds = new ArrayList<String>();
        for (PlateIdentifier plate : plates)
        {
            if (plate.getPermId() != null)
            {
                samplePermIds.add(plate.getPermId());
            }
        }

        String[] sampleCodesArray = new String[samplePermIds.size()];
        samplePermIds.toArray(sampleCodesArray);

        return sampleCodesArray;
    }

    private List<Long> extractSampleIds()
    {
        ArrayList<Long> sampleIds = new ArrayList<Long>(samples.size());
        for (Sample sample : samples)
        {
            sampleIds.add(sample.getId());
        }
        return sampleIds;
    }

    protected String getDataStoreUrlFromDataStore(DataStore dataStore)
    {
        String datastoreUrl = dataStore.getDownloadUrl();
        // The url objained form a DataStore object is the *download* url. Convert this to the
        // datastore URL
        if (datastoreUrl.endsWith(DATA_STORE_SERVER_WEB_APPLICATION_NAME))
        {
            datastoreUrl =
                    datastoreUrl.substring(0, datastoreUrl.length()
                            - DATA_STORE_SERVER_WEB_APPLICATION_NAME.length());
        }
        if (datastoreUrl.endsWith("/"))
        {
            datastoreUrl = datastoreUrl.substring(0, datastoreUrl.length() - 1);
        }

        return datastoreUrl;
    }

    protected static SampleIdentifier createSampleIdentifier(PlateIdentifier plate,
            String homeSpaceCodeOrNull)
    {
        SampleOwnerIdentifier owner;
        String spaceCode = plate.tryGetSpaceCode();
        if (StringUtils.isNotBlank(spaceCode))
        {
            final SpaceIdentifier space =
                    new SpaceIdentifier(DatabaseInstanceIdentifier.HOME, spaceCode);
            owner = new SampleOwnerIdentifier(space);
        } else
        {
            if (spaceCode == null)
            {
                owner = new SampleOwnerIdentifier(DatabaseInstanceIdentifier.createHome());
            } else
            {
                if (homeSpaceCodeOrNull == null)
                {
                    throw new UserFailureException("No space given and user has no home space.");
                }
                final SpaceIdentifier space =
                        new SpaceIdentifier(DatabaseInstanceIdentifier.HOME, homeSpaceCodeOrNull);
                owner = new SampleOwnerIdentifier(space);
            }
        }
        return SampleIdentifier.createOwnedBy(owner, plate.getPlateCode());
    }

    protected static SampleIdentifier createSampleIdentifier(Sample sample)
    {
        SampleOwnerIdentifier owner;
        Space spaceOrNull = sample.getSpace();
        String spaceCode = (null == spaceOrNull) ? null : spaceOrNull.getCode();
        if (spaceCode != null)
        {
            SpaceIdentifier space = new SpaceIdentifier(DatabaseInstanceIdentifier.HOME, spaceCode);
            owner = new SampleOwnerIdentifier(space);
        } else
        {
            owner = new SampleOwnerIdentifier(DatabaseInstanceIdentifier.createHome());
        }
        return SampleIdentifier.createOwnedBy(owner, sample.getCode());
    }
}
