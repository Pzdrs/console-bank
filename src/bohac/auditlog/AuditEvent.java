package bohac.auditlog;

import bohac.entity.User;
import bohac.storage.JSONSerializable;

import java.time.LocalDateTime;
import java.util.Comparator;

/**
 * Represents a single audit event
 */
public interface AuditEvent extends Comparable<AuditEvent>, JSONSerializable {
    Comparator<AuditEvent> CHRONOLOGICAL = Comparator.comparing(AuditEvent::getDateTime).reversed();

    /**
     * Event type
     */
    enum Type {
        CREATION, MODIFICATION, ACCESS, CLOSURE
    }

    User getUser();

    LocalDateTime getDateTime();

    Type getType();

    @Override
    default int compareTo(AuditEvent o) {
        return CHRONOLOGICAL.compare(o, this);
    }
}
