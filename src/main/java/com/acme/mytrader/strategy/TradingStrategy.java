package com.acme.mytrader.strategy;

import com.acme.mytrader.execution.ExecutionService;
import com.acme.mytrader.price.PriceListener;
import com.acme.mytrader.price.PriceSource;

import java.util.Objects;

import static java.lang.Double.isInfinite;
import static java.lang.Double.isNaN;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * <pre>
 * User Story: As a trader I want to be able to monitor stock prices such
 * that when they breach a trigger level orders can be executed automatically
 * </pre>
 */
public class TradingStrategy implements PriceListener {
    private final PriceSource priceSource;
    private final ExecutionService executionService;
    private final TradingStrategyData tradingStrategyData;


    public TradingStrategy(PriceSource priceSource, ExecutionService executionService, TradingStrategyData tradingStrategyData) {
        this.priceSource = priceSource;
        this.executionService = executionService;
        this.tradingStrategyData = tradingStrategyData;
        validate(tradingStrategyData);
        priceSource.addPriceListener(this);
    }


    @Override
    public void priceUpdate(String security, double price) {
        if(nonNull(security) && validDouble(price)) {
            if (Objects.equals(tradingStrategyData.getSecurity(), security)) {
                if (tradingStrategyData.getSide() == TradingStrategyData.Side.BUY) {
                    if (price <= tradingStrategyData.getPrice()) {
                        executionService.buy(security, price, tradingStrategyData.getSize());
                        // assuming that we only want one execution
                        priceSource.removePriceListener(this);
                    }
                } else {
                    if (price >= tradingStrategyData.getPrice()) {
                        executionService.sell(security, price, tradingStrategyData.getSize());
                        // assuming that we only want one execution
                        priceSource.removePriceListener(this);
                    }
                }
            }
        }
    }

    private void validate(TradingStrategyData data) {
        if (isNull(data) || !validString(data.getSecurity()) || isNull(data.getSide()) || !validDouble(data.getPrice()) || data.getSize() <= 0) {
            throw new IllegalArgumentException("Invalid strategy data");
        }
    }

    private static boolean validDouble(double value) {
        return !(isNaN(value) || isInfinite(value) || value <= 0.0);
    }

    private static boolean validString(String string) {
        return nonNull(string) && !string.isEmpty();
    }
}
