package com.anze.CarCounter;

public class CarCounter {
    public static void main(String[] args) {
        CarCounterConsumer consumer = new CarCounterConsumer();
        consumer.startPolling();
    }
}