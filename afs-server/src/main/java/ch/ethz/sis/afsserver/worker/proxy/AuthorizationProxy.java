package ch.ethz.sis.afsserver.worker.proxy;

import ch.ethz.sis.afsapi.api.dto.File;
import ch.ethz.sis.afs.dto.operation.OperationName;
import ch.ethz.sis.afsserver.exception.FSExceptions;
import ch.ethz.sis.afsserver.worker.AbstractProxy;
import ch.ethz.sis.afsserver.worker.providers.AuthorizationInfoProvider;
import ch.ethz.sis.shared.io.FilePermission;
import ch.ethz.sis.shared.io.IOUtils;
import lombok.NonNull;

import java.util.List;
import java.util.Set;

public class AuthorizationProxy extends AbstractProxy {

    AuthorizationInfoProvider authorizationInfoProvider;

    public AuthorizationProxy(AbstractProxy nextProxy,
                              AuthorizationInfoProvider authorizationInfoProvider) {
        super(nextProxy);
        this.authorizationInfoProvider = authorizationInfoProvider;
    }

    private void validateUserRights(String owner, String source, Set<FilePermission> permissions, OperationName operationName) throws Exception {
        boolean doesSessionHaveRights = authorizationInfoProvider.doesSessionHaveRights(workerContext.getSessionToken(),
                owner,
                permissions);
        if (!doesSessionHaveRights) {
            throw FSExceptions.USER_NO_ACL_RIGHTS.getInstance(workerContext.getSessionToken(), permissions, owner, source, operationName);
        }
    }

    @Override
    public @NonNull List<File> list(@NonNull String owner, @NonNull String source, @NonNull Boolean recursively) throws Exception {
        validateUserRights(owner, source, IOUtils.readPermissions, OperationName.List);
        return nextProxy.list(owner, source, recursively);
    }

    @Override
    public byte @NonNull [] read(@NonNull String owner, @NonNull String source, @NonNull Long offset, @NonNull Integer limit) throws Exception {
        validateUserRights(owner, source, IOUtils.readPermissions, OperationName.Read);
        return nextProxy.read(owner, source, offset, limit);
    }

    @Override
    public @NonNull Boolean write(@NonNull String owner, @NonNull String source, @NonNull Long offset, byte @NonNull [] data, byte @NonNull [] md5Hash) throws Exception {
        validateUserRights(owner, source, IOUtils.writePermissions, OperationName.Write);
        return nextProxy.write(owner, source, offset, data, md5Hash);
    }

    @Override
    public @NonNull Boolean delete(@NonNull String owner, @NonNull String source) throws Exception {
        validateUserRights(owner, source, IOUtils.writePermissions, OperationName.Delete);
        return nextProxy.delete(owner, source);
    }

    @Override
    public @NonNull Boolean copy(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner, @NonNull String target) throws Exception {
        validateUserRights(sourceOwner, source, IOUtils.readPermissions, OperationName.Copy);
        validateUserRights(targetOwner, target, IOUtils.writePermissions, OperationName.Copy);
        return nextProxy.copy(sourceOwner, source, targetOwner, target);
    }

    @Override
    public @NonNull Boolean move(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner, @NonNull String target) throws Exception {
        validateUserRights(sourceOwner, source, IOUtils.readWritePermissions, OperationName.Move);
        validateUserRights(targetOwner, target, IOUtils.writePermissions, OperationName.Move);
        return nextProxy.move(sourceOwner, source, targetOwner, target);
    }
}
