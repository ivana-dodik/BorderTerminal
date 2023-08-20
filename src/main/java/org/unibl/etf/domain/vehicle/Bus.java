package org.unibl.etf.domain.vehicle;

import org.unibl.etf.domain.passenger.BusPassenger;

public class Bus extends Vehicle {
   private static final int PASSENGER_PROCESSING_TIME = 100;
   private static final int MAX_PASSENGERS = 52;

   public Bus() {
      super(MAX_PASSENGERS);
      this.passengerProcessingTime = PASSENGER_PROCESSING_TIME;
   }

   @Override
   protected void addOtherPassengers() {
      int numberOfOtherPassengers = RANDOM.nextInt(MAX_PASSENGERS);

      for (int i = 0; i < numberOfOtherPassengers; i++) {
         passengers.add(new BusPassenger());
      }
   }
}
