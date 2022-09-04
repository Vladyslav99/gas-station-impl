package com.testtask.gasstationimpl;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasStation;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GasStationImplTest {

    private GasStation testedInstance;

    private static final double DIESEL_PUMP_AMOUNT = 22.5;
    private static final double DIESEL_PRICE = 2.3;
    private static final double SUCCESSFUL_PURCHASE_TOTAL_PRICE = DIESEL_PUMP_AMOUNT * DIESEL_PRICE;

    private static final double EXPENSIVE_DIESEL_WILLING_PRICE = 1.1;
    private static final double NOT_ENOUGH_GAS_AMOUNT = 34.98;

    private static final String INVALID_GAS_TYPE_MESSAGE = "Gas type is null";
    private static final String INVALID_GAS_AMOUNT_MESSAGE = "Gas amount must be > 0";
    private static final String INVALID_MAX_PRICE_PER_LITER_MESSAGE = "Price can't be negative";
    private static final String NO_PUMP_FOR_GAS_TYPE_MESSAGE = "No gas pump for gas type: " + GasType.REGULAR;

    @BeforeEach
    void setUp() {
        testedInstance = new GasStationImpl();
        GasPump dieselPump = new GasPump(GasType.DIESEL, DIESEL_PUMP_AMOUNT);
        testedInstance.addGasPump(dieselPump);
        testedInstance.setPrice(GasType.DIESEL, DIESEL_PRICE);
    }

    @Test
    void shouldReturnCorrectTotalPrice_whenPurchaseIsSuccessful() throws GasTooExpensiveException, NotEnoughGasException {
        double totalPrice = testedInstance.buyGas(GasType.DIESEL, DIESEL_PUMP_AMOUNT, DIESEL_PRICE);

        assertEquals(SUCCESSFUL_PURCHASE_TOTAL_PRICE, totalPrice);
        assertEquals(SUCCESSFUL_PURCHASE_TOTAL_PRICE, testedInstance.getRevenue());
        assertEquals(1, testedInstance.getNumberOfSales());
    }

    @Test
    void shouldThrowGasTooExpensiveException_whenWillingPriceLess() {
        assertThrows(GasTooExpensiveException.class,
                () -> testedInstance.buyGas(GasType.DIESEL, DIESEL_PUMP_AMOUNT, EXPENSIVE_DIESEL_WILLING_PRICE));
        assertEquals(1, testedInstance.getNumberOfCancellationsTooExpensive());
    }

    @Test
    void shouldThrowNotEnoughGasException_whenWillingGasAmountHigher() {
        assertThrows(NotEnoughGasException.class,
                () -> testedInstance.buyGas(GasType.DIESEL, NOT_ENOUGH_GAS_AMOUNT, DIESEL_PRICE));
        assertEquals(1, testedInstance.getNumberOfCancellationsNoGas());
    }

    @Test
    void shouldThrowIllegalArgumentException_whenGasTypeIsNull() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> testedInstance.buyGas(null, DIESEL_PUMP_AMOUNT, DIESEL_PRICE));
        assertEquals(INVALID_GAS_TYPE_MESSAGE, thrown.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentException_whenPassedGasTypeIsNotSupported() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> testedInstance.buyGas(GasType.REGULAR, DIESEL_PUMP_AMOUNT, DIESEL_PRICE));
        assertEquals(NO_PUMP_FOR_GAS_TYPE_MESSAGE, thrown.getMessage());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-5.6, 0.0})
    void shouldThrowIllegalArgumentException_whenAmountInLitersIsInvalid(double amountInLiters) {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> testedInstance.buyGas(GasType.DIESEL, amountInLiters, DIESEL_PRICE));
        assertEquals(INVALID_GAS_AMOUNT_MESSAGE, thrown.getMessage());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-4.5, 0.0})
    void shouldThrowIllegalArgumentException_whenMaxPriceForLiterIsInvalid(double maxPricePerLiter) {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> testedInstance.buyGas(GasType.DIESEL, DIESEL_PUMP_AMOUNT, maxPricePerLiter));
        assertEquals(INVALID_MAX_PRICE_PER_LITER_MESSAGE, thrown.getMessage());
    }

    @Test
    void shouldSetPrice() {
        testedInstance.setPrice(GasType.SUPER, 5.7);

        assertEquals(5.7, testedInstance.getPrice(GasType.SUPER));
    }

    @Test
    void shouldReturnNewCollectionOfPumps() {
        Collection<GasPump> pumps = testedInstance.getGasPumps();
        pumps.clear();

        assertFalse(testedInstance.getGasPumps().isEmpty());
    }

}
