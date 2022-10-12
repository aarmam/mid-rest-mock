package com.nortal.mid.mock.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;

import java.math.BigInteger;

import static org.apache.commons.lang3.ArrayUtils.addAll;
import static org.apache.commons.lang3.ArrayUtils.subarray;

@RequiredArgsConstructor
public class DsaSignature {
    private final BigInteger r;
    private final BigInteger s;

    @SneakyThrows
    public static DsaSignature fromAsn1Encoding(byte[] asn1DsaSignature) {
        ASN1Sequence sequence = (ASN1Sequence) ASN1Primitive.fromByteArray(asn1DsaSignature);
        BigInteger r = ASN1Integer.getInstance(sequence.getObjectAt(0)).getValue();
        BigInteger s = ASN1Integer.getInstance(sequence.getObjectAt(1)).getValue();
        return new DsaSignature(r, s);
    }

    public static byte[] toByteArrayWithoutLeadingZero(BigInteger value) {
        byte[] result = value.toByteArray();
        return result[0] == 0 ? subarray(result, 1, result.length) : result;
    }

    public static byte[] padLeftWithZeroes(byte[] array, int requiredLength) {
        return array.length >= requiredLength ? array : addAll(new byte[requiredLength - array.length], array);
    }

    public static int fieldSizeInBytes(int fieldSizeInBits) {
        return (int) Math.ceil((double) fieldSizeInBits / 8);
    }

    public byte[] encodeInCvc(int fieldSizeInBits) {
        byte[] rBytes = toByteArrayWithoutLeadingZero(r);
        byte[] sBytes = toByteArrayWithoutLeadingZero(s);
        int itemLength = fieldSizeInBytes(fieldSizeInBits);
        return addAll(padLeftWithZeroes(rBytes, itemLength), padLeftWithZeroes(sBytes, itemLength));
    }
}
