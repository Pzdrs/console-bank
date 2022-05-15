package bohac.auditlog;

import bohac.Bank;
import bohac.util.Utils;
import bohac.entity.User;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.UUID;

public class ModificationAuditEvent implements AuditEvent {
    private final User user;
    private final LocalDateTime dateTime;
    private final Type type;

    private final String message;

    /**
     * Copy constructor
     *
     * @param auditEvent copy object
     */
    public ModificationAuditEvent(ModificationAuditEvent auditEvent) {
        this.user = auditEvent.getUser();
        this.dateTime = auditEvent.getDateTime();
        this.type = auditEvent.getType();
        this.message = auditEvent.getMessage();
    }

    public ModificationAuditEvent(User user, LocalDateTime dateTime, Type type, String message) {
        this.user = user;
        this.dateTime = dateTime;
        this.type = type;
        this.message = message;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public Type getType() {
        return type;
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
        return "ModificationAuditEvent{" +
                "user=" + user +
                ", dateTime=" + dateTime +
                ", type=" + type +
                ", message='" + message + '\'' +
                '}';
    }
}
