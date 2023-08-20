package org.unibl.etf.domain.passenger;

import java.io.Serializable;

public class Passenger implements Serializable {
   protected final PassengerId passengerId;

   public Passenger() {
      passengerId = PassengerId.generateRandomPassengerId();
   }

   public Passenger(PassengerId passengerId) {
      this.passengerId = passengerId;
   }

   public static Passenger fromString(String line) {
      var passengerId = PassengerId.fromString(line);
      return new Passenger(passengerId);
   }

   @Override
   public String toString() {
      return passengerId.firstName() + " " + passengerId.lastName();
   }

   public PassengerId getPassengerId() {
      return passengerId;
   }

   public String getDetailedInformation() {
      return passengerId.toString();
   }

   public String toTextualRepresentation() {
      return passengerId.toTextualRepresentation();
   }
}
