package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.regex.Pattern;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

public class MatchingContentCopy implements IMatchingEntity
{

    private String externalCode;

    private String path;

    @SuppressWarnings("unused")
    private String gitCommitHash;

    @SuppressWarnings("unused")
    private String gitRepositoryId;

    private ExternalDataManagementSystemPE externalDms;


    public MatchingContentCopy(String externalCode, String path, String gitCommitHash, String gitRepositoryId, ExternalDataManagementSystemPE externalDms)
    {
        this.externalCode = externalCode;
        this.path = path;
        this.gitCommitHash = gitCommitHash;
        this.gitRepositoryId = gitRepositoryId;
        this.externalDms = externalDms;
    }

    @Override
    public String toString()
    {
        if (externalDms.getAddressType().equals(ExternalDataManagementSystemType.FILE_SYSTEM))
        {
            return externalDms.getAddress() + path;
        } else
        {
            return externalDms.getAddress().replaceAll(Pattern.quote("${") + ".*" + Pattern.quote("}"), externalCode);
        }
    }

    @Override
    public EntityTypePE getEntityType()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EntityKind getEntityKind()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getCode()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPermId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getIdentifier()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PersonPE getRegistrator()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
