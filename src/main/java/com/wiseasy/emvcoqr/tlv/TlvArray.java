package com.wiseasy.emvcoqr.tlv;

import com.wiseasy.emvcoqr.crc.CrcUtil;
import com.wiseasy.emvcoqr.exception.EMVCoQRException;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author Greg
 * @since 2024-04-15 10:39
 */
public class TlvArray extends ArrayList<Tlv> {

    private static final Pattern TAG_LENGTH_PATTEN = Pattern.compile("^(0\\d|[1-9]\\d)$");

    public static final int DEFAULT_LENGTH = 2;

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Integer template) {
        return new Builder(template);
    }

    public static class Builder {

        private static final int DEFAULT_BUFFER_LENGTH = 5 * 1024;
        private final StringBuffer buffer;
        private final Integer tag;

        public Builder(Integer tag) {
            this(new StringBuffer(DEFAULT_BUFFER_LENGTH), tag);
        }

        public Builder() {
            this(new StringBuffer(DEFAULT_BUFFER_LENGTH), null);
        }

        public Builder(StringBuffer buffer, Integer tag) {
            this.buffer = buffer;
            this.tag = tag;
        }

        public void build() {
            if (tag != null) {
                buffer.insert(0, String.format("%02d%02d", tag, buffer.length()));
            }
        }


        public Builder add(Integer tag, String value) {
            buffer.append(String.format("%02d", tag));
            buffer.append(String.format("%02d", value.length()));
            buffer.append(value);
            return this;
        }

        public Builder add(Integer tag, Integer value) {
            return add(tag, String.valueOf(value));
        }

        public Builder add(Builder builder) {
            this.buffer.append(builder.buildStr());
            return this;
        }

        public String buildStr() {
            build();
            return buffer.toString();
        }

        public TlvArray buildTlv() {
            return TlvArray.parse(buffer.toString());
        }

        public String buildEMVCoQrCodeStr() {
            build();
            StringBuffer append = buffer.append("6304");
            String crc16Code = CrcUtil.getCrc16Code(append.toString());
            return append.append(String.format("%4s", crc16Code).replaceAll(" ", "0")).toString();
        }


    }

    public String format() {
        return format(this);
    }

    public Tlv queryTag(Integer tag) {
        return this.stream().filter(tlv -> Objects.equals(tlv.getTag(), tag)).findFirst().orElse(null);
    }

    public String queryTagValue(Integer tag) {
        Tlv tlv = queryTag(tag);
        if (null == tlv) {
            return null;
        }
        return tlv.getValue();
    }


    public static TlvArray parse(String tlvStr) {
        TlvArray result = new TlvArray();
        int pos = 0;
        int length = tlvStr.length();
        while (pos < length) {
            // The tag + length bit must be at least 4 characters
            if (pos + 4 > length) {
                throw new EMVCoQRException("The data does not conform to the format");
            }

            // Tag
            String tagStr = tlvStr.substring(pos, pos + DEFAULT_LENGTH);
            pos += DEFAULT_LENGTH;

            // Value Length
            String lengthStr = tlvStr.substring(pos, pos + DEFAULT_LENGTH);
            pos += DEFAULT_LENGTH;

            // If none of them are numbers, the format is abnormal
            if (isNotNumber(tagStr) && isNotNumber(lengthStr)) {
                throw new EMVCoQRException("The data does not conform to the format");
            }
            // value length
            int valueLength = Integer.parseInt(lengthStr);
            if (pos + valueLength > length) {
                throw new EMVCoQRException("The data does not conform to the format");
            }
            String tagValue = tlvStr.substring(pos, pos + valueLength);
            Tlv tlv = new Tlv(Integer.parseInt(tagStr), tagValue, valueLength);
            if (checkHasSubTlv(tagValue)) {
                tlv.setSubTlv(parse(tagValue));
            }
            result.add(tlv);
            pos += valueLength;
        }
        return result;
    }

    private static boolean isNotNumber(String str) {
        return !TAG_LENGTH_PATTEN.matcher(str).matches();
    }

    private static boolean checkHasSubTlv(String value) {
        int length = value.length();
        if (length <= 4) {
            return false;
        }
        int index = 0;
        while (index <= length) {
            String tagStr = value.substring(index, index + 2);
            index += 2;

            String lengthStr = value.substring(index, index + 2);
            index += 2;

            if (isNotNumber(tagStr) || isNotNumber(lengthStr)) {
                return false;
            }
            index += Integer.parseInt(lengthStr);
            if (index == length) {
                return true;
            }
        }
        return false;
    }

    public static String format(String tlvStr) {
        return format("-", tlvStr);
    }

    public static String format(String aPadding, String tlvStr) {
        return format(aPadding, TlvArray.parse(tlvStr));
    }

    public static String format(TlvArray tlvArray) {
        return format("-", tlvArray);
    }


    public static String format(String aPadding, TlvArray tlvArray) {
        StringBuilder stringBuffer = new StringBuilder();
        tlvArray.forEach(tlv -> stringBuffer.append(Tlv.format(aPadding, tlv)));
        return stringBuffer.toString();
    }


}
