package org.fenixedu.treasury.services.groups;

import static org.fenixedu.treasury.util.Constants.treasuryBundle;

import java.util.Set;

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
    public Set<User> getMembers() {
        return TreasuryAccessControlAPI.getFrontOfficeMembers();
    }

    @Override
    public Set<User> getMembers(final DateTime when) {
        return getMembers();
    }

    @Override
    public String getPresentationName() {
        return treasuryBundle("label.TreasuryFrontOfficeGroup.description");
    }

    @Override
    public boolean isMember(User user) {
        return getMembers().contains(user);
    }

    @Override
    public boolean isMember(final User user, final DateTime when) {
        return getMembers().contains(user);
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
    public boolean equals(Object obj) {
        return obj instanceof TreasuryFrontOfficeGroup;
    }

}
