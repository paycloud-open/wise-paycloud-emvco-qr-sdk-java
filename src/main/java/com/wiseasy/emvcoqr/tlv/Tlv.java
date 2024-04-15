package com.wiseasy.emvcoqr.tlv;

/**
 * @author Greg
 * @since 2024-04-15 10:15
 */
public class Tlv {

    private final Integer tag;
    private final Integer length;
    private final String value;
    private TlvArray subTlv = new TlvArray();

    public Tlv(Integer tag, Integer length, String value) {
        this.tag = tag;
        this.length = length;
        this.value = value;
    }


    public boolean hasSubTlv() {
        return !subTlv.isEmpty();
    }

    public Integer getLength() {
        return length;
    }

    public TlvArray getSubTlv() {
        return subTlv;
    }

    public void setSubTlv(TlvArray subTlv) {
        this.subTlv = subTlv;
    }

    public Integer getTag() {
        return tag;
    }

    public String getValue() {
        return value;
    }


    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(String.format("%02d", this.getTag()));
        stringBuffer.append(String.format("%02d", this.getLength()));
        if (hasSubTlv()) {
            this.getSubTlv().forEach(tempTlv -> stringBuffer.append(tempTlv.toString()));
        } else {
            stringBuffer.append(this.getValue());
        }
        return stringBuffer.toString();
    }

    public static String format(String aPadding, Tlv tlv) {
        StringBuilder stringBuffer = new StringBuilder();
        if (tlv == null) {
            return stringBuffer.toString();
        }
        if (tlv.hasSubTlv()) {
            stringBuffer.append(String.format("%s [%02d-%02d] \n", aPadding, tlv.getTag(), tlv.getLength()));
            for (Tlv child : tlv.getSubTlv()) {
                stringBuffer.append(format(aPadding + "    ", child));
            }
        } else {
            stringBuffer.append(String.format("%s [%02d-%02d] %s\n", aPadding, tlv.getTag(), tlv.getLength(), tlv.getValue()));
        }
        return stringBuffer.toString();
    }
}
