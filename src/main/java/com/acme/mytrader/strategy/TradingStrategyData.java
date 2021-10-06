package com.acme.mytrader.strategy;

public class TradingStrategyData {
    public enum Side {BUY, SELL};
    private final String security;
    private final double price;
    private final int size;
    private final Side side;

    public TradingStrategyData(String security, double price, int size, Side side) {
        this.security = security;
        this.price = price;
        this.size = size;
        this.side = side;
    }

    public String getSecurity() {
        return security;
    }

    public double getPrice() {
        return price;
    }

    public int getSize() {
        return size;
    }

    public Side getSide() {
        return side;
    }
}
