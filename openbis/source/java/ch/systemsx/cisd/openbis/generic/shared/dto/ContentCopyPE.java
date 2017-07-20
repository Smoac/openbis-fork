package ch.systemsx.cisd.openbis.generic.shared.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.NotNull;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

@Entity
@Table(name = TableNames.CONTENT_COPIES_TABLE)
public class ContentCopyPE extends HibernateAbstractRegistrationHolder
{

    private static final long serialVersionUID = 1L;

    private Long id;

    private LinkDataPE dataSet;

    private ExternalDataManagementSystemPE externalDataManagementSystem;

    private LocationType locationType;

    private String externalCode;

    private String path;

    private String gitCommitHash;

    private String gitRepositoryId;
    
    @Id
    @SequenceGenerator(name = SequenceNames.CONTENT_COPY_SEQUENCE, sequenceName = SequenceNames.CONTENT_COPY_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.CONTENT_COPY_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.DATA_ID_COLUMN, updatable = true)
    public LinkDataPE getDataSet()
    {
        return dataSet;
    }

    public void setDataSet(LinkDataPE dataSet)
    {
        this.dataSet = dataSet;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.EXTERNAL_DATA_MANAGEMENT_SYSTEM_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.EXTERNAL_DATA_MANAGEMENT_SYSTEM_ID_COLUMN, updatable = true)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_EXTERNAL_DMS)
    public ExternalDataManagementSystemPE getExternalDataManagementSystem()
    {
        return externalDataManagementSystem;
    }

    public void setExternalDataManagementSystem(ExternalDataManagementSystemPE externalDataManagementSystem)
    {
        this.externalDataManagementSystem = externalDataManagementSystem;
    }

    @Column(name = ColumnNames.LOCATION_TYPE_COLUMN, nullable = false)
    @Enumerated(EnumType.STRING)
    public LocationType getLocationType()
    {
        return locationType;
    }

    public void setLocationType(LocationType locationType)
    {
        this.locationType = locationType;
    }

    @Column(name = ColumnNames.EXTERNAL_CODE_COLUMN)
    @Field(name = SearchFieldConstants.EXTERNAL_CODE, index = Index.YES, store = Store.YES)
    public String getExternalCode()
    {
        return externalCode;
    }

    public void setExternalCode(String externalCode)
    {
        this.externalCode = externalCode;
    }

    @Column(name = ColumnNames.PATH_COLUMN)
    @Field(name = SearchFieldConstants.PATH, index = Index.YES, store = Store.YES)
    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    @Column(name = ColumnNames.GIT_COMMIT_HASH_COLUMN)
    @Field(index = Index.YES, store = Store.YES, name = SearchFieldConstants.GIT_COMMIT_HASH)
    public String getGitCommitHash()
    {
        return gitCommitHash;
    }

    public void setGitCommitHash(String gitCommitHash)
    {
        this.gitCommitHash = gitCommitHash;
    }

    @Column(name = ColumnNames.GIT_REPOSITORY_ID_COLUMN)
    @Field(index = Index.YES, store = Store.YES, name = SearchFieldConstants.GIT_REPOSITORY_ID)
    public String getGitRepositoryId()
    {
        return gitRepositoryId;
    }

    public void setGitRepositoryId(String gitRepositoryId)
    {
        this.gitRepositoryId = gitRepositoryId;
    }

}
