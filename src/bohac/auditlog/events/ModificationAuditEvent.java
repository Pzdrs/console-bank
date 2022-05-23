package bohac.auditlog.events;

import bohac.Bank;
import bohac.auditlog.AuditEvent;
import bohac.ui.TerminalSession;
import bohac.util.Utils;
import bohac.entity.User;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.UUID;

public class ModificationAuditEvent extends GenericAuditEvent {
    private final String message;

    public ModificationAuditEvent(User user, LocalDateTime dateTime, Type type, String message) {
        super(user, dateTime, type);
        this.message = message;
    }

    public ModificationAuditEvent(User user, String message) {
        super(user, LocalDateTime.now(), Type.MODIFICATION);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static ModificationAuditEvent load(JSONObject object) {
        return new ModificationAuditEvent(
                Bank.users.getByID(UUID.fromString(object.getString("user"))).orElse(null),
                Utils.parseEpoch(object.getLong("date_time")),
                Type.valueOf(object.getString("type")),
                object.getString("message")
        );
    }

    @Override
    public String toString() {
        return super.toString() + TerminalSession.languageManager.getString("account_modified_at", Map.of(
                "message", message,
                "time", DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(getDateTime())
        ));
    }

    @Override
    public JSONObject toJSON() {
        return super.toJSON().put("message", message);
    }
}
