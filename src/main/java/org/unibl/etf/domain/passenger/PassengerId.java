package org.unibl.etf.domain.passenger;

import com.github.javafaker.Faker;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public record PassengerId(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        UUID uniqueId,
        boolean isValid) implements Serializable {
   private static final Faker faker = new Faker();
   private static final Random RANDOM = new Random();

   public static PassengerId generateRandomPassengerId() {
      String firstName = faker.name().firstName();
      String lastName = faker.name().lastName();
      LocalDate dateOfBirth = convertDateToLocalDate(faker.date().birthday());
      UUID uniqueId = UUID.randomUUID();
      boolean isValid = RANDOM.nextDouble() >= 0.03;

      return new PassengerId(firstName, lastName, dateOfBirth, uniqueId, isValid);
   }

   public static PassengerId fromString(String line) {
      String[] parts = line.split(",");

      if (parts.length == 5) {
         String firstName = parts[0];
         String lastName = parts[1];
         LocalDate dateOfBirth = LocalDate.parse(parts[2]);
         UUID uniqueId = UUID.fromString(parts[3]);
         boolean isValid = Boolean.parseBoolean(parts[4]);

         return new PassengerId(firstName, lastName, dateOfBirth, uniqueId, isValid);
      } else {
         throw new IllegalArgumentException("Invalid format for PassengerId: " + line);
      }
   }

   private static LocalDate convertDateToLocalDate(Date date) {
      Instant instant = date.toInstant();
      return instant.atZone(ZoneId.systemDefault()).toLocalDate();
   }

   @Override
   public String toString() {
      return "Full name: " + firstName + " " + lastName
              + "; Date of birth: " + dateOfBirth
              + "; Valid Id?: " + ((isValid) ? "Yes" : "No");
   }

   public String toTextualRepresentation() {
      String dateOfBirthText = dateOfBirth.toString();
      String uniqueIdText = uniqueId.toString();
      String isValidText = String.valueOf(isValid);

      return String.join(",", firstName, lastName, dateOfBirthText, uniqueIdText, isValidText);
   }
}
