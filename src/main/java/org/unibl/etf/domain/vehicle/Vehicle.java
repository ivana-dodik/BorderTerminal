package org.unibl.etf.domain.vehicle;

import com.github.javafaker.Faker;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unibl.etf.controller.SimulationController;
import org.unibl.etf.domain.passenger.*;
import org.unibl.etf.domain.terminal.BorderTerminal;
import org.unibl.etf.domain.terminal.CustomsTerminal;
import org.unibl.etf.domain.terminal.PoliceTerminal;
import org.unibl.etf.gui.VehicleButton;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Vehicle extends Thread implements Serializable {
   public static final BorderTerminal policeTerminal1 = new PoliceTerminal();
   public static final BorderTerminal policeTerminal2 = new PoliceTerminal();
   public static final BorderTerminal policeTerminalForTrucks = new PoliceTerminal();
   public static final BorderTerminal customsTerminal = new CustomsTerminal();
   public static final BorderTerminal customsTerminalForTrucks = new CustomsTerminal();
   protected static final Random RANDOM = new Random();
   private static final List<DeniedPassengerReport> passengersWithIllegalDocuments = new CopyOnWriteArrayList<>();
   private static final List<DeniedPassengerReport> passengersDeniedAtCustomsTerminal = new CopyOnWriteArrayList<>();
   @Serial
   private static final long serialVersionUID = 2023_08_02L;
   private static final Logger LOGGER = LoggerFactory.getLogger(Vehicle.class);
   private static final Faker faker = new Faker();
   protected static SimulationController simulationController;
   protected final List<Passenger> passengers;
   protected final int maxPassengers;
   private final String licensePlate;
   private final String vehicleType;
   protected int passengerProcessingTime;
   private Driver driver;
   private String iconClass;
   protected VehicleButton button;

   public Vehicle(int maxPassengers) {
      this.passengers = new ArrayList<>();
      this.maxPassengers = maxPassengers;
      this.passengerProcessingTime = 500;
      this.licensePlate = generateLicensePlate();
      this.vehicleType = this.getClass().getSimpleName();
      boardPassengers();
   }

   public static void setSimulationController(SimulationController simulationController) {
      Vehicle.simulationController = simulationController;
   }

   public static Optional<String> serializePassengersWithIllegalDocuments() {
      if (passengersWithIllegalDocuments.isEmpty()) {
         return Optional.empty();
      }

      String fileName = System.currentTimeMillis() + ".ser";

      try (var fos = new FileOutputStream(fileName);
           var oos = new ObjectOutputStream(fos)) {

         oos.writeObject(passengersWithIllegalDocuments);
         LOGGER.info("Serialization completed. File name: " + fileName);
         return Optional.of(fileName);
      } catch (IOException e) {
         LOGGER.error("Serialization failed: " + e.getMessage());
         return Optional.empty();
      }
   }

   public static List<DeniedPassengerReport> deserializePassengersWithIllegalDocuments(String filename) {
      try (var fis = new FileInputStream(filename);
           var ois = new ObjectInputStream(fis)) {

         @SuppressWarnings("unchecked")
         List<DeniedPassengerReport> deserializedReport = (List<DeniedPassengerReport>) ois.readObject();
         LOGGER.info("Deserialization completed.");

         return deserializedReport;
      } catch (IOException | ClassNotFoundException e) {
         LOGGER.error("Deserialization failed: " + e.getMessage());
      }

      return List.of();
   }

   public static Optional<String> writeReportAsTextDocument() {
      if (passengersDeniedAtCustomsTerminal.isEmpty()) {
         return Optional.empty();
      }

      String filename = System.currentTimeMillis() + ".txt";
      try (var writer = new BufferedWriter(new FileWriter(filename))) {

         for (DeniedPassengerReport report : passengersDeniedAtCustomsTerminal) {
            writer.write(report.toTextualRepresentation());
            writer.newLine();
         }

         LOGGER.info("Data written to the file: " + filename);
         return Optional.of(filename);
      } catch (IOException e) {
         LOGGER.error("Failed writing to text document: " + e.getMessage());
         return Optional.empty();
      }
   }

   public static List<DeniedPassengerReport> readReportFromTextDocument(String filename) {
      List<DeniedPassengerReport> deniedPassengerReports = new ArrayList<>();

      try {
         Files.readAllLines(Paths.get(filename)).forEach(line -> {
            String[] parts = line.split("#");
            var passenger = Passenger.fromString(parts[0]);
            var vehicleInfo = parts[1];
            var denialReason = DenialReason.valueOf(parts[2]);
            deniedPassengerReports.add(new DeniedPassengerReport(passenger, vehicleInfo, denialReason));
         });
      } catch (IOException e) {
         LOGGER.error("Failed at reading from text document: " + e.getMessage());
      } catch (IllegalArgumentException e) {
         LOGGER.error("Failed at reconstructing object from string: " + e.getMessage());
      }

      return deniedPassengerReports;
   }

   public String getVehicleType() {
      return vehicleType;
   }

   private String generateLicensePlate() {
      String countryCode = faker.address().countryCode().toUpperCase();

      String threeDigits1 = faker.number().digits(3);

      String letter = faker.regexify("[A-Z]");

      String threeDigits2 = faker.number().digits(3);

      return countryCode + " [" + threeDigits1 + "-" + letter + "-" + threeDigits2 + "]";
   }

   private void boardPassengers() {
      addDriver();
      addOtherPassengers();
   }

   private void addDriver() {
      this.driver = new Driver();
      passengers.add(driver);
   }

   protected void addOtherPassengers() {
      int numberOfOtherPassengers = RANDOM.nextInt(maxPassengers);

      for (int i = 0; i < numberOfOtherPassengers; i++) {
         passengers.add(new Passenger());
      }
   }

   private void reportIllegalDocuments(Passenger passenger) {
      passengersWithIllegalDocuments.add(
            new DeniedPassengerReport(passenger, this.toString(), DenialReason.ILLEGAL_DOCUMENTS));
   }

   private void reportStrandedPassengers() {
      for (int i = 1; i < passengers.size(); i++) {
         passengersWithIllegalDocuments.add(
               new DeniedPassengerReport(passengers.get(i), this.toString(), DenialReason.STRANDED_PASSENGER)
         );
      }
   }

   private void reportIllegalSuitcase(Passenger passenger) {
      passengersDeniedAtCustomsTerminal.add(
            new DeniedPassengerReport(passenger, this.toString(), DenialReason.ILLEGAL_SUITCASE));
   }

   protected void reportOverweightCargo() {
      passengers.forEach(passenger -> passengersDeniedAtCustomsTerminal.add(
            new DeniedPassengerReport(passenger, this.toString(), DenialReason.OVERWEIGHT_CARGO)));
   }

   protected boolean isIllegalDriver() throws InterruptedException {
      sleep(passengerProcessingTime);
      if (!driver.getPassengerId().isValid()) {
         reportIllegalDocuments(driver);
         reportStrandedPassengers();
         String msg = "Vehicle: " + this + ", couldn't pass police terminal as the driver had invalid documents.";
         LOGGER.warn(msg);
         Platform.runLater(() -> simulationController.logTextArea.appendText("\n" + msg));
         return true;
      }

      return false;
   }

   protected void checkOtherPassengersDocuments() throws InterruptedException {
      List<Passenger> passengersToRemove = new ArrayList<>();
      for (int i = 1; i < passengers.size(); i++) {
         sleep(passengerProcessingTime);
         var passenger = passengers.get(i);
         if (!passenger.getPassengerId().isValid()) {
            passengersToRemove.add(passenger);
            reportIllegalDocuments(passenger);
            String msg = "Removed passenger: " + passenger + ", because of invalid documents from: " + this;
            Platform.runLater(() -> simulationController.logTextArea.appendText("\n" + msg));
            LOGGER.warn(msg);
         }
      }

      // removed marked passengers
      passengers.removeAll(passengersToRemove);
   }

   protected void checkPassengerLuggage() throws InterruptedException {
      List<Passenger> passengersToRemove = new ArrayList<>();
      for (int i = 1; i < passengers.size(); i++) {
         sleep(passengerProcessingTime);
         var passenger = (BusPassenger) passengers.get(i);
         if (passenger.hasIllegalItems()) {
            passengersToRemove.add(passenger);
            reportIllegalSuitcase(passenger);
            String msg = "Removed passenger: " + passenger + ", because of illegal items in suitcase, from: " + this;
            LOGGER.warn(msg);
            Platform.runLater(() -> simulationController.logTextArea.appendText("\n" + msg));
         }
      }

      // removed marked passengers
      passengers.removeAll(passengersToRemove);
   }

   protected boolean processVehicle() throws InterruptedException {
      if (policeTerminal1.hasVehicle(this)) {
         if (isIllegalDriver()) {
            Platform.runLater(() -> {
               simulationController.firstPoliceTerminalBtn.releaseVehicle();
               simulationController.punishedLane.getChildren().add(button);
            });
            policeTerminal1.release();
            return false;
         }

         checkOtherPassengersDocuments();

         customsTerminal.acquire(this);

         String msg1 = "Processed: " + this + " at police terminal 1";
         String msg2 = "Forwarded to customs terminal: " + this;

         LOGGER.info(msg1);
         LOGGER.info(msg2);

         Platform.runLater(() -> {
            simulationController.firstPoliceTerminalBtn.releaseVehicle();
            simulationController.customsTerminalBtn.acquireVehicle(this);
            simulationController.logTextArea.appendText("\n" + msg1);
            simulationController.logTextArea.appendText("\n" + msg2);
         });

         policeTerminal1.release();
      } else if (policeTerminal2.hasVehicle(this)) {
         if (isIllegalDriver()) {
            Platform.runLater(() -> {
               simulationController.secondPoliceTerminalBtn.releaseVehicle();
               simulationController.punishedLane.getChildren().add(button);
            });
            policeTerminal2.release();
            return false;
         }

         checkOtherPassengersDocuments();

         customsTerminal.acquire(this);

         String msg1 = "Processed: " + this + " at police terminal 2";
         String msg2 = "Forwarded to customs terminal: " + this;

         LOGGER.info(msg1);
         LOGGER.info(msg2);

         Platform.runLater(() -> {
            simulationController.secondPoliceTerminalBtn.releaseVehicle();
            simulationController.customsTerminalBtn.acquireVehicle(this);
            simulationController.logTextArea.appendText("\n" + msg1);
            simulationController.logTextArea.appendText("\n" + msg2);
         });

         policeTerminal2.release();
      }

      return processAtCustoms();
   }

   protected abstract boolean processAtCustoms() throws InterruptedException;

   protected void releaseFromCustoms() {
      String msg = "Finished processing at customs terminal of: " + this;
      LOGGER.info(msg);
      Platform.runLater(() -> {
         simulationController.customsTerminalBtn.releaseVehicle();
         simulationController.logTextArea.appendText("\n" + msg);
      });
      customsTerminal.release();
   }

   @Override
   public void run() {
      boolean needsReleaseFromCustomsTerminal = false;
      try {
         needsReleaseFromCustomsTerminal = processVehicle();
      } catch (InterruptedException e) {
         LOGGER.error(e.getMessage());
      } finally {
         if (needsReleaseFromCustomsTerminal) {
            releaseFromCustoms();
         }
         simulationController.countDown(); // this thread has finished it's work, so we count down
      }
   }

   @Override
   public String toString() {
      return vehicleType + "[" + licensePlate + "]";
   }

   public String getDetailedInformation() {
      return "Vehicle type:" + vehicleType + "\n"
            + "License plate:" + licensePlate + "\n"
            + "Number of passengers:" + passengers.size() + "\n"
            + "Passengers:\n" + getDetailedInformationForPassengers();
   }

   private String getDetailedInformationForPassengers() {
      var sb = new StringBuilder();
      sb.append("[Driver] - ").append(driver.getDetailedInformation()).append(".\n");
      for (int i = 1; i < passengers.size(); i++) {
         var passenger = passengers.get(i);
         sb.append(passenger.getDetailedInformation()).append(".\n");
      }

      return sb.toString();
   }

   public String getIconClass() {
      return iconClass;
   }

   public void setIconClass(String iconClass) {
      this.iconClass = iconClass;
   }

   public void setButton(VehicleButton vehicleButton) {
      this.button = vehicleButton;
   }
}
