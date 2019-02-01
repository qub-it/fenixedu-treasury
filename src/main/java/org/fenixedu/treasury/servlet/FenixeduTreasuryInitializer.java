package org.fenixedu.treasury.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.fenixedu.treasury.services.integration.FenixEDUTreasuryPlatformDependentServices;
import org.fenixedu.treasury.services.integration.TreasuryPlataformDependentServicesFactory;
import org.fenixedu.treasury.util.TreasuryBootstrapUtil;

@WebListener
public class FenixeduTreasuryInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        TreasuryPlataformDependentServicesFactory.registerImplementation(new FenixEDUTreasuryPlatformDependentServices());

        TreasuryBootstrapUtil.InitializeDomain();
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }
}