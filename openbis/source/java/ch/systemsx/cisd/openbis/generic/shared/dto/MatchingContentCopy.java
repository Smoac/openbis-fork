package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.regex.Pattern;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;

public class MatchingContentCopy implements IRelatedEntity
{

    private String externalCode;

    private String path;

    @SuppressWarnings("unused")
    private String gitCommitHash;

    @SuppressWarnings("unused")
    private String gitRepositoryId;

    private ExternalDataManagementSystemPE externalDms;

    public MatchingContentCopy(String externalCode, String path, String gitCommitHash, String gitRepositoryId,
            ExternalDataManagementSystemPE externalDms)
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

}
