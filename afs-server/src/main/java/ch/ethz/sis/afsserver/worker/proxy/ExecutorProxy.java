package ch.ethz.sis.afsserver.worker.proxy;

import ch.ethz.sis.afsapi.api.dto.File;
import ch.ethz.sis.afsserver.worker.AbstractProxy;
import ch.ethz.sis.shared.io.IOUtils;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ExecutorProxy extends AbstractProxy {

    public ExecutorProxy() {
        super(null);
    }

    //
    // Transaction Management
    //

    @Override
    public void begin(UUID transactionId) throws Exception {
        workerContext.setTransactionId(transactionId);
        workerContext.getConnection().begin(transactionId);
    }

    @Override
    public Boolean prepare() throws Exception {
        return workerContext.getConnection().prepare();
    }

    @Override
    public void commit() throws Exception {
        workerContext.getConnection().commit();
    }

    @Override
    public void rollback() throws Exception {
        workerContext.getConnection().rollback();
    }

    @Override
    public List<UUID> recover() throws Exception {
        return workerContext.getConnection().recover();
    }

    //
    // File System Operations
    //

    public String getPath(String owner, String source) {
        return IOUtils.PATH_SEPARATOR + owner.toString() + source;
    }

    @Override
    public @NonNull List<File> list(@NonNull final String owner, @NonNull final String source,
            @NonNull final Boolean recursively)
            throws Exception {
        return workerContext.getConnection().list(getPath(owner, source), recursively).stream()
                .map(file -> new File(file.getPath(), file.getName(), file.getDirectory(), file.getSize(),
                        file.getLastModifiedTime(), file.getCreationTime(), file.getLastAccessTime()))
                .collect(Collectors.toList());
    }

    @Override
    @NonNull
    public byte[] read(@NonNull final String owner, @NonNull final String source, @NonNull final Long offset,
            @NonNull final Integer limit) throws Exception {
        return workerContext.getConnection().read(getPath(owner, source), offset, limit);
    }

    @Override
    @NonNull
    public Boolean write(@NonNull final String owner, @NonNull final String source, @NonNull final Long offset,
            final byte @NonNull [] data, final byte @NonNull [] md5Hash) throws Exception {
        return workerContext.getConnection().write(getPath(owner, source), offset, data, md5Hash);
    }

    @Override
    @NonNull
    public Boolean delete(@NonNull final String owner, @NonNull final String source) throws Exception {
        return workerContext.getConnection().delete(getPath(owner, source));
    }

    @Override
    @NonNull
    public Boolean copy(@NonNull final String sourceOwner, @NonNull final String source,
            @NonNull final String targetOwner, @NonNull final String target) throws Exception {
        return workerContext.getConnection().copy(getPath(sourceOwner, source), getPath(targetOwner, target));
    }

    @Override
    @NonNull
    public Boolean move(@NonNull final String sourceOwner, @NonNull final String source,
            @NonNull final String targetOwner, @NonNull final String target) throws Exception {
        return workerContext.getConnection().move(getPath(sourceOwner, source), getPath(targetOwner, target));
    }

}
