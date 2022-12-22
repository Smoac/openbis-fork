package ch.ethz.sis.afsserver.worker.proxy;

import ch.ethz.sis.afsapi.api.dto.File;
import ch.ethz.sis.afsserver.exception.FsExceptions;
import ch.ethz.sis.afsserver.worker.AbstractProxy;
import lombok.NonNull;

import java.util.List;

public class ValidationProxy extends AbstractProxy {

    private int maxReadSizeInBytes;

    public ValidationProxy(AbstractProxy nextProxy, int maxReadSizeInBytes) {
        super(nextProxy);
        this.maxReadSizeInBytes = maxReadSizeInBytes;
    }


    @Override
    public @NonNull List<File> list(@NonNull String owner, @NonNull String source, @NonNull Boolean recursively) throws Exception {
        return nextProxy.list(owner, source, recursively);
    }

    @Override
    public byte @NonNull [] read(@NonNull String owner, @NonNull String source, @NonNull Long offset, @NonNull Integer limit) throws Exception {
        validateReadSize(source, limit);
        return nextProxy.read(owner, source, offset, limit);
    }

    @Override
    public @NonNull Boolean write(@NonNull String owner, @NonNull String source, @NonNull Long offset, byte @NonNull [] data, byte @NonNull [] md5Hash) throws Exception {
        return nextProxy.write(owner, source, offset, data, md5Hash);
    }

    @Override
    public @NonNull Boolean delete(@NonNull String owner, @NonNull String source) throws Exception {
        return nextProxy.delete(owner, source);
    }

    @Override
    public @NonNull Boolean copy(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner, @NonNull String target) throws Exception {
        return nextProxy.copy(sourceOwner, source, targetOwner, target);
    }

    @Override
    public @NonNull Boolean move(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner, @NonNull String target) throws Exception {
        return nextProxy.move(sourceOwner, source, targetOwner, target);
    }

    private void validateReadSize(String source, Integer limit) {
        if (limit > maxReadSizeInBytes) {
            throw FsExceptions.MAX_READ_SIZE_EXCEEDED.getInstance(workerContext.getSessionToken(), limit, source, maxReadSizeInBytes);
        }
    }
}
