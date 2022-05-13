package bohac.auditlog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * ArrayList wrapper - Prevent all modifications to existing elements => AddOnly list
 */
public class AuditEventList extends ArrayList<AuditEvent> {

    public AuditEventList() {
    }

    public AuditEventList(Collection<? extends AuditEvent> c) {
        super(c);
    }

    @Override
    public AuditEvent set(int index, AuditEvent element) {
        return null;
    }

    @Override
    public AuditEvent remove(int index) {
        return null;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean removeIf(Predicate<? super AuditEvent> filter) {
        return false;
    }

    @Override
    public void replaceAll(UnaryOperator<AuditEvent> operator) {

    }
}
