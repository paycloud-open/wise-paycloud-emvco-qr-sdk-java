package com.wiseasy.emvcoqr;

import com.wiseasy.emvcoqr.tlv.TlvArray;
import org.junit.Test;

import java.io.File;

/**
 * @author Greg
 * @since 2024-04-15 10:24
 */
public class EMVCoQRTest {

    /**
     * Test generate a dynamic QR code
     */
    @Test
    public void testGeneratorQrStr() {
        EMVCoQR emvCoQR = new EMVCoQR();
        emvCoQR.setTrans_no("ORDER" + System.currentTimeMillis());
        emvCoQR.setQr_type("12");
        emvCoQR.setIndicator("01");
        emvCoQR.setMerchant_name("MerchantName");
        emvCoQR.setTrans_amount("230.00");
        emvCoQR.setTrans_currency("710");
        emvCoQR.setMerchant_no("781618781716671");
        emvCoQR.setTerminal_sn("P5189168176");
        emvCoQR.setExpire_time(String.valueOf(System.currentTimeMillis() + 60 * 1000));
        System.out.println("qrStr: " + emvCoQR.generatorQRStr());
    }

    /**
     * Test to generate a QR file
     */
    @Test
    public void testGeneratorQrFile() {
        EMVCoQR emvCoQR = new EMVCoQR();
        emvCoQR.setTrans_no("ORDER" + System.currentTimeMillis());
        emvCoQR.setQr_type("12");
        emvCoQR.setIndicator("01");
        emvCoQR.setMerchant_name("MerchantName");
        emvCoQR.setTrans_amount("230.00");
        emvCoQR.setTrans_currency("710");
        emvCoQR.setMerchant_no("781618781716671");
        emvCoQR.setTerminal_sn("P5189168176");
        emvCoQR.setExpire_time(String.valueOf(System.currentTimeMillis() + 60 * 1000));
        emvCoQR.generatorQRFile(300, 300, new File("qr.png"));
    }


    /**
     * Test formatting TLV
     */
    @Test
    public void testFormat() {
        String qrStr = "00020101021215157816187817166715303840540520.005912MerchantName62540118ORDER17131486777990711P5189168176501317131487377996304BBB6";
        System.out.println("format: " + TlvArray.format(qrStr));
    }

    /**
     * Test parsing QR content
     */
    @Test
    public void testParse() {
        String qrStr = "000201010212151578161878171667153037105406230.005912MerchantName62540118ORDER17131538774750711P51891681765013171315393747563049652";
        EMVCoQR qr = EMVCoQR.parse(qrStr);
        System.out.println(qr);
    }


    /**
     * Test parses the content of the QR through a file
     */
    @Test
    public void testParseFromFile() {
        EMVCoQR qr = EMVCoQR.parse(new File("qr.png"));
        System.out.println(qr);
    }


}
