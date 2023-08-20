package org.unibl.etf.domain.passenger;

import java.io.Serializable;

public record DeniedPassengerReport(
      Passenger passenger,
      String vehicle,
      DenialReason denialReason) implements Serializable {
   public String toTextualRepresentation() {
      String textualRepresentation = "";
      // Passenger(Id) fields
      textualRepresentation += passenger.toTextualRepresentation() + "#";
      textualRepresentation += vehicle + "#";
      textualRepresentation += denialReason;
      return textualRepresentation;
   }

   public String getFormatted() {
      return passenger + ", vehicle: " + vehicle + ", denied for:" + denialReason;
   }
}
