package com.wiseasy.emvcoqr;

import cn.hutool.extra.qrcode.QrCodeUtil;
import com.alibaba.fastjson2.JSONObject;
import com.wiseasy.emvcoqr.crc.CrcUtil;
import com.wiseasy.emvcoqr.exception.EMVCoQRException;
import com.wiseasy.emvcoqr.tlv.Tlv;
import com.wiseasy.emvcoqr.tlv.TlvArray;

import java.io.File;

/**
 * @author Greg
 * @since 2024-04-15 10:12
 */
public class EMVCoQR {

    /**
     * Payload Format Indicator
     */
    private String indicator;

    /**
     * Point of Initiation Method
     * 11: Static QRC
     * 12: Dynamic QRC
     */
    private String qr_type;

    /**
     * Merchant No
     */
    private String merchant_no;

    /**
     * Indicates the currency code of the transaction.
     * A 3-digit numerical value, as defined in [ISO 4217].
     * Whenever the amount is displayed or when prompted to enter the amount,
     * the mobile application will use this value to display the recognizable currency to the consumer.
     */
    private String trans_currency;

    /**
     * Transaction amount. For example,"99.34".
     * If it exists, the mobile application will display the value to the consumer when processing the transaction.
     */
    private String trans_amount;

    /**
     * The Merchant Name can only use ASCl character
     * set.The Merchant Name shall be the "doing businesses" name for the merchant.
     * The mobile application should display the Merchant Name.
     */
    private String merchant_name;

    /**
     * Transition Number
     */
    private String trans_no;

    /**
     * Terminal SN
     */
    private String terminal_sn;

    /**
     * The validity period of a QR code order is a 13 digit timestamp.
     * If it exceeds the current time, the order cannot be payment anymore, and the application will display the expired QR code
     */
    private String expire_time;


    public static boolean checkCrc(String payload, String crc) {
        String crc16Code = CrcUtil.getCrc16Code(payload);
        System.out.println(crc16Code);
        return crc16Code.equals(crc);
    }

    public String generatorQRStr() {
        // Build QR Code
        return TlvArray.builder()
                .add(0, indicator) // Payload Format Indicator
                .add(1, qr_type)         // Point of Initiation Method
                .add(15, merchant_no) // Merchant Account Information
                .add(53, trans_currency)     // Transaction Currency
                .add(54, trans_amount)   // Transaction Amount
                .add(59, merchant_name)  // Merchant Name
                .add(
                        TlvArray.builder(62)  // Additional Data Field Template
                                .add(1, trans_no) // Paycloud Trans No.
                                .add(7, terminal_sn) // Terminal SN
                                .add(50, expire_time) // Expiration time
                )
                .buildEMVCoQrCodeStr();
    }

    public void generatorQRFile(Integer width, Integer height, File targetFile) {
        // Build QR Code
        QrCodeUtil.generate(generatorQRStr(), width, height, targetFile);
    }

    public static EMVCoQR parse(File file) {
        return parse(QrCodeUtil.decode(file));
    }

    public static EMVCoQR parse(String tlvStr) {

        try {
            int length = tlvStr.length();
            String start = tlvStr.substring(0, length - 4);
            String crc = tlvStr.substring(length - 4);
            boolean b = checkCrc(start, crc);
            if (!b) {
                throw new EMVCoQRException("The QR is modified, and the CRC check value is not the same");
            }
        } catch (Exception e) {
            throw new EMVCoQRException("check crc error");
        }

        TlvArray tlvArray = TlvArray.parse(tlvStr);

        EMVCoQR emvCoQR = new EMVCoQR();
        emvCoQR.setIndicator(tlvArray.queryTagValue(0));
        emvCoQR.setQr_type(tlvArray.queryTagValue(1));
        emvCoQR.setMerchant_no(tlvArray.queryTagValue(15));
        emvCoQR.setTrans_currency(tlvArray.queryTagValue(53));
        emvCoQR.setTrans_amount(tlvArray.queryTagValue(54));
        emvCoQR.setMerchant_name(tlvArray.queryTagValue(59));

        Tlv tlv = tlvArray.queryTag(62);
        if (null != tlv && tlv.hasSubTlv()) {
            TlvArray subTag = tlv.getSubTlv();
            emvCoQR.setTrans_no(subTag.queryTagValue(1));
            emvCoQR.setTerminal_sn(subTag.queryTagValue(7));
            emvCoQR.setExpire_time(subTag.queryTagValue(50));
        }

        return emvCoQR;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

    public String getExpire_time() {
        return expire_time;
    }

    public void setExpire_time(String expire_time) {
        this.expire_time = expire_time;
    }

    public String getIndicator() {
        return indicator;
    }

    public void setIndicator(String indicator) {
        this.indicator = indicator;
    }

    public String getMerchant_name() {
        return merchant_name;
    }

    public void setMerchant_name(String merchant_name) {
        this.merchant_name = merchant_name;
    }

    public String getMerchant_no() {
        return merchant_no;
    }

    public void setMerchant_no(String merchant_no) {
        this.merchant_no = merchant_no;
    }

    public String getQr_type() {
        return qr_type;
    }

    public void setQr_type(String qr_type) {
        this.qr_type = qr_type;
    }

    public String getTerminal_sn() {
        return terminal_sn;
    }

    public void setTerminal_sn(String terminal_sn) {
        this.terminal_sn = terminal_sn;
    }

    public String getTrans_amount() {
        return trans_amount;
    }

    public void setTrans_amount(String trans_amount) {
        this.trans_amount = trans_amount;
    }

    public String getTrans_currency() {
        return trans_currency;
    }

    public void setTrans_currency(String trans_currency) {
        this.trans_currency = trans_currency;
    }

    public String getTrans_no() {
        return trans_no;
    }

    public void setTrans_no(String trans_no) {
        this.trans_no = trans_no;
    }
}
