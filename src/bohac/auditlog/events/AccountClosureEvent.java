package bohac.auditlog.events;

import bohac.auditlog.AuditEvent;
import bohac.entity.User;
import bohac.ui.TerminalSession;
import bohac.util.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class AccountClosureEvent extends GenericAuditEvent {
    public AccountClosureEvent(AuditEvent auditEvent) {
        super(auditEvent);
    }

    public AccountClosureEvent(User user) {
        super(user, LocalDateTime.now(), Type.CREATION);
    }

    @Override
    public String toString() {
        return super.toString() + TerminalSession.languageManager
                .getString("account_closed_event", "time", Utils.localizedDateTime(getDateTime(), FormatStyle.SHORT));
    }
}