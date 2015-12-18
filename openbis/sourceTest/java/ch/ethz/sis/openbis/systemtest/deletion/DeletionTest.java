package ch.ethz.sis.openbis.systemtest.deletion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Transformer;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.common.update.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.create.PhysicalDataCreation;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.id.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.id.ProprietaryStorageFormatPermId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.id.RelativeLocationLocatorTypePermId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.material.create.MaterialCreation;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.material.delete.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.material.update.MaterialUpdate;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.project.delete.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.space.delete.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.systemtest.api.v3.AbstractTest;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;

public abstract class DeletionTest extends AbstractTest
{

    {
        System.setProperty("rebuild-index", "false");
    }

    public static void newTx()
    {
        TestTransaction.end();
        TestTransaction.start();
    }

    @Autowired
    protected SessionFactory sessionFactory;

    protected String sessionToken;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public Set<String> set(String... values)
    {
        return new HashSet<String>(Arrays.asList(values));
    }

    @BeforeClass
    public void setSessionToken()
    {
        sessionToken = v3api.login(TEST_USER, PASSWORD);
    }

    @SuppressWarnings("unchecked")
    public <T extends IProjectId> void delete(T... ids)
    {
        ProjectDeletionOptions deletionOptions = new ProjectDeletionOptions();
        deletionOptions.setReason("reason");
        v3api.deleteProjects(sessionToken, Arrays.asList(ids), deletionOptions);
    }

    @SuppressWarnings("unchecked")
    public <T extends IExperimentId> void delete(T... ids)
    {
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("reason");
        IDeletionId delId = v3api.deleteExperiments(sessionToken, Arrays.asList(ids), deletionOptions);
        v3api.confirmDeletions(sessionToken, Collections.singletonList(delId));
    }

    @SuppressWarnings("unchecked")
    public <T extends ISampleId> void delete(T... ids)
    {
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("reason");
        IDeletionId delId = v3api.deleteSamples(sessionToken, Arrays.asList(ids), deletionOptions);
        v3api.confirmDeletions(sessionToken, Collections.singletonList(delId));
    }

    @SuppressWarnings("unchecked")
    public <T extends ISpaceId> void delete(T... ids)
    {
        SpaceDeletionOptions deletionOptions = new SpaceDeletionOptions();
        deletionOptions.setReason("reason");
        v3api.deleteSpaces(sessionToken, Arrays.asList(ids), deletionOptions);
    }

    @SuppressWarnings("unchecked")
    public <T extends IMaterialId> void delete(T... ids)
    {
        MaterialDeletionOptions deletionOptions = new MaterialDeletionOptions();
        deletionOptions.setReason("reason");
        v3api.deleteMaterials(sessionToken, Arrays.asList(ids), deletionOptions);
    }

    @SuppressWarnings("unchecked")
    public <T extends IDataSetId> void delete(T... ids)
    {
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("reason");
        IDeletionId delId = v3api.deleteDataSets(sessionToken, Arrays.asList(ids), deletionOptions);
        v3api.confirmDeletions(sessionToken, Collections.singletonList(delId));
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final void assertHistory(String permId, final String key, String... expected) throws Exception
    {
        List<Set<String>> values = new ArrayList<>();
        for (String e : expected)
        {
            if (e == "")
            {
                values.add(set());
            } else
            {
                values.add(set(e));
            }
        }

        assertHistory(permId, key, values.toArray(new Set[0]));
    }

    @SafeVarargs
    public final void assertHistory(String permId, final String key, Set<String>... expected) throws Exception
    {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("SELECT e FROM EventPE e WHERE :permId IN e.identifiersInternal").setParameter("permId", permId);
        EventPE event = (EventPE) query.uniqueResult();

        JsonNode tree = new ObjectMapper().reader().readTree(event.getContent());

        List<Modification> allMods =
                IteratorUtils.toList(IteratorUtils.transformedIterator(tree.get(permId).elements(), new JsonNodeToModificationTransformer()));

        List<Modification> mods = new ArrayList<>();
        for (Modification mod : allMods)
        {
            if (mod.key.equals(key))
            {
                mods.add(mod);
            }
        }

        List<Change> changes = new ArrayList<>();
        for (Modification mod : mods)
        {
            changes.add(new Change(mod.validFrom, mod.value, false));
            if (mod.validUntil != null)
            {
                changes.add(new Change(mod.validUntil, mod.value, true));
            }
        }

        Collections.sort(changes);

        List<Set<String>> actualValues = new ArrayList<>();

        Date currentTime = changes.get(0).time;
        Set<String> currentSet = new HashSet<>();
        for (Change change : changes)
        {
            if (currentTime.equals(change.time) == false)
            {
                actualValues.add(currentSet);
                currentSet = new HashSet<>(currentSet);
                currentTime = change.time;
            }
            if (change.isRemoval)
            {
                if (!currentSet.remove(change.value))
                {
                    throw new IllegalStateException("Removing " + change + " from empty set");
                }
            } else
            {
                currentSet.add(change.value);
            }
        }

        actualValues.add(currentSet);

        List<Set<String>> expectedList = Arrays.asList(expected);

        if (expectedList.equals(actualValues) == false)
        {
            Assert.assertEquals(actualValues, expectedList,
                    "Property " + key + " of entity " + permId + " has wrong value history. Expected " + expectedList + ", actual " + actualValues);
        }
    }

    private class JsonNodeToModificationTransformer implements Transformer<JsonNode, Modification>
    {
        @Override
        public Modification transform(JsonNode node)
        {
            try
            {
                Modification m = new Modification();
                m.key = node.get("key").textValue();
                m.value = node.get("value").textValue();
                m.validFrom = dateFormat.parse(node.get("validFrom").textValue());
                String validUntil = node.get("validUntil").textValue();
                if (validUntil != null && validUntil.length() > 0)
                {
                    m.validUntil = dateFormat.parse(validUntil);
                }
                return m;
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private static class Modification
    {
        public String key;

        public String value;

        public Date validFrom;

        public Date validUntil;
    }

    public Map<String, String> props(String... keyvals)
    {
        Map<String, String> props = new HashMap<String, String>();
        for (int i = 0; i < keyvals.length; i += 2)
        {
            String key = keyvals[i];
            String val = keyvals[i + 1];
            props.put(key, val);
        }
        return props;
    }

    protected SamplePermId createSample(IExperimentId experiment, ISpaceId space, String code, String... properties)
    {
        SampleCreation sample = new SampleCreation();
        sample.setCode(code);
        sample.setExperimentId(experiment);
        sample.setSpaceId(space);
        sample.setTypeId(new EntityTypePermId("DELETION_TEST"));

        for (Map.Entry<String, String> entry : props(properties).entrySet())
        {
            sample.setProperty(entry.getKey(), entry.getValue());
        }

        return v3api.createSamples(sessionToken, Arrays.asList(sample)).get(0);
    }

    protected DataSetPermId createDataSet(IExperimentId experiment, String code, String... properties)
    {
        DataSetCreation creation = new DataSetCreation();
        creation.setExperimentId(experiment);
        creation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        creation.setCode(code + "-" + UUID.randomUUID().toString());
        creation.setDataStoreId(new DataStorePermId("STANDARD"));

        PhysicalDataCreation data = new PhysicalDataCreation();
        data.setLocation("test/location/" + code);
        data.setFileFormatTypeId(new FileFormatTypePermId("TIFF"));
        data.setLocatorTypeId(new RelativeLocationLocatorTypePermId());
        data.setStorageFormatId(new ProprietaryStorageFormatPermId());
        creation.setPhysicalData(data);

        for (Map.Entry<String, String> entry : props(properties).entrySet())
        {
            creation.setProperty(entry.getKey(), entry.getValue());
        }
        return v3api.createDataSets(sessionToken, Arrays.asList(creation)).get(0);
    }

    protected DataSetPermId createContainerDataSet(IExperimentId experiment, String code, String... properties)
    {
        DataSetCreation creation = new DataSetCreation();
        creation.setExperimentId(experiment);
        creation.setTypeId(new EntityTypePermId("DELETION_TEST_CONTAINER"));
        creation.setCode(code + "-" + UUID.randomUUID().toString());
        creation.setDataStoreId(new DataStorePermId("STANDARD"));

        for (Map.Entry<String, String> entry : props(properties).entrySet())
        {
            creation.setProperty(entry.getKey(), entry.getValue());
        }
        return v3api.createDataSets(sessionToken, Arrays.asList(creation)).get(0);
    }

    protected ExperimentPermId createExperiment(IProjectId projectId, String code, String... properties)
    {
        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode(code);
        experimentCreation.setProjectId(projectId);
        experimentCreation.setTypeId(new EntityTypePermId("DELETION_TEST"));

        for (Map.Entry<String, String> entry : props(properties).entrySet())
        {
            experimentCreation.setProperty(entry.getKey(), entry.getValue());
        }

        return v3api.createExperiments(sessionToken, Arrays.asList(experimentCreation)).get(0);
    }

    protected SpacePermId createSpace(String code)
    {
        SpaceCreation space = new SpaceCreation();
        space.setCode(code);
        return v3api.createSpaces(sessionToken, Arrays.asList(space)).get(0);
    }

    protected ProjectPermId createProject(SpacePermId space, String code)
    {
        ProjectCreation project = new ProjectCreation();
        project.setCode(code);
        project.setSpaceId(space);
        return v3api.createProjects(sessionToken, Arrays.asList(project)).get(0);
    }

    protected MaterialPermId createMaterial(String code, Map<String, String> properties)
    {
        MaterialCreation material = new MaterialCreation();
        material.setCode(code);
        material.setTypeId(new EntityTypePermId("DELETION_TEST"));

        for (Map.Entry<String, String> entry : properties.entrySet())
        {
            material.setProperty(entry.getKey(), entry.getValue());
        }

        return v3api.createMaterials(sessionToken, Arrays.asList(material)).get(0);
    }

    protected void updateChildren(SamplePermId sample, SamplePermId... children)
    {
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sample);
        IdListUpdateValue<ISampleId> upd = new IdListUpdateValue<ISampleId>();
        upd.set(children);
        update.setChildActions(upd.getActions());
        v3api.updateSamples(sessionToken, Arrays.asList(update));
    }

    protected void updateParents(SamplePermId sample, SamplePermId... parents)
    {
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sample);
        IdListUpdateValue<ISampleId> upd = new IdListUpdateValue<ISampleId>();
        upd.set(parents);
        update.setParentActions(upd.getActions());
        v3api.updateSamples(sessionToken, Arrays.asList(update));
    }

    protected void updateComponents(SamplePermId sample, SamplePermId... components)
    {
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sample);
        IdListUpdateValue<ISampleId> upd = new IdListUpdateValue<ISampleId>();
        upd.set(components);
        update.setComponentActions(upd.getActions());
        v3api.updateSamples(sessionToken, Arrays.asList(update));
    }

    protected void updateContainer(SamplePermId sample, SamplePermId... containers)
    {
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sample);
        if (containers.length == 0)
        {
            update.setContainerId(null);
        } else if (containers.length == 1)
        {
            update.setContainerId(containers[0]);
        } else
        {
            throw new IllegalArgumentException("multiple containers not allowed");
        }
        v3api.updateSamples(sessionToken, Arrays.asList(update));
    }

    protected void updateChildren(DataSetPermId dataSet, IDataSetId... children)
    {
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSet);
        IdListUpdateValue<IDataSetId> upd = new IdListUpdateValue<IDataSetId>();
        upd.set(children);
        update.setChildActions(upd.getActions());
        v3api.updateDataSets(sessionToken, Arrays.asList(update));
    }

    protected void updateParents(DataSetPermId dataSet, DataSetPermId... parents)
    {
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSet);
        IdListUpdateValue<IDataSetId> upd = new IdListUpdateValue<IDataSetId>();
        upd.set(parents);
        update.setParentActions(upd.getActions());
        v3api.updateDataSets(sessionToken, Arrays.asList(update));
    }

    protected void updateComponents(DataSetPermId dataSet, DataSetPermId... components)
    {
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSet);
        IdListUpdateValue<IDataSetId> upd = new IdListUpdateValue<IDataSetId>();
        upd.set(components);
        update.setComponentActions(upd.getActions());
        v3api.updateDataSets(sessionToken, Arrays.asList(update));
    }

    protected void updateContainers(DataSetPermId dataSet, DataSetPermId... components)
    {
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSet);
        IdListUpdateValue<IDataSetId> upd = new IdListUpdateValue<IDataSetId>();
        upd.set(components);
        update.setContainerActions(upd.getActions());
        v3api.updateDataSets(sessionToken, Arrays.asList(update));
    }

    protected void setProperties(IMaterialId sample, String... properties)
    {
        MaterialUpdate update = new MaterialUpdate();
        update.setMaterialId(sample);
        for (Map.Entry<String, String> entry : props(properties).entrySet())
        {
            update.setProperty(entry.getKey(), entry.getValue());
        }
        v3api.updateMaterials(sessionToken, Arrays.asList(update));
    }

    protected void setProperties(IExperimentId sample, String... properties)
    {
        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(sample);
        for (Map.Entry<String, String> entry : props(properties).entrySet())
        {
            update.setProperty(entry.getKey(), entry.getValue());
        }
        v3api.updateExperiments(sessionToken, Arrays.asList(update));
    }

    protected void setProperties(ISampleId sample, String... properties)
    {
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sample);
        for (Map.Entry<String, String> entry : props(properties).entrySet())
        {
            update.setProperty(entry.getKey(), entry.getValue());
        }
        v3api.updateSamples(sessionToken, Arrays.asList(update));
    }

    protected void setProperties(IDataSetId dataSet, String... properties)
    {
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSet);
        for (Map.Entry<String, String> entry : props(properties).entrySet())
        {
            update.setProperty(entry.getKey(), entry.getValue());
        }
        v3api.updateDataSets(sessionToken, Arrays.asList(update));
    }
}
