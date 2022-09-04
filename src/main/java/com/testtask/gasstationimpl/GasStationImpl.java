package com.testtask.gasstationimpl;

import com.testtask.util.AtomicDouble;
import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasStation;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GasStationImpl implements GasStation {

    private static final double DEFAULT_GAS_PRICE = 0.0;
    private final Map<GasType, Lock> gasTypeToLock = new ConcurrentHashMap<>();
    private final Map<GasType, Double> gasTypeToPrice = new ConcurrentHashMap<>();
    private final List<GasPump> pumps = new CopyOnWriteArrayList<>();

    private final AtomicInteger salesNumber = new AtomicInteger();
    private final AtomicInteger cancellationsNoGasNumber = new AtomicInteger();
    private final AtomicInteger cancellationsTooExpensiveNumber = new AtomicInteger();
    private final AtomicDouble revenue = new AtomicDouble();


    public GasStationImpl() {
        Stream.of(GasType.values())
                .forEach(type -> {
                    gasTypeToLock.put(type, new ReentrantLock());
                    gasTypeToPrice.put(type, DEFAULT_GAS_PRICE);
                });
    }

    @Override
    public void addGasPump(GasPump pump) {
        if (Objects.isNull(pump)) {
            throw new IllegalArgumentException("Gas pump is null");
        }
        if (Objects.isNull(pump.getGasType())) {
            throw new IllegalArgumentException("Gas type is null for gas pump");
        }
        pumps.add(pump);
    }

    @Override
    public Collection<GasPump> getGasPumps() {
        return pumps.stream()
                .map(p -> new GasPump(p.getGasType(), p.getRemainingAmount()))
                .collect(Collectors.toList());
    }

    @Override
    public double buyGas(GasType type, double amountInLiters, double maxPricePerLiter) throws NotEnoughGasException, GasTooExpensiveException {
        validateInput(type, amountInLiters, maxPricePerLiter);

        double pricePerLiter = getPrice(type);
        if (pricePerLiter > maxPricePerLiter) {
            cancellationsTooExpensiveNumber.getAndIncrement();
            throw new GasTooExpensiveException();
        }

        GasPump pump = pumps.stream()
                .filter(p -> p.getGasType().equals(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No gas pump for gas type: " + type));

        if (pump.getRemainingAmount() < amountInLiters) {
            cancellationsNoGasNumber.getAndIncrement();
            throw new NotEnoughGasException();
        }

        Lock pumpLock = gasTypeToLock.get(type);
        pumpLock.lock();

        double totalPrice;
        try {
            pump.pumpGas(amountInLiters);
            totalPrice = pricePerLiter * amountInLiters;
            revenue.add(totalPrice);
            salesNumber.getAndIncrement();
        } finally {
            pumpLock.unlock();
        }

        return totalPrice;
    }

    private void validateInput(GasType type, double amountInLiters, double maxPricePerLiter) {
        if (type == null) {
            throw new IllegalArgumentException("Gas type is null");
        }
        if (amountInLiters <= 0) {
            throw new IllegalArgumentException("Gas amount must be > 0");
        }
        if (maxPricePerLiter <= 0) {
            throw new IllegalArgumentException("Price can't be negative");
        }
    }

    @Override
    public double getRevenue() {
        return revenue.get();
    }

    @Override
    public int getNumberOfSales() {
        return salesNumber.get();
    }

    @Override
    public int getNumberOfCancellationsNoGas() {
        return cancellationsNoGasNumber.get();
    }

    @Override
    public int getNumberOfCancellationsTooExpensive() {
        return cancellationsTooExpensiveNumber.get();
    }

    @Override
    public double getPrice(GasType type) {
        if (Objects.isNull(type)) {
            throw new IllegalArgumentException("Gas typ is null");
        }
        return gasTypeToPrice.get(type);
    }

    @Override
    public void setPrice(GasType type, double price) {
        if (Objects.isNull(type)) {
            throw new IllegalArgumentException("Gas typ is null");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price can't be negative");
        }
        gasTypeToPrice.put(type, price);
    }
}
