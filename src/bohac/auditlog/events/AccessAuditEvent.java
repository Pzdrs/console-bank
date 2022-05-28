package bohac.auditlog.events;

import bohac.auditlog.AuditEvent;
import bohac.entity.User;
import bohac.ui.TerminalSession;
import bohac.util.Utils;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.Map;

public class AccessAuditEvent extends GenericAuditEvent {
    public AccessAuditEvent(AuditEvent auditEvent) {
        super(auditEvent);
    }

    public AccessAuditEvent(User user) {
        super(user, LocalDateTime.now(), Type.ACCESS);
    }

    public String toStringShort() {
        return TerminalSession.LANGUAGE_MANAGER.getString("account_accessed_at_short", Map.of(
                "user", getUser().getUsername(),
                "time", Utils.localizedDateTime(getDateTime(), FormatStyle.MEDIUM)
        ));
    }

    @Override
    public String toString() {
        return super.toString() + TerminalSession.LANGUAGE_MANAGER
                .getString("account_accessed_at", "time", Utils.localizedDateTime(getDateTime(), FormatStyle.SHORT));
    }
}
