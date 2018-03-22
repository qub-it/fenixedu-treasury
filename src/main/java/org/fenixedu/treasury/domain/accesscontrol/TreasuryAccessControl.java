package org.fenixedu.treasury.domain.accesscontrol;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.DynamicGroup;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.services.accesscontrol.spi.ITreasuryAccessControlExtension;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class TreasuryAccessControl {

    private static final String TREASURY_MANAGERS = "treasuryManagers";

    private static final String TREASURY_BACK_OFFICE = "treasuryBackOffice";

    private static final String TREASURY_FRONT_OFFICE = "treasuryFrontOffice";

    private static TreasuryAccessControl _instance = null;

    private final List<ITreasuryAccessControlExtension> extensions = Collections.synchronizedList(Lists.newArrayList());

    private TreasuryAccessControl() {
    }

    public boolean isFrontOfficeMember() {
        return isFrontOfficeMember(Authenticate.getUser());
    }

    public boolean isBackOfficeMember() {
        return isBackOfficeMember(Authenticate.getUser());
    }

    public boolean isFrontOfficeMember(final FinantialInstitution finantialInstitution) {
        return isFrontOfficeMember(Authenticate.getUser(), finantialInstitution);
    }

    public boolean isBackOfficeMember(final FinantialInstitution finantialInstitution) {
        return isBackOfficeMember(Authenticate.getUser(), finantialInstitution);
    }

    public boolean isManager() {
        return isManager(Authenticate.getUser());
    }

    public boolean isFrontOfficeMember(final User user) {
        for (ITreasuryAccessControlExtension iTreasuryAccessControlExtension : extensions) {
            if (iTreasuryAccessControlExtension.isFrontOfficeMember(user)) {
                return true;
            }
        }

        return getOrCreateDynamicGroup(TREASURY_FRONT_OFFICE).isMember(user);
    }

    public boolean isFrontOfficeMember(final User user, final FinantialInstitution finantialInstitution) {
        for (ITreasuryAccessControlExtension iTreasuryAccessControlExtension : extensions) {
            if (iTreasuryAccessControlExtension.isFrontOfficeMember(user, finantialInstitution)) {
                return true;
            }
        }

        return getOrCreateDynamicGroup(TREASURY_FRONT_OFFICE).isMember(user);
    }

    public boolean isBackOfficeMember(final User user) {
        for (ITreasuryAccessControlExtension iTreasuryAccessControlExtension : extensions) {
            if (iTreasuryAccessControlExtension.isBackOfficeMember(user)) {
                return true;
            }
        }

        return getOrCreateDynamicGroup(TREASURY_BACK_OFFICE).isMember(user);
    }

    public boolean isBackOfficeMember(final User user, final FinantialInstitution finantialInstitution) {
        for (ITreasuryAccessControlExtension iTreasuryAccessControlExtension : extensions) {
            if (iTreasuryAccessControlExtension.isBackOfficeMember(user, finantialInstitution)) {
                return true;
            }
        }

        return getOrCreateDynamicGroup(TREASURY_BACK_OFFICE).isMember(user);
    }

    public boolean isBackOfficeMember(final User user, final FinantialEntity finantialEntity) {
        for (ITreasuryAccessControlExtension iTreasuryAccessControlExtension : extensions) {
            if (iTreasuryAccessControlExtension.isBackOfficeMember(user, finantialEntity)) {
                return true;
            }
        }

        return getOrCreateDynamicGroup(TREASURY_BACK_OFFICE).isMember(user);
    }

    public boolean isManager(final User user) {
        return getOrCreateDynamicGroup(TREASURY_MANAGERS).isMember(user);
    }

    public Set<User> getFrontOfficeMembers() {
        final Set<User> result = Sets.newHashSet();

        for (ITreasuryAccessControlExtension iTreasuryAccessControlExtension : extensions) {
            result.addAll(iTreasuryAccessControlExtension.getFrontOfficeMembers());
        }

        result.addAll(getOrCreateDynamicGroup(TREASURY_FRONT_OFFICE).getMembers().collect(Collectors.toSet()));

        return result;
    }

    public Set<User> getBackOfficeMembers() {
        final Set<User> result = Sets.newHashSet();

        for (ITreasuryAccessControlExtension iTreasuryAccessControlExtension : extensions) {
            result.addAll(iTreasuryAccessControlExtension.getBackOfficeMembers());
        }

        result.addAll(getOrCreateDynamicGroup(TREASURY_BACK_OFFICE).getMembers().collect(Collectors.toSet()));

        return result;
    }

    public Set<User> getTreasuryManagerMembers() {
        final Set<User> result = Sets.newHashSet();

        result.addAll(getOrCreateDynamicGroup(TREASURY_MANAGERS).getMembers().collect(Collectors.toSet()));

        return result;
    }

    public void registerExtension(final ITreasuryAccessControlExtension extension) {
        extensions.add(extension);
    }

    public void unregisterExtension(final ITreasuryAccessControlExtension extension) {
        extensions.add(extension);
    }

    public synchronized static TreasuryAccessControl getInstance() {
        if (_instance == null) {
            _instance = new TreasuryAccessControl();
        }

        return _instance;
    }

    private DynamicGroup getOrCreateDynamicGroup(final String dynamicGroupName) {
        final DynamicGroup dynamicGroup = DynamicGroup.get(dynamicGroupName);

        if (!dynamicGroup.isDefined()) {
            User manager = User.findByUsername("manager");
            if (manager != null) {
                dynamicGroup.mutator().grant(manager);
            } else {
                dynamicGroup.toPersistentGroup();
            }
        }

        return dynamicGroup;
    }

    public boolean isAllowToModifyInvoices(final User user, final FinantialInstitution finantialInstitution) {
        boolean result = getOrCreateDynamicGroup(TREASURY_FRONT_OFFICE).isMember(user);
        if (result == true) {
            return result;
        }
        for (ITreasuryAccessControlExtension iTreasuryAccessControlExtension : extensions) {
            if (iTreasuryAccessControlExtension.isAllowToModifyInvoices(user, finantialInstitution) == false) {
                return false;
            }
        }

        return true;
    }

    public boolean isAllowToModifySettlements(final User user, final FinantialInstitution finantialInstitution) {
        boolean result = getOrCreateDynamicGroup(TREASURY_FRONT_OFFICE).isMember(user);
        if (result == true) {
            return result;
        }
        for (ITreasuryAccessControlExtension iTreasuryAccessControlExtension : extensions) {
            if (iTreasuryAccessControlExtension.isAllowToModifySettlements(user, finantialInstitution) == false) {
                return false;
            }
        }

        return true;
    }

}
