package kin.sdk.core.util;

import java.math.BigDecimal;
import org.ethereum.geth.BigInt;
import org.ethereum.geth.Geth;

/**
 * A Utility class used to convert currency values to/from Kin.
 */
public class KinConverter {

    private static final BigDecimal KIN = BigDecimal.TEN.pow(18);

    private static BigInt toBigInt(BigDecimal bigDecimal) {
        BigInt bigInt = Geth.newBigInt(0L);
        //to get ByteArray representation, convert to Java BigInteger (will discard the fractional part)
        //then extract ByteArray from the BigInteger, BigInteger representation is in two's complement,
        // but as we're not dealing with negative numbers, it's safe to init the Unsigned BigInt with it
        bigInt.setBytes(bigDecimal.toBigInteger().toByteArray());
        return bigInt;
    }

    public static BigInt fromKin(BigDecimal value) {
        BigDecimal bigDecimal = value.multiply(KIN);
        return toBigInt(bigDecimal);
    }

    public static BigDecimal toKin(BigInt value) {
        return toKin(new BigDecimal(value.string()));
    }

    public static BigDecimal toKin(BigDecimal value) {
        return value.divide(KIN, 18, BigDecimal.ROUND_FLOOR);
    }
}
