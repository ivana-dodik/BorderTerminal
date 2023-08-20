package org.unibl.etf.gui;

import javafx.scene.control.Button;
import org.unibl.etf.domain.vehicle.Vehicle;

public class BorderTerminalButton extends Button {
   private String iconClass;

   public BorderTerminalButton() {
      super();
      getStyleClass().add("borderTerminal");
   }

   public void acquireVehicle(Vehicle vehicle) {
      this.iconClass = vehicle.getIconClass();
      getStyleClass().add(this.iconClass);
      getStyleClass().add(vehicle.getVehicleType().toLowerCase());
   }

   public void releaseVehicle() {
      getStyleClass().remove(this.iconClass);
      this.iconClass = null;
      getStyleClass().remove(getStyleClass().size() - 1);
   }

   public void block() {
   }
}
