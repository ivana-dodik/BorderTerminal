package org.unibl.etf.gui;

import org.unibl.etf.domain.vehicle.Vehicle;

public class TruckButton extends VehicleButton {
   public TruckButton(Vehicle vehicle) {
      super("", vehicle);
      getStyleClass().add("truck");
   }
}
