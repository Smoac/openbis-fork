package ch.ethz.sis.afsserver.worker.proxy;

import ch.ethz.sis.afsapi.api.dto.File;
import ch.ethz.sis.afsserver.worker.AbstractProxy;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.Logger;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

public class LogProxy extends AbstractProxy {

    private static final Logger logger = LogManager.getLogger(LogProxy.class);

    public LogProxy(AbstractProxy nextProxy) {
        super(nextProxy);
    }

    @Override
    public void begin(UUID transactionId) throws Exception {
        logger.traceAccess(null);
        nextProxy.begin(transactionId);
        logger.traceExit(null);
    }

    @Override
    public Boolean prepare() throws Exception {
        logger.traceAccess(null);
        return logger.traceExit(nextProxy.prepare());
    }

    @Override
    public void commit() throws Exception {
        logger.traceAccess(null);
        nextProxy.commit();
        logger.traceExit(null);
    }

    @Override
    public void rollback() throws Exception {
        logger.traceAccess(null);
        nextProxy.rollback();
        logger.traceExit(null);
    }

    @Override
    public List<UUID> recover() throws Exception {
        logger.traceAccess(null);
        return logger.traceExit(nextProxy.recover());
    }

    @Override
    public @NonNull List<File> list(@NonNull String owner, @NonNull String source, @NonNull Boolean recursively) throws Exception {
        logger.traceAccess(null, owner, source, recursively);
        return logger.traceExit(nextProxy.list(owner, source, recursively));
    }

    @Override
    public byte @NonNull [] read(@NonNull String owner, @NonNull String source, @NonNull Long offset, @NonNull Integer limit) throws Exception {
        logger.traceAccess(null, owner, source, offset, limit);
        return logger.traceExit(nextProxy.read(owner, source, offset, limit));
    }

    @Override
    public @NonNull Boolean write(@NonNull String owner, @NonNull String source, @NonNull Long offset, @NonNull byte[] data, @NonNull byte[] md5Hash) throws Exception {
        logger.traceAccess(null, owner, source, offset, data.length, md5Hash.length);
        return logger.traceExit(nextProxy.write(owner, source, offset, data, md5Hash));
    }

    @Override
    public @NonNull Boolean delete(@NonNull String owner, @NonNull String source) throws Exception {
        logger.traceAccess(null, owner, source);
        return logger.traceExit(nextProxy.delete(owner, source));
    }

    @Override
    public @NonNull Boolean copy(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner, @NonNull String target) throws Exception {
        logger.traceAccess(null, sourceOwner, source, targetOwner, target);
        return logger.traceExit(nextProxy.copy(sourceOwner, source, targetOwner, target));
    }

    @Override
    public @NonNull Boolean move(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner, @NonNull String target) throws Exception {
        logger.traceAccess(null, sourceOwner, source, targetOwner, target);
        return logger.traceExit(nextProxy.move(sourceOwner, source, targetOwner, target));
    }

}
