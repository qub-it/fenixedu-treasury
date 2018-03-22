package org.fenixedu.treasury.services.groups;

import java.util.stream.Stream;

import org.fenixedu.bennu.core.annotation.GroupOperator;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.groups.PersistentGroup;
import org.fenixedu.bennu.core.groups.CustomGroup;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.treasury.domain.accesscontrol.PersistentTreasuryBackOfficeGroup;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;

import com.google.common.base.Objects;

@GroupOperator("treasuryBackOffice")
public class TreasuryBackOfficeGroup extends CustomGroup {

    private static final long serialVersionUID = -933441447959188208L;

    private static final TreasuryBackOfficeGroup INSTANCE = new TreasuryBackOfficeGroup();

    private TreasuryBackOfficeGroup() {
        super();
    }

    public static TreasuryBackOfficeGroup get() {
        return INSTANCE;
    }

    @Override
    public String getPresentationName() {
        return BundleUtil.getString(Constants.BUNDLE, "label.TreasuryBackOfficeGroup.description");
    }

    @Override
    public Stream<User> getMembers() {
        return TreasuryAccessControlAPI.getBackOfficeMembers().stream();
    }

    @Override
    public boolean isMember(final User user) {
        return getMembers().anyMatch(u -> u == user);
    }

    @Override
    public Stream<User> getMembers(final DateTime when) {
        return getMembers();
    }

    @Override
    public boolean isMember(final User user, final DateTime when) {
        return isMember(user);
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof TreasuryBackOfficeGroup;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(TreasuryBackOfficeGroup.class);
    }

    @Override
    public PersistentGroup toPersistentGroup() {
        return PersistentTreasuryBackOfficeGroup.getInstance();
    }
}
