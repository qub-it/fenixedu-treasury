package org.fenixedu.treasury.services.groups;

import static org.fenixedu.treasury.util.TreasuryConstants.treasuryBundle;

import java.util.stream.Stream;

import org.fenixedu.bennu.core.annotation.GroupOperator;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.groups.PersistentGroup;
import org.fenixedu.bennu.core.groups.CustomGroup;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.treasury.domain.accesscontrol.PersistentTreasuryManagersGroup;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.util.TreasuryConstants;
import org.joda.time.DateTime;

import com.google.common.base.Objects;

@GroupOperator("treasuryManagers")
public class TreasuryManagersGroup extends CustomGroup {
    private static final long serialVersionUID = -933441447959188208L;

    private static final TreasuryManagersGroup INSTANCE = new TreasuryManagersGroup();

    private TreasuryManagersGroup() {
        super();
    }

    public static TreasuryManagersGroup get() {
        return INSTANCE;
    }

    @Override
    public Stream<User> getMembers() {
        return TreasuryAccessControlAPI.getTreasuryManagerMembers().stream();
    }

    @Override
    public Stream<User> getMembers(final DateTime when) {
        return getMembers();
    }

    @Override
    public String getPresentationName() {
        return treasuryBundle("label.TreasuryManagersGroup.description");
    }

    @Override
    public boolean isMember(final User user) {
        return getMembers().anyMatch(u -> u == user);
    }

    @Override
    public boolean isMember(final User user, final DateTime when) {
        return getMembers().anyMatch(u -> u == user);
    }

    @Override
    public PersistentGroup toPersistentGroup() {
        return PersistentTreasuryManagersGroup.getInstance();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(TreasuryManagersGroup.class);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof TreasuryManagersGroup;
    }

}
