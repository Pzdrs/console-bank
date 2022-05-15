package bohac.auditlog;

import bohac.storage.JSONSerializable;
import org.json.JSONObject;

import java.util.Optional;

public record AccountAuditLog(AuditEventList eventList) implements JSONSerializable {
    public AccountAuditLog() {
        this(new AuditEventList());
    }

    public AuditEvent getLatestEvent() {
        Optional<AuditEvent> first = eventList.stream().sorted().findFirst();
        if (first.isEmpty()) return null;
        return new GenericAuditEvent(first.get());
    }

    public void addEvent(AuditEvent event) {
        eventList.add(event);
    }

    @Override
    public JSONObject toJSON() {
        return new JSONObject();
    }
}
