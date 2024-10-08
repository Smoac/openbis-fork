package ch.ethz.sis.afsserver.server.shuffling;

import java.util.List;
import java.util.UUID;

import ch.ethz.sis.afs.dto.Lock;

public interface ILockManager
{
    boolean lock(List<Lock<UUID, String>> locks);

    boolean unlock(List<Lock<UUID, String>> locks);
}
