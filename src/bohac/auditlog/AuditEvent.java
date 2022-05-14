package bohac.auditlog;

import bohac.entity.User;

import java.time.LocalDateTime;

public interface AuditEvent extends Comparable<AuditEvent> {
    enum Type {
        CREATION, MODIFICATION, ACCESS, CLOSURE
    }

    User getUser();

    LocalDateTime getDateTime();

    Type getType();

    @Override
    default int compareTo(AuditEvent o) {
        return getDateTime().compareTo(o.getDateTime());
    }

}
