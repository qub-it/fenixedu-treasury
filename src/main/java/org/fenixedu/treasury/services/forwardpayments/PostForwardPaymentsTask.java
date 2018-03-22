package org.fenixedu.treasury.services.forwardpayments;

import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.joda.time.DateTime;

@Task(englishTitle = "Post forward payments registration", readOnly = true)
public class PostForwardPaymentsTask extends CronTask {

    @Override
    public void runTask() throws Exception {
        DateTime now = new DateTime();
        ForwardPayment.postForwardPaymentProcessService(now.minusDays(3), now, getLogger());
    }

}
