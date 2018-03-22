package org.fenixedu.treasury.services.groups;

import java.util.stream.Stream;

import org.fenixedu.bennu.core.annotation.GroupOperator;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.groups.PersistentGroup;
import org.fenixedu.bennu.core.groups.CustomGroup;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.treasury.domain.accesscontrol.PersistentTreasuryFrontOfficeGroup;
import org.fenixedu.treasury.services.accesscontrol.TreasuryAccessControlAPI;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;

import com.google.common.base.Objects;

@GroupOperator("treasuryFrontOffice")
public class TreasuryFrontOfficeGroup extends CustomGroup {

    private static final long serialVersionUID = 1L;

    private static final TreasuryFrontOfficeGroup INSTANCE = new TreasuryFrontOfficeGroup();

    private TreasuryFrontOfficeGroup() {
        super();
    }

    public static TreasuryFrontOfficeGroup get() {
        return INSTANCE;
    }

    @Override
    public Stream<User> getMembers() {
        return TreasuryAccessControlAPI.getFrontOfficeMembers().stream();
    }

    @Override
    public Stream<User> getMembers(final DateTime when) {
        return getMembers();
    }

    @Override
    public String getPresentationName() {
        return BundleUtil.getString(Constants.BUNDLE, "label.TreasuryFrontOfficeGroup.description");
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
        return PersistentTreasuryFrontOfficeGroup.getInstance();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(TreasuryFrontOfficeGroup.class);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof TreasuryFrontOfficeGroup;
    }

}
