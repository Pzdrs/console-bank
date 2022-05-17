package bohac.auditlog.events;

import bohac.auditlog.AuditEvent;

public class AccessAuditEvent extends GenericAuditEvent {
    public AccessAuditEvent(AuditEvent auditEvent) {
        super(auditEvent);
    }

    @Override
    public String toString() {
        return String.format("%s at %s", getUser().getUsername(), getDateTime());
    }
}
