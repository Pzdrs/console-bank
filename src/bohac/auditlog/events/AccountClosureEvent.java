package bohac.auditlog.events;

import bohac.auditlog.AuditEvent;
import bohac.entity.User;
import bohac.ui.TerminalSession;
import bohac.util.Utils;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;

/**
 * Represents an account closure event
 */
public class AccountClosureEvent extends GenericAuditEvent {
    public AccountClosureEvent(AuditEvent auditEvent) {
        super(auditEvent);
    }

    public AccountClosureEvent(User user) {
        super(user, LocalDateTime.now(), Type.CLOSURE);
    }

    @Override
    public String toString() {
        return super.toString() + TerminalSession.LANGUAGE_MANAGER
                .getString("account_closed", "time", Utils.localizedDateTime(getDateTime(), FormatStyle.SHORT));
    }
}
