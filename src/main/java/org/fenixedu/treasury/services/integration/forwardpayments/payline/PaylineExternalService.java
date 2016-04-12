package org.fenixedu.treasury.services.integration.forwardpayments.payline;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;

import javax.xml.ws.BindingProvider;

import org.fenixedu.treasury.domain.Currency;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.fenixedu.treasury.domain.forwardpayments.implementations.PaylineImplementation;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.qubit.solution.fenixedu.bennu.webservices.services.client.BennuWebServiceClient;

import pt.ist.fenixframework.Atomic;

public class PaylineExternalService extends BennuWebServiceClient<WebPaymentAPI> {

    private static final String EURO_CURRENCY = "978";
    private static final String ACTION_AUTHORIZATION_AND_VALIDATION = "101";
    private static final String MODE_FULL = "CPT";
    private static final String TRANSACTION_APPROVED_CODE = "00000";

    public PaylineExternalService() {
    }

    @Atomic
    public boolean doWebPayment(final ForwardPayment forwardPayment) {
        PaylineImplementation implementation =
                (PaylineImplementation) forwardPayment.getForwardPaymentConfiguration().implementation();

        final Payment paymentDetails = new Payment();
        paymentDetails.setAmount(forwardPayment.getAmount().multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_EVEN).toString());
        paymentDetails.setCurrency(EURO_CURRENCY);
        paymentDetails.setAction(ACTION_AUTHORIZATION_AND_VALIDATION);
        paymentDetails.setMode(MODE_FULL);

        paymentDetails.setContractNumber(forwardPayment.getForwardPaymentConfiguration().getPaylineContractNumber());

        final Order order = new Order();
        order.setRef(String.valueOf(forwardPayment.getOrderNumber()));
        order.setAmount(forwardPayment.getAmount().multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_EVEN).toString());
        order.setCurrency(EURO_CURRENCY);
        order.setDate(forwardPayment.getVersioningCreationDate().toString("dd/MM/yyyy HH:mm"));
        order.setCountry("PT");

        // fillOrderDetails(forwardPayment, order);

        final Customer customer = forwardPayment.getDebtAccount().getCustomer();

        final Buyer buyerDetails = new Buyer();
        buyerDetails.setFirstName(customer.getFirstNames());
        buyerDetails.setLastName(customer.getLastNames());
        buyerDetails.setEmail(customer.getEmail());
        buyerDetails.setMobilePhone(customer.getPhoneNumber());

        /*
        final Address address = new Address();
        address.setStreet1(customer.getAddress());
        address.setZipCode(customer.getZipCode());
        address.setCountry(customer.getCountryCode());
        buyerDetails.setShippingAdress(address);
        */

        final DoWebPaymentRequest request = new DoWebPaymentRequest();

        request.setPayment(paymentDetails);
        request.setOrder(order);
        request.setReturnURL(implementation.getReturnURL(forwardPayment));
        request.setCancelURL(implementation.getCancelURL(forwardPayment));

        request.setBuyer(buyerDetails);
        request.setSecurityMode("SSL");

        final DoWebPaymentResponse response = getClient().doWebPayment(request);

        final boolean success = response != null && response.getResult() != null
                && TRANSACTION_APPROVED_CODE.equals(response.getResult().getCode());
        if (!success) {
            forwardPayment.reject(response.getResult().getCode(), response.getResult().getLongMessage(), null, null);
        } else {
            forwardPayment.advanceToRequestState(response.getResult().getCode(), response.getResult().getLongMessage());
            forwardPayment.setPaylineToken(response.getToken());
            forwardPayment.setPaylineRedirectUrl(response.getRedirectURL());
        }

        return success;
    }

    @Atomic
    public boolean getWebPaymentDetails(final ForwardPayment forwardPayment) {
        final GetWebPaymentDetailsRequest request = new GetWebPaymentDetailsRequest();

        request.setToken(forwardPayment.getPaylineToken());

        final GetWebPaymentDetailsResponse response = getClient().getWebPaymentDetails(request);

        final boolean success = TRANSACTION_APPROVED_CODE.equals(response.getResult().getCode());

        if (!success) {
            forwardPayment.reject(response.getResult().getCode(), response.getResult().getLongMessage(), null, null);
            return false;
        }

        final String transactionId = response.getTransaction().getId();
        final String authorizationNumber = response.getAuthorization().getNumber();

        final DateTime transactionDate =
                DateTimeFormat.forPattern("dd/MM/yyyy HH:mm").parseDateTime(response.getTransaction().getDate());
        final BigDecimal payedAmount = new BigDecimal(response.getPayment().getAmount()).divide(new BigDecimal("100"));

        forwardPayment.advanceToPayedState(response.getResult().getCode(), response.getResult().getLongMessage(), payedAmount,
                transactionDate, transactionId, authorizationNumber);
        return true;
    }

    private void fillOrderDetails(final ForwardPayment forwardPayment, final Order order) {
        final Currency currency = forwardPayment.getDebtAccount().getFinantialInstitution().getCurrency();
        final Details details = new Details();
        for (final DebitEntry debitEntry : forwardPayment.getDebitEntriesSet()) {
            final OrderDetail orderDetail = new OrderDetail();
            orderDetail.setRef(debitEntry.getExternalId());
            orderDetail.setPrice(currency.getValueWithScale(debitEntry.getOpenAmount()).multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_EVEN)
                    .toString());
            orderDetail.setQuantity("1");
            orderDetail.setComment(debitEntry.getDescription());
            details.getDetails().add(orderDetail);
        }

        order.setDetails(details);
    }

    @Override
    protected BindingProvider getService() {
        BindingProvider bindingProvider = (BindingProvider) new WebPaymentAPI_Service().getWebPaymentAPI();
        return bindingProvider;
    }

}
