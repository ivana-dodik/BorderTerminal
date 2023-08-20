package org.unibl.etf.gui;

import org.unibl.etf.domain.vehicle.Vehicle;

public class CarButton extends VehicleButton {
   public CarButton(Vehicle vehicle) {
      super("", vehicle);
      getStyleClass().add("car");
   }
}
