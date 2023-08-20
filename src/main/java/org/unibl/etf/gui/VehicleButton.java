package org.unibl.etf.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import org.unibl.etf.domain.vehicle.Vehicle;

import java.util.Random;

public class VehicleButton extends Button {
   private static final Random RANDOM = new Random();
   protected final Vehicle vehicle;
   private String smallIconClass;
   private String bigIconClass;

   public VehicleButton(String text, Vehicle vehicle) {
      super(text);
      this.vehicle = vehicle;
      getStyleClass().add("vehicle");
      pickRandomIcon();

      setOnMouseClicked(event -> {
         var alert = new Alert(Alert.AlertType.INFORMATION);

         alert.setTitle("Vehicle Information");
         alert.setHeaderText("Detailed information about the vehicle and it's passengers.");
         alert.setContentText(vehicle.getDetailedInformation());
         alert.showAndWait();
      });
   }

   public void setSmallIcon() {
      getStyleClass().add(smallIconClass);
   }

   public void enlargeIcon() {
      getStyleClass().remove(smallIconClass);
      getStyleClass().add(bigIconClass);
   }

   public void setBigIcon() {
      getStyleClass().add(bigIconClass);
   }

   private void pickRandomIcon() {
      String iconClass = "image_" + vehicle.getVehicleType().toLowerCase() + "_" + (1 + RANDOM.nextInt(7));
      smallIconClass = iconClass + "_small";
      bigIconClass = iconClass + "_big";
   }

   public String getBigIconClass() {
      return bigIconClass;
   }

}
