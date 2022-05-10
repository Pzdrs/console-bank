package bohac.auditlog;

import bohac.User;

import java.time.LocalDateTime;

public interface AuditEvent extends Comparable<AuditEvent> {
    enum Type {
        CREATION, MODIFICATION, ACCESS, CLOSURE
    }

    User getUser();

    LocalDateTime getDateTime();

    Type getType();
}
