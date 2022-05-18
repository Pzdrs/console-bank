package bohac.auditlog.events;

import bohac.auditlog.AuditEvent;
import bohac.ui.TerminalSession;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;

public class AccessAuditEvent extends GenericAuditEvent {
    public AccessAuditEvent(AuditEvent auditEvent) {
        super(auditEvent);
    }

    public String toStringShort() {
        return TerminalSession.languageManager.getString("account_accessed_at_short", Map.of(
                "user", getUser().getUsername(),
                "time", DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(getDateTime())
        ));
    }

    @Override
    public String toString() {
        return super.toString() + TerminalSession.languageManager
                .getString("account_accessed_at", "time", DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(getDateTime()));
    }
}
