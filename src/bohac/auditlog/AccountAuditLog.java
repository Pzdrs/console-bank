package bohac.auditlog;

import bohac.auditlog.events.AccessAuditEvent;
import bohac.storage.JSONSerializable;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Optional;

public record AccountAuditLog(AuditEventList eventList) implements Iterable<AuditEvent>, JSONSerializable {
    public AccountAuditLog() {
        this(new AuditEventList());
    }

    public AuditEvent getLatestEvent() {
        Optional<AuditEvent> first = eventList.stream().sorted().findFirst();
        return first.orElse(null);
    }

    public void addEvent(AuditEvent event) {
        eventList.add(event);
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject();
    }

    public AccessAuditEvent getLastAccess() {
        return (AccessAuditEvent) eventList.stream()
                .filter(auditEvent -> auditEvent instanceof AccessAuditEvent).max(AuditEvent::compareTo)
                .orElse(null);
    }

    @Override
    public Iterator<AuditEvent> iterator() {
        return eventList.iterator();
    }
}
