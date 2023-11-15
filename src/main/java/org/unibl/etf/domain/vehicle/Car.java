package org.unibl.etf.domain.vehicle;

public class Car extends Vehicle {
   private static final int MAX_PASSENGERS = 5;
   private static final int CAR_PROCESSING_TIME_CUSTOMS_TERMINAL = 2000;

   public Car() {
      super(MAX_PASSENGERS);
   }

   @Override
   protected boolean processAtCustoms() throws InterruptedException {
      sleep(CAR_PROCESSING_TIME_CUSTOMS_TERMINAL);
      return true;
   }
}
