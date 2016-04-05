package org.fenixedu.treasury.services.payments.sibs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.services.payments.sibs.incomming.SibsIncommingPaymentFile;
import org.fenixedu.treasury.services.payments.sibs.incomming.SibsIncommingPaymentFileDetailLine;
import org.fenixedu.treasury.services.payments.sibs.incomming.SibsIncommingPaymentFileFooter;
import org.fenixedu.treasury.services.payments.sibs.incomming.SibsIncommingPaymentFileHeader;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.YearMonthDay;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;

public class SibsPaymentsBrokerService {

    //@formatter:off
/*
 * 
E034        - A036 (Código da Mensagem)
01      - A037 (Id. Sistema)
01      - A038 (Versão da mensagem)
0254        - A008 (Identificação Log SIBS)
00169930    - A014 (Nr. Log SIBS)
20647       - A033 (Nr. Entidade)
264775209   - A034 (Referência do Pagamento)
0000025000  - A011 (Montante Pago)
978     - A006 (Código de moeda)
201604050827    - A035 (Data /home transacção cliente)
01      - A015 (Tipo de terminal)
0010003504  - A016 (Identificação do terminal)
04117       - A017 (Identificação da transacção local)
Cartaxo         - A030 (Localidade do terminal)
000000000   - Número de contribuinte
00000000    - Número da factura
\ 

 */
    //@formatter:on

    public static SibsIncommingPaymentFile readPaymentsFromBroker(final FinantialInstitution finantialInstitution,
            final LocalDate fromDate, final LocalDate toDate) {
        if (!isSibsPaymentsBrokerActive(finantialInstitution)) {
            return null;
        }

        try {
            final URL url = new URL(invocationUrl(finantialInstitution, fromDate, toDate));
            final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setDoOutput(false);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String strResult = "";
            String strLine;
            while ((strLine = in.readLine()) != null) {
                strResult = strResult + strLine;
            }
            in.close();

            strResult = strResult.substring(0, strResult.indexOf("\"data\":\"") + 8) + strResult
                    .substring(strResult.indexOf("\"data\":\"") + 8, strResult.lastIndexOf("\"")).replaceAll("\\\\\"", "\"")
                    + "}";

            strResult = strResult.replaceAll("\"data\":\"\\[", "\"data\":[");

            final SibsPayments sibsPayments = new GsonBuilder().create().fromJson(strResult, SibsPayments.class);

            return parsePayments(finantialInstitution, sibsPayments);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String DATE_TIME_FORMAT = "yyyyMMddHHmm";
    private static final int[] FIELD_SIZES = new int[] { 4, 2, 2, 4, 8, 5, 9, 10, 3, 12, 2,10, 5, 15, 9, 8 };
    private static final Integer DEFAULT_SIBS_VERSION = 1;

    private static SibsIncommingPaymentFile parsePayments(final FinantialInstitution finantialInstitution,
            final SibsPayments sibsPayments) {

        final List<SibsIncommingPaymentFileDetailLine> detailLines = Lists.newArrayList();
        BigDecimal transactionsTotalAmount = BigDecimal.ZERO;
        for (final SibsPaymentEntry entry : sibsPayments.data) {
            final String rawLine = entry.msg.substring(47);
            final String[] fields = splitLine(rawLine);

            SibsIncommingPaymentFileDetailLine line =
                    new SibsIncommingPaymentFileDetailLine(getWhenOccuredTransactionFrom(fields), getAmountFrom(fields),
                            getSibsTransactionIdFrom(fields), getCodeFrom(fields));
            detailLines.add(line);

            transactionsTotalAmount = transactionsTotalAmount.add(line.getAmount());
        }

        final SibsIncommingPaymentFileHeader header = new SibsIncommingPaymentFileHeader(new YearMonthDay(), DEFAULT_SIBS_VERSION,
                finantialInstitution.getSibsConfiguration().getEntityReferenceCode());

        final SibsIncommingPaymentFileFooter footer =
                new SibsIncommingPaymentFileFooter(transactionsTotalAmount, BigDecimal.ZERO);

        return new SibsIncommingPaymentFile(String.format("SIBS_%s.inp", new DateTime().toString("yyyyMMddHHmmss")), header, footer,
                detailLines);
    }

    private static String getCodeFrom(String[] fields) {
        return fields[6];
    }

    private static String getSibsTransactionIdFrom(String[] fields) {
        return fields[3];
    }

    private static BigDecimal getAmountFrom(String[] fields) {
        return BigDecimal.valueOf(Double.parseDouble(fields[7].substring(0, 8) + "." + fields[7].substring(8)));
    }

    private static DateTime getWhenOccuredTransactionFrom(String[] fields) {
        try {
            return new DateTime(new SimpleDateFormat(DATE_TIME_FORMAT).parse(fields[9]));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private final static String[] splitLine(final String line) {
        int lastIndex = 0;
        final String[] result = new String[FIELD_SIZES.length];
        for (int i = 0; i < FIELD_SIZES.length; i++) {
            result[i] = line.substring(lastIndex, lastIndex + FIELD_SIZES[i]);
            lastIndex += FIELD_SIZES[i];
        }
        return result;
    }

    public static boolean isSibsPaymentsBrokerActive(final FinantialInstitution finantialInstitution) {
        return !Strings.isNullOrEmpty(finantialInstitution.getSibsConfiguration().getSibsPaymentsBrokerUrl());
    }

    private static String invocationUrl(final FinantialInstitution finantialInstitution, final LocalDate fromDate,
            final LocalDate toDate) {
        final String url = finantialInstitution.getSibsConfiguration().getSibsPaymentsBrokerUrl();
        final String key = finantialInstitution.getSibsConfiguration().getSibsPaymentsBrokerSharedKey();
        final String dtInicio = fromDate.toString("yyyy-MM-dd");
        final String dtFim = toDate.toString("yyyy-MM-dd");
        return String.format("%s?secret=%s&dt_inicio=%s&dt_fim=%s", url, key, dtInicio, dtFim);
    }

    private static class SibsPaymentEntry {
        private String id;
        private String msg;
        private String tipo;
        private String data;
    }

    private static class SibsPayments {
        private String error;
        private List<SibsPaymentEntry> data;
    }

}
