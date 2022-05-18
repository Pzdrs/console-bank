package bohac.auditlog.events;

import bohac.Bank;
import bohac.auditlog.AuditEvent;
import bohac.util.Utils;
import bohac.entity.User;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.UUID;

public class GenericAuditEvent implements AuditEvent {
    private final User user;
    private final LocalDateTime dateTime;
    private final Type type;

    /**
     * Copy constructor
     *
     * @param auditEvent copy object
     */
    public GenericAuditEvent(AuditEvent auditEvent) {
        this.user = auditEvent.getUser();
        this.dateTime = auditEvent.getDateTime();
        this.type = auditEvent.getType();
    }

    public GenericAuditEvent(User user, LocalDateTime dateTime, Type type) {
        this.user = user;
        this.dateTime = dateTime;
        this.type = type;
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

    @Override
    public String toString() {
        return "GenericAccountEvent{" +
                "user=" + user +
                ", dateTime=" + dateTime +
                ", type=" + type +
                '}';
    }

    public static GenericAuditEvent load(JSONObject object) {
        return new GenericAuditEvent(
                Bank.users.getByID(UUID.fromString(object.getString("user"))).orElse(null),
                Utils.parseEpoch(object.getLong("date_time")),
                Type.valueOf(object.getString("type"))
        );
    }

    @Override
    public JSONObject toJSON() {
        return null;
    }
}
