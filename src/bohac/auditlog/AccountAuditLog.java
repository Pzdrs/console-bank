package bohac.auditlog;

import bohac.auditlog.events.AccessAuditEvent;
import bohac.storage.JSONSerializableArray;
import org.json.JSONArray;

import java.util.Iterator;
import java.util.Optional;

/**
 * Audit log wrapper
 *
 * @param eventList events
 */
public record AccountAuditLog(AuditEventList eventList) implements Iterable<AuditEvent>, JSONSerializableArray {
    public AccountAuditLog() {
        this(new AuditEventList());
    }

    /**
     * Add an even to the audit log
     *
     * @param event event
     */
    public void addEvent(AuditEvent event) {
        eventList.add(event);
    }

    /**
     * @return the last access event
     */
    public AccessAuditEvent getLastAccess() {
        return (AccessAuditEvent) eventList.stream()
                .filter(auditEvent -> auditEvent instanceof AccessAuditEvent).max(AuditEvent::compareTo)
                .orElse(null);
    }

    @Override
    public JSONArray toJSON() {
        JSONArray auditLog = new JSONArray();
        eventList.forEach(event -> auditLog.put(event.toJSON()));
        return auditLog;
    }

    @Override
    public Iterator<AuditEvent> iterator() {
        return eventList.iterator();
    }
}
