package org.unibl.etf.domain.vehicle;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Truck extends Vehicle {
   private static final Logger LOGGER = LoggerFactory.getLogger(Truck.class);
   private static final int MAX_PASSENGERS = 3;

   private final Cargo cargo;

   public Truck() {
      super(MAX_PASSENGERS);
      this.cargo = Cargo.generateRandomCargo();
   }

   public Cargo getCargo() {
      return cargo;
   }

   @Override
   protected boolean processVehicle() throws InterruptedException {
      if (isIllegalDriver()) {
         Platform.runLater(() -> {
            simulationController.policeTerminalForTrucksBtn.releaseVehicle();
            simulationController.punishedLane.getChildren().add(button);
         });
         policeTerminalForTrucks.release();
         return false;
      }

      checkOtherPassengersDocuments();

      customsTerminalForTrucks.acquire(this);

      String msg1 = "Processed: " + this + " at police terminal for trucks";
      String msg2 = "Forwarded to customs terminal for trucks: " + this;
      LOGGER.info(msg1);
      LOGGER.info(msg2);

      Platform.runLater(() -> {
         simulationController.policeTerminalForTrucksBtn.releaseVehicle();
         simulationController.customsTerminalForTrucksBtn.acquireVehicle(this);
         simulationController.logTextArea.appendText("\n" + msg1);
         simulationController.logTextArea.appendText("\n" + msg2);
      });

      policeTerminalForTrucks.release();

      return processAtCustoms();
   }

   @Override
   protected void releaseFromCustoms() {
      String msg = "Finished processing at customs terminal for trucks: " + this;
      LOGGER.info(msg);
      Platform.runLater(() -> {
         simulationController.customsTerminalForTrucksBtn.releaseVehicle();
         simulationController.logTextArea.appendText("\n" + msg);
      });
      customsTerminalForTrucks.release();
   }

   @Override
   protected boolean processAtCustoms() throws InterruptedException {
      int totalProcessingTime = passengerProcessingTime * passengers.size();
      sleep(totalProcessingTime);
      if (this.getCargo().requiresDocuments()) {
         String msg = "Issued customs documents for: " + this;
         LOGGER.info(msg);
         Platform.runLater(() -> simulationController.logTextArea.appendText("\n" + msg));
      }

      if (this.getCargo().isOverloaded()) {
         reportOverweightCargo();
         String msg = this + ", couldn't pass the customs terminal, it's overloaded";
         LOGGER.warn(msg);
         Platform.runLater(() -> {
            simulationController.customsTerminalForTrucksBtn.releaseVehicle();
            simulationController.punishedLane.getChildren().add(button);
            simulationController.logTextArea.appendText("\n" + msg);
         });
         customsTerminalForTrucks.release();
         return false;
      }

      return true;
   }
}
