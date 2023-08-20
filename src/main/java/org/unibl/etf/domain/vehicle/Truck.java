package org.unibl.etf.domain.vehicle;

public class Truck extends Vehicle {
   private static final int MAX_PASSENGERS = 3;

   private final Cargo cargo;

   public Truck() {
      super(MAX_PASSENGERS);
      this.cargo = Cargo.generateRandomCargo();
   }

   public Cargo getCargo() {
      return cargo;
   }
}
