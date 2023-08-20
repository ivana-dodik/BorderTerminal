package org.unibl.etf.domain.passenger;

import java.io.Serializable;
import java.util.Random;

public record Suitcase(boolean containsIllegalItems) implements Serializable {
   private static final Random RANDOM = new Random();

   public static Suitcase generateRandomSuitcase() {
      boolean containsIllegalItems = RANDOM.nextDouble() < 0.1;
      return new Suitcase(containsIllegalItems);
   }
}