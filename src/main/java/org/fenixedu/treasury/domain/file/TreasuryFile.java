package org.fenixedu.treasury.domain.file;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;

public class TreasuryFile extends TreasuryFile_Base {
    
    public TreasuryFile() {
        super();
        setBennu(Bennu.getInstance());
    }
    
    public TreasuryFile(final String displayName, final String fileName, final byte[] content) {
        this();
        this.init(displayName, fileName, content);
        
        checkRules();
    }

    private void checkRules() {
        if (getBennu() == null) {
            throw new TreasuryDomainException("error.TreasuryFile.bennu.required");
        }
    }
    
    @Override
    public void delete() {
        super.setBennu(null);

        super.setForwardPaymentConfigurationFile(null);
        super.setForwardPaymentLogFile(null);
        super.setOperationFile(null);
        super.setPostForwardPaymentsReportFile(null);
        super.setSibsInputFile(null);
        super.setSibsOutputFile(null);
        super.setSibsReportFile(null);
        
        super.delete();
    }

    @Override
    public boolean isAccessible(final User user) {
        return false;
    }
    
}
