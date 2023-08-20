package org.unibl.etf.domain.passenger;

import java.util.Random;

public class BusPassenger extends Passenger {

   private static final Random RANDOM = new Random();
   private final Suitcase suitcase;

   public BusPassenger() {
      suitcase = createRandomSuitcase();
   }

   public BusPassenger(PassengerId passengerId) {
      super(passengerId);
      suitcase = createRandomSuitcase();
   }

   private Suitcase createRandomSuitcase() {
      boolean hasSuitcase = RANDOM.nextDouble() < 0.7;
      return hasSuitcase ? Suitcase.generateRandomSuitcase() : suitcase;
   }

   public boolean hasIllegalItems() {
      return suitcase != null && suitcase.containsIllegalItems();
   }

   @Override
   public String getDetailedInformation() {
      String detailedInfo = super.getDetailedInformation() + "; Has suitcase?: " + ((suitcase != null) ? "Yes" : "No");

      if (suitcase != null) {
         detailedInfo += "; Contains Illegal Items?: " + (hasIllegalItems() ? "Yes" : "No");
      }

      return detailedInfo;
   }
}
