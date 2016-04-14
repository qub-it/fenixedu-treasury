package org.fenixedu.treasury.domain.forwardpayments.implementations;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.xml.ws.BindingProvider;

import org.fenixedu.treasury.domain.Currency;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.document.DebitEntry;
import org.fenixedu.treasury.domain.forwardpayments.ForwardPayment;
import org.fenixedu.treasury.services.integration.forwardpayments.payline.Address;
import org.fenixedu.treasury.services.integration.forwardpayments.payline.Buyer;
import org.fenixedu.treasury.services.integration.forwardpayments.payline.Details;
import org.fenixedu.treasury.services.integration.forwardpayments.payline.DoWebPaymentRequest;
import org.fenixedu.treasury.services.integration.forwardpayments.payline.DoWebPaymentResponse;
import org.fenixedu.treasury.services.integration.forwardpayments.payline.GetWebPaymentDetailsRequest;
import org.fenixedu.treasury.services.integration.forwardpayments.payline.GetWebPaymentDetailsResponse;
import org.fenixedu.treasury.services.integration.forwardpayments.payline.Order;
import org.fenixedu.treasury.services.integration.forwardpayments.payline.OrderDetail;
import org.fenixedu.treasury.services.integration.forwardpayments.payline.Payment;
import org.fenixedu.treasury.services.integration.forwardpayments.payline.WebPaymentAPI;
import org.fenixedu.treasury.services.integration.forwardpayments.payline.WebPaymentAPI_Service;
import org.fenixedu.treasury.ui.document.forwardpayments.IForwardPaymentController;
import org.fenixedu.treasury.ui.document.forwardpayments.implementations.PaylineController;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;
import com.qubit.solution.fenixedu.bennu.webservices.services.client.BennuWebServiceClient;

import pt.ist.fenixframework.Atomic;

public class PaylineImplementation extends BennuWebServiceClient<WebPaymentAPI> implements IForwardPaymentImplementation {

    private static final String SECURITY_MODE = "SSL";
    private static final String PT = "PT";
    private static final String EURO_CURRENCY = "978";
    private static final String ACTION_AUTHORIZATION_AND_VALIDATION = "101";
    private static final String MODE_CPT = "CPT";
    private static final String TRANSACTION_APPROVED_CODE = "00000";

    public static final String ACTION_RETURN_URL = "return";
    public static final String ACTION_CANCEL_URL = "cancel";
    public static final String LANG_PT = "pt";

    @Override
    public String getPaymentURL(final ForwardPayment forwardPayment) {
        throw new RuntimeException("not applied");
    }

    public String getReturnURL(final ForwardPayment forwardPayment) {
        return String.format("%s/%s/%s", forwardPayment.getForwardPaymentConfiguration().getReturnURL(),
                forwardPayment.getExternalId(), ACTION_RETURN_URL);
    }

    public String getCancelURL(final ForwardPayment forwardPayment) {
        return String.format("%s/%s/%s", forwardPayment.getForwardPaymentConfiguration().getReturnURL(),
                forwardPayment.getExternalId(), ACTION_CANCEL_URL);
    }

    @Override
    public String getFormattedAmount(final ForwardPayment forwardPayment) {
        return forwardPayment.getAmount().multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_EVEN).toString();
    }

    @Override
    public IForwardPaymentController getForwardPaymentController(final ForwardPayment forwardPayment) {
        return new PaylineController();
    }

    public boolean isActionReturn(final String action) {
        return ACTION_RETURN_URL.equals(action);
    }

    public boolean isActionCancel(final String action) {
        return ACTION_CANCEL_URL.equals(action);
    }

    @Atomic
    public boolean doWebPayment(final ForwardPayment forwardPayment) {
        PaylineImplementation implementation =
                (PaylineImplementation) forwardPayment.getForwardPaymentConfiguration().implementation();

        final Payment paymentDetails = new Payment();
        paymentDetails.setAmount(getFormattedAmount(forwardPayment));
        paymentDetails.setCurrency(EURO_CURRENCY);
        paymentDetails.setAction(ACTION_AUTHORIZATION_AND_VALIDATION);
        paymentDetails.setMode(MODE_CPT);

        paymentDetails.setContractNumber(forwardPayment.getForwardPaymentConfiguration().getPaylineContractNumber());

        final Order order = new Order();
        order.setRef(String.valueOf(forwardPayment.getOrderNumber()));
        order.setAmount(getFormattedAmount(forwardPayment));
        order.setCurrency(EURO_CURRENCY);
        order.setDate(forwardPayment.getVersioningCreationDate().toString("dd/MM/yyyy HH:mm"));
        order.setCountry(PT);

        // fillOrderDetails(forwardPayment, order);

        final Customer customer = forwardPayment.getDebtAccount().getCustomer();

        final Buyer buyerDetails = new Buyer();
        buyerDetails.setFirstName(customer.getFirstNames());
        buyerDetails.setLastName(customer.getLastNames());
        buyerDetails.setEmail(customer.getEmail());
        buyerDetails.setMobilePhone(customer.getPhoneNumber());

        // fillAddress(customer, buyerDetails);

        final DoWebPaymentRequest request = new DoWebPaymentRequest();

        request.setPayment(paymentDetails);
        request.setOrder(order);
        request.setReturnURL(implementation.getReturnURL(forwardPayment));
        request.setCancelURL(implementation.getCancelURL(forwardPayment));
        request.setLanguageCode(LANG_PT);
        
        request.setBuyer(buyerDetails);
        request.setSecurityMode(SECURITY_MODE);

        final DoWebPaymentResponse response = getClient().doWebPayment(request);

        final boolean success = response != null && response.getResult() != null
                && TRANSACTION_APPROVED_CODE.equals(response.getResult().getCode());

        if (!success) {
            forwardPayment.reject(response.getResult().getCode(), response.getResult().getLongMessage(), json(request),
                    json(response));

            return false;
        }

        final String code = response.getResult().getCode();
        final String longMessage = response.getResult().getLongMessage();

        forwardPayment.advanceToRequestState(code, longMessage, json(request), json(response));
        forwardPayment.setPaylineToken(response.getToken());
        forwardPayment.setPaylineRedirectUrl(response.getRedirectURL());

        return true;
    }

    private String json(final Object obj) {
        GsonBuilder builder = new GsonBuilder();
        builder.addSerializationExclusionStrategy(new ExclusionStrategy() {

            @Override
            public boolean shouldSkipField(FieldAttributes arg0) {
                return false;
            }

            @Override
            public boolean shouldSkipClass(final Class<?> clazz) {
                if (clazz == Class.class) {
                    return true;
                }

                return false;
            }
        });

        return builder.create().toJson(obj);
    }

    @Atomic
    public boolean processPayment(final ForwardPayment forwardPayment, final String action) {

        if (!isActionReturn(action)) {
            final GetWebPaymentDetailsRequest request = new GetWebPaymentDetailsRequest();
            request.setToken(forwardPayment.getPaylineToken());

            final GetWebPaymentDetailsResponse response = getClient().getWebPaymentDetails(request);
            forwardPayment.reject(response.getResult().getCode(),
                    Constants.bundle("label.PaylineImplementation.cancelled") + ": " + response.getResult().getLongMessage(),
                    json(request), json(response));

            return false;
        }

        final GetWebPaymentDetailsRequest request = new GetWebPaymentDetailsRequest();
        request.setToken(forwardPayment.getPaylineToken());

        final GetWebPaymentDetailsResponse response = getClient().getWebPaymentDetails(request);

        final boolean success = TRANSACTION_APPROVED_CODE.equals(response.getResult().getCode());

        if (!success) {
            forwardPayment.reject(response.getResult().getCode(), response.getResult().getLongMessage(), json(request),
                    json(response));
            return false;
        }

        final String transactionId = response.getTransaction().getId();
        final String authorizationNumber = response.getAuthorization().getNumber();

        final DateTime transactionDate =
                DateTimeFormat.forPattern("dd/MM/yyyy HH:mm").parseDateTime(response.getTransaction().getDate());
        final BigDecimal payedAmount = new BigDecimal(response.getPayment().getAmount()).divide(new BigDecimal("100"));

        forwardPayment.advanceToPayedState(response.getResult().getCode(), response.getResult().getLongMessage(), payedAmount,
                transactionDate, transactionId, authorizationNumber, json(request), json(response));

        return true;
    }

    private void fillAddress(final Customer customer, final Buyer buyerDetails) {
        final Address address = new Address();
        address.setStreet1(customer.getAddress());
        address.setZipCode(customer.getZipCode());
        address.setCountry(customer.getCountryCode());
        buyerDetails.setShippingAdress(address);
    }

    private void fillOrderDetails(final ForwardPayment forwardPayment, final Order order) {
        final Currency currency = forwardPayment.getDebtAccount().getFinantialInstitution().getCurrency();
        final Details details = new Details();
        for (final DebitEntry debitEntry : forwardPayment.getDebitEntriesSet()) {
            final OrderDetail orderDetail = new OrderDetail();
            orderDetail.setRef(debitEntry.getExternalId());
            orderDetail.setPrice(currency.getValueWithScale(debitEntry.getOpenAmount()).multiply(new BigDecimal(100))
                    .setScale(0, RoundingMode.HALF_EVEN).toString());
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

    @Override
    public String getLogosJspPage() {
        return "implementations/payline/logos.jsp";
    }
}
