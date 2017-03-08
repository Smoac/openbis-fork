package ch.ethz.sis.openbis.systemtest.asapi.v3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ContentCopy;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.ContentCopyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.LinkedDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.LinkedDataFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDmsAddressType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.delete.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.delete.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

public abstract class AbstractLinkDataSetTest extends AbstractExternalDmsTest
{

    private ExperimentPermId experiment;

    private SpacePermId space;

    private ProjectPermId project;

    private List<DataSetPermId> datasets = new ArrayList<DataSetPermId>();

    @BeforeClass
    protected void createData()
    {
        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode("space-" + uuid());
        space = v3api.createSpaces(session, Collections.singletonList(spaceCreation)).get(0);

        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setCode("project-" + uuid());
        projectCreation.setSpaceId(space);
        project = v3api.createProjects(session, Collections.singletonList(projectCreation)).get(0);

        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode("experiment-" + uuid());
        experimentCreation.setProperty("DESCRIPTION", "DESCRIPTION");
        experimentCreation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experimentCreation.setProjectId(project);
        experiment = v3api.createExperiments(session, Collections.singletonList(experimentCreation)).get(0);
    }

    @AfterClass
    protected void deleteData()
    {
        DataSetDeletionOptions dd = new DataSetDeletionOptions();
        dd.setReason("test");
        ExperimentDeletionOptions ed = new ExperimentDeletionOptions();
        ed.setReason("test");
        ProjectDeletionOptions pd = new ProjectDeletionOptions();
        pd.setReason("test");
        SpaceDeletionOptions sd = new SpaceDeletionOptions();
        sd.setReason("test");

        IDeletionId delDataSets = v3api.deleteDataSets(session, datasets, dd);
        IDeletionId delExperiments = v3api.deleteExperiments(session, Collections.singletonList(experiment), ed);
        v3api.confirmDeletions(session, Collections.singletonList(delDataSets));
        v3api.confirmDeletions(session, Collections.singletonList(delExperiments));
        v3api.deleteProjects(session, Collections.singletonList(project), pd);
        v3api.deleteSpaces(session, Collections.singletonList(space), sd);
    }

    protected DataSetPermId create(LinkDataCreationBuilder creation)
    {
        List<DataSetPermId> ids = v3api.createDataSets(session, Collections.singletonList(creation.build()));
        datasets.addAll(ids);
        return ids.get(0);
    }

    protected DataSet get(DataSetPermId id)
    {
        DataSetFetchOptions fo = new DataSetFetchOptions();
        LinkedDataFetchOptions lfo = new LinkedDataFetchOptions();
        lfo.withExternalDms();
        fo.withLinkedDataUsing(lfo);
        Map<IDataSetId, DataSet> result = v3api.getDataSets(session, Collections.singletonList(id), fo);
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

        private List<ContentCopyCreation> copies = new ArrayList<>();

        private String externalCode;

        private IExternalDmsId edms;

        public LinkDataCreationBuilder with(ContentCopyCreationBuilder copy1, ContentCopyCreationBuilder... rest)
        {
            LinkDataCreationBuilder result = with(copy1.build());
            for (ContentCopyCreationBuilder b : rest)
            {
                result = result.with(b.build());
            }
            return result;
        }

        public LinkDataCreationBuilder with(ContentCopyCreation first, ContentCopyCreation... rest)
        {
            copies.add(first);
            copies.addAll(Arrays.asList(rest));
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
            creation.setTypeId(new EntityTypePermId("LINK_TYPE"));
            creation.setExperimentId(experiment);
            creation.setAutoGeneratedCode(true);
            creation.setDataStoreId(new DataStorePermId("STANDARD"));
            return creation;
        }
    }

    protected String stringify(ContentCopyCreation c)
    {
        return c.getExternalId() + " / " + c.getPath() + " / " + c.getGitCommitHash() + " / " + c.getExternalDmsId();
    }

    protected String stringify(ContentCopy c)
    {
        return c.getExternalCode() + " / " + c.getPath() + " / " + c.getGitCommitHash() + " / " + c.getExternalDms().getCode();
    }
}
