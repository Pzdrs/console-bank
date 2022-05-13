package bohac.auditlog;

import java.util.Optional;

public record AccountAuditLog(AuditEventList eventList) {
    public AccountAuditLog() {
        this(new AuditEventList());
    }

    public AuditEvent getLatestEvent() {
        Optional<AuditEvent> first = eventList.stream().sorted().findFirst();
        if (first.isEmpty()) return null;
        return new GenericAuditEvent(first.get());
    }
}
