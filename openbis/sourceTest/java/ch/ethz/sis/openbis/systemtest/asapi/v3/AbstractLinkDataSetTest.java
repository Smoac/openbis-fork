package ch.ethz.sis.openbis.systemtest.asapi.v3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.ContentCopyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.LinkedDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.LinkedDataFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDmsAddressType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

public abstract class AbstractLinkDataSetTest extends AbstractExternalDmsTest
{

    private IEntityTypeId linkDataSetType;

    private ExperimentPermId experiment;

    @BeforeClass
    protected void createData()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetTypeCreation datasetTypeCreation = new DataSetTypeCreation();
        datasetTypeCreation.setCode("linked-" + uuid());
        datasetTypeCreation.setKind(DataSetKind.LINK);

        this.linkDataSetType = v3api.createDataSetTypes(sessionToken, Collections.singletonList(datasetTypeCreation)).get(0);

        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode("space-" + uuid());
        SpacePermId space = v3api.createSpaces(sessionToken, Collections.singletonList(spaceCreation)).get(0);

        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setCode("project-" + uuid());
        projectCreation.setSpaceId(space);
        ProjectPermId project = v3api.createProjects(sessionToken, Collections.singletonList(projectCreation)).get(0);

        ExperimentTypeCreation experimentTypeCreation = new ExperimentTypeCreation();
        experimentTypeCreation.setCode("experiment-type-" + uuid());
        EntityTypePermId experimentType = v3api.createExperimentTypes(sessionToken, Collections.singletonList(experimentTypeCreation)).get(0);

        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode("experiment-" + uuid());
        experimentCreation.setTypeId(experimentType);
        experimentCreation.setProjectId(project);

        this.experiment = v3api.createExperiments(sessionToken, Collections.singletonList(experimentCreation)).get(0);

    }

    protected DataSetPermId create(LinkDataCreationBuilder creation)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        List<DataSetPermId> ids = v3api.createDataSets(sessionToken, Collections.singletonList(creation.build()));
        return ids.get(0);
    }

    protected DataSet get(DataSetPermId id)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetFetchOptions fo = new DataSetFetchOptions();
        LinkedDataFetchOptions lfo = new LinkedDataFetchOptions();
        lfo.withExternalDms();
        fo.withLinkedDataUsing(lfo);
        Map<IDataSetId, DataSet> result = v3api.getDataSets(sessionToken, Collections.singletonList(id), fo);
        return result.get(id);
    }

    protected ContentCopyCreationBuilder copyAt(ExternalDmsPermId edmsId)
    {
        return new ContentCopyCreationBuilder(edmsId);
    }

    protected LinkDataCreationBuilder linkDataSet()
    {
        return new LinkDataCreationBuilder();
    }

    protected class ContentCopyCreationBuilder
    {
        private ExternalDmsPermId edmsId;

        private ExternalDmsAddressType type;

        private String externalCode;

        private String path;

        private String gitCommitHash;

        public ContentCopyCreationBuilder(ExternalDmsPermId edmsId)
        {
            if (edmsId != null)
            {
                this.edmsId = edmsId;
                this.type = get(edmsId).getAddressType();
                switch (type)
                {
                    case OPENBIS:
                    case URL:
                        externalCode = uuid();
                        break;
                    case FILE_SYSTEM:
                }
            }
        }

        public ContentCopyCreationBuilder withExternalCode(String externalCode)
        {
            this.externalCode = externalCode;
            return this;
        }

        public ContentCopyCreationBuilder withPath(String path)
        {
            this.path = path;
            return this;
        }

        public ContentCopyCreationBuilder withGitCommitHash(String gitCommitHash)
        {
            this.gitCommitHash = gitCommitHash;
            return this;
        }

        public ContentCopyCreation build()
        {
            ContentCopyCreation creation = new ContentCopyCreation();
            creation.setExternalDmsId(edmsId);
            creation.setExternalId(externalCode);
            creation.setPath(path);
            creation.setGitCommitHash(gitCommitHash);
            return creation;
        }
    }

    protected class LinkDataCreationBuilder
    {

        private List<ContentCopyCreation> copies;

        private String externalCode;

        private IExternalDmsId edms;

        public LinkDataCreationBuilder with(ContentCopyCreationBuilder copy)
        {
            return with(copy.build());
        }

        public LinkDataCreationBuilder with(ContentCopyCreation copy)
        {
            if (copies == null)
            {
                copies = new ArrayList<>();
            }
            copies.add(copy);
            return this;
        }

        public LinkDataCreationBuilder withExternalCode(String externalCode)
        {
            this.externalCode = externalCode;
            return this;
        }

        public LinkDataCreationBuilder withExternalDms(IExternalDmsId externalDms)
        {
            this.edms = externalDms;
            return this;
        }

        public DataSetCreation build()
        {
            LinkedDataCreation linkData = new LinkedDataCreation();
            linkData.setContentCopies(copies);
            linkData.setExternalCode(externalCode);
            linkData.setExternalDmsId(edms);
            DataSetCreation creation = new DataSetCreation();
            creation.setLinkedData(linkData);
            creation.setTypeId(linkDataSetType);
            creation.setExperimentId(experiment);
            creation.setAutoGeneratedCode(true);
            creation.setDataStoreId(new DataStorePermId("STANDARD"));
            return creation;
        }
    }
}
