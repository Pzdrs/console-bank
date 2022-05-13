package bohac.auditlog;

import bohac.entity.User;

import java.time.LocalDateTime;

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
    public int compareTo(AuditEvent o) {
        return getDateTime().compareTo(o.getDateTime());
    }

    @Override
    public String toString() {
        return "GenericAccountEvent{" +
                "user=" + user +
                ", dateTime=" + dateTime +
                ", type=" + type +
                '}';
    }
}
