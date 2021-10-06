package com.acme.mytrader.strategy;

import com.acme.mytrader.execution.ExecutionService;
import com.acme.mytrader.price.PriceSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.Collection;

import static com.acme.mytrader.strategy.TradingStrategyData.Side.BUY;
import static com.acme.mytrader.strategy.TradingStrategyData.Side.SELL;
import static java.lang.Double.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(Parameterized.class)
public class TradingStrategyTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    private PriceSource priceSource;
    @Mock
    private ExecutionService executionService;

    @Parameter(value=0)
    public String scenario;
    @Parameter(value=1)
    public TradingStrategyData strategyData;
    @Parameter(value=2)
    public boolean validStrategyData;
    @Parameter(value=3)
    public String feedSecurity;
    @Parameter(value=4)
    public double feedPrice;
    @Parameter(value=5)
    public boolean generateOrder;

    @Parameters(name = "{0}")
    public static Collection values() {
        return Arrays.asList(new Object[][]{
            {"Buy Strategy - Feed Price low - Create Order", new TradingStrategyData("IBM", 55.0, 100, BUY), true, "IBM", 50.0, true},
            {"Buy Strategy - Feed Price high - No Order", new TradingStrategyData("IBM", 55.0, 100, BUY), true, "IBM", 60.0, false},
            {"Buy Strategy - Different Security - No Order", new TradingStrategyData("IBM", 55.0, 100, BUY), true, "XXX", 50.0, false},
            {"Buy Strategy - Null Feed Security - No Order", new TradingStrategyData("IBM", 55.0, 100, BUY), true, null, 50.0, false},
            {"Buy Strategy - Negative Feed Price - No Order", new TradingStrategyData("IBM", 55.0, 100, BUY), true, "IBM", -1.0, false},
            {"Buy Strategy - Negative Zero Price - No Order", new TradingStrategyData("IBM", 55.0, 100, BUY), true, "IBM", 0.0, false},
            {"Buy Strategy - Invalid Feed Price 1 - No Order", new TradingStrategyData("IBM", 55.0, 100, BUY), true, "IBM", NaN, false},
            {"Buy Strategy - Invalid Feed Price 2 - No Order", new TradingStrategyData("IBM", 55.0, 100, BUY), true, "IBM", NEGATIVE_INFINITY, false},
            {"Buy Strategy - Invalid Feed Price 3 - No Order", new TradingStrategyData("IBM", 55.0, 100, BUY), true, "IBM", POSITIVE_INFINITY, false},
            {"Sell Strategy - Feed Price High - Create Order", new TradingStrategyData("IBM", 55.0, 100, SELL), true, "IBM", 60.0, true},
            {"Sell Strategy - Feed Price Low - No Order", new TradingStrategyData("IBM", 55.0, 100, SELL), true, "IBM", 50.0, false},
            {"Sell Strategy - Different Security - No Order", new TradingStrategyData("IBM", 55.0, 100, SELL), true, "XXX", 60.0, false},
            {"Sell Strategy - Null Feed Security - No Order", new TradingStrategyData("IBM", 55.0, 100, SELL), true, null, 60.0, false},
            {"Sell Strategy - Negative Feed Price - No Order", new TradingStrategyData("IBM", 55.0, 100, SELL), true, "IBM", -1.0, false},
            {"Sell Strategy - Negative Zero Price - No Order", new TradingStrategyData("IBM", 55.0, 100, SELL), true, "IBM", 0.0, false},
            {"Sell Strategy - Invalid Feed Price 1 - No Order", new TradingStrategyData("IBM", 55.0, 100, SELL), true, "IBM", NaN, false},
            {"Sell Strategy - Invalid Feed Price 2 - No Order", new TradingStrategyData("IBM", 55.0, 100, SELL), true, "IBM", NEGATIVE_INFINITY, false},
            {"Sell Strategy - Invalid Feed Price 3 - No Order", new TradingStrategyData("IBM", 55.0, 100, SELL), true, "IBM", POSITIVE_INFINITY, false},
            {"Invalid Strategy Data - Null Strategy Data", null, false, "IBM", 60.0, false},
            {"Invalid Strategy Data - Null Security", new TradingStrategyData(null, 55.0, 100, SELL), false, "IBM", 60.0, false},
            {"Invalid Strategy Data - Empty Security", new TradingStrategyData("", 55.0, 100, SELL), false, "IBM", 60.0, false},
            {"Invalid Strategy Data - Invalid Price 1", new TradingStrategyData("IBM", NaN, 100, SELL), false, "IBM", 60.0, false},
            {"Invalid Strategy Data - Invalid Price 2", new TradingStrategyData("IBM", POSITIVE_INFINITY, 100, SELL), false, "IBM", 60.0, false},
            {"Invalid Strategy Data - Invalid Price 3", new TradingStrategyData("IBM", NEGATIVE_INFINITY, 100, SELL), false, "IBM", 60.0, false},
            {"Invalid Strategy Data - Zero Price", new TradingStrategyData("IBM", 0.0, 100, SELL), false, "IBM", 60.0, false},
            {"Invalid Strategy Data - Negative Price", new TradingStrategyData("IBM", -10.0, 100, SELL), false, "IBM", 60.0, false},
            {"Invalid Strategy Data - Negative Size", new TradingStrategyData("IBM", 55.0, -1, SELL), false, "IBM", 60.0, false},
            {"Invalid Strategy Data - Zero Size", new TradingStrategyData("IBM", 55.0, 0, SELL), false, "IBM", 60.0, false},
            {"Invalid Strategy Data - Null Side", new TradingStrategyData("IBM", 55.0, 100, null), false, "IBM", 60.0, false},
       });
    }

    @Test
    public void strategyTest() {
        TradingStrategy strategy = null;
        try {
            strategy = new TradingStrategy(priceSource, executionService, strategyData);
        } catch (IllegalArgumentException ex) {
            if(validStrategyData) fail("Invalid");
        }
        if(validStrategyData) {
            assertNotNull(strategy);
            verify(priceSource).addPriceListener(eq(strategy));
            strategy.priceUpdate(feedSecurity, feedPrice);
            if(generateOrder) {
                if(strategyData.getSide() == BUY) {
                    verify(executionService).buy(strategyData.getSecurity(), feedPrice, strategyData.getSize());
                } else {
                    verify(executionService).sell(strategyData.getSecurity(), feedPrice, strategyData.getSize());
                }
                verify(priceSource).removePriceListener(eq(strategy));
            }
        }
        verifyNoMoreInteractions(priceSource, executionService);
    }
}
