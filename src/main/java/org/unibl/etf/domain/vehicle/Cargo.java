package org.unibl.etf.domain.vehicle;

import java.util.Random;

public record Cargo(boolean requiresDocuments, int declaredWeight, int actualWeight) {
   private final static int MAX_DECLARED_WEIGHT = 5000;
   private static final Random RANDOM = new Random();

   public static Cargo generateRandomCargo() {
      boolean requiresDocuments = RANDOM.nextDouble() < 0.5;
      int declaredWeight = RANDOM.nextInt(MAX_DECLARED_WEIGHT) + 1;
      int actualWeight = calculateActualWeight(declaredWeight);

      return new Cargo(requiresDocuments, declaredWeight, actualWeight);
   }

   private static int calculateActualWeight(int declaredWeight) {
      Random random = new Random();
      int randomPercentage = random.nextInt(31);
      double multiplier = 1.0 + (double) randomPercentage / 100.0;

      if (random.nextInt(5) == 0) {
         return (int) Math.round(declaredWeight * multiplier);
      } else {
         return declaredWeight;
      }
   }

   public boolean isOverloaded() {
      return actualWeight > declaredWeight;
   }
}
