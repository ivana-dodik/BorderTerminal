package org.unibl.etf.gui;

import org.unibl.etf.domain.vehicle.Vehicle;

public class BusButton extends VehicleButton {
   public BusButton(Vehicle vehicle) {
      super("", vehicle);
      getStyleClass().add("bus");
   }
}
