package kin.sdk.core.util;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.ethereum.geth.BigInt;
import org.ethereum.geth.Geth;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class KinConverterTest {

    @Test
    @Parameters({
        // 1 Wei to Kin (smallest coin value)
        "1, 0.000000000000000001",
        //Max Kin Supply
        "10000000000000000000000000000000, 10000000000000.000000000000000000",
        //Invalid Wei (wei is integer)
        "0.1, 0.000000000000000000"})
    public void toKinTest(String input, String output) throws Exception {
        BigDecimal bigDecimalWei = new BigDecimal(input);
        BigDecimal bigDecimalKin = KinConverter.toKin(bigDecimalWei);
        assertEquals(output, bigDecimalKin.toPlainString());
    }

    @Test
    @Parameters({
        // one wei (smallest coin value)
        "1, 0.000000000000000001",
        //max wei supply to kin
        "10000000000000000000000000000000, 10000000000000.000000000000000000",
        "1234567, 0.000000000001234567"})
    public void toKinFromBigIntTest(String input, String output) throws Exception {
        BigInt bigIntWei = Geth.newBigInt(0L);
        bigIntWei.setString(input, 10);
        BigDecimal bigDecimalKin = KinConverter.toKin(bigIntWei);
        assertEquals(output, bigDecimalKin.toPlainString());
    }

    @Test
    @Parameters({
        // 1 kin to wei
        "1, 1000000000000000000",
        //max kin supply to wei
        "10000000000000.000000000000000000, 10000000000000000000000000000000",
        //discard too low value (less then wei)
        "0.0000000000012345678, 1234567"})
    public void fromKinTest(String input, String output) throws Exception {
        BigDecimal bigDecimalKin = new BigDecimal(input);
        BigInt bigInt = KinConverter.fromKin(bigDecimalKin);
        assertEquals(output, bigInt.string());
    }

}