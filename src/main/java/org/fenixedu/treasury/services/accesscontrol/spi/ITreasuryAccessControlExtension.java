package org.fenixedu.treasury.services.accesscontrol.spi;

import java.util.Set;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.event.TreasuryEvent;

public interface ITreasuryAccessControlExtension {

    public boolean isFrontOfficeMember(final User user);

    public boolean isFrontOfficeMember(final User user, final FinantialInstitution finantialInstitution);
    
    public boolean isBackOfficeMember(final User user);

    public boolean isBackOfficeMember(final User user, final FinantialInstitution finantialInstitution);

    public boolean isBackOfficeMember(final User user, final FinantialEntity finantialEntity);

    public Set<User> getFrontOfficeMembers();

    public Set<User> getBackOfficeMembers();

    public boolean isAllowToModifySettlements(final User user, final FinantialInstitution finantialInstitution);

    public boolean isAllowToModifyInvoices(final User user, final FinantialInstitution finantialInstitution);

}
