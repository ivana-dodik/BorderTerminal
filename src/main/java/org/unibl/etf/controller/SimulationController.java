package org.unibl.etf.controller;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unibl.etf.domain.passenger.DeniedPassengerReport;
import org.unibl.etf.domain.vehicle.Bus;
import org.unibl.etf.domain.vehicle.Car;
import org.unibl.etf.domain.vehicle.Truck;
import org.unibl.etf.domain.vehicle.Vehicle;
import org.unibl.etf.gui.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulationController implements Initializable {
   private static final Logger LOGGER = LoggerFactory.getLogger(SimulationController.class);
   private static final String STATUS_FILE = "./status.txt";
   private static final String INITIAL_BORDER_STATUS = "11111";
   private static final String PAUSED_BORDER_STATUS = "00000";
   private static final int NUMBER_OF_VEHICLES_IN_MAIN_LANE = 5;
   private static final int NUMBER_OF_BUSES = 5;
   private static final int NUMBER_OF_TRUCKS = 10;
   private static final int NUMBER_OF_CARS = 35;
   public static Queue<Vehicle> vehicles;
   private final AtomicInteger seconds = new AtomicInteger(0);
   private final CountDownLatch countDownLatch = new CountDownLatch(NUMBER_OF_BUSES + NUMBER_OF_CARS + NUMBER_OF_TRUCKS);
   @FXML
   public BorderTerminalButton firstPoliceTerminalBtn;
   @FXML
   public BorderTerminalButton secondPoliceTerminalBtn;
   @FXML
   public BorderTerminalButton policeTerminalForTrucksBtn;
   @FXML
   public BorderTerminalButton customsTerminalBtn;
   @FXML
   public BorderTerminalButton customsTerminalForTrucksBtn;
   public TextArea logTextArea;
   private ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
   @FXML
   private Button startBtn;
   @FXML
   private TextArea reportTextArea;
   @FXML
   private Label simulationFinishedLabel;
   @FXML
   private Button pauseBtn;
   @FXML
   private Button resumeBtn;
   @FXML
   private Button restartBtn;
   @FXML
   private Button policeReportBtn;
   @FXML
   private Button customsReportBtn;
   @FXML
   private Button bothReportBtn;
   private ScheduledExecutorService timerExecutor;
   @FXML
   private Label elapsedSecondsLabel;
   @FXML
   private VBox mainLane;
   @FXML
   private VBox sideLane;
   public HBox punishedLane;

   private List<DeniedPassengerReport> policeReport;
   private List<DeniedPassengerReport> customsReport;

   private String getFormattedReport(List<DeniedPassengerReport> reports) {
      var sb = new StringBuilder();
      reports.forEach(r -> sb.append(r.getFormatted()).append("\n"));
      return sb.toString();
   }

   private String getReport(List<DeniedPassengerReport> reports, String terminal) {
      String report = "";

      if (policeReport.isEmpty()) {
         report += "No passengers where punished at the " + terminal.toLowerCase() + " terminal.";
      } else {
         report += terminal + " report:\n" + getFormattedReport(reports);
      }

      return report;
   }

   private void pauseTerminals() {
      File filePath = new File(STATUS_FILE);
      try {
         Files.writeString(filePath.toPath(), PAUSED_BORDER_STATUS);
      } catch (IOException e) {
         LOGGER.error("Couldn't pause terminals: " + e.getMessage());
      }
   }

   private void resumeTerminals() {
      File filePath = new File(STATUS_FILE);
      try {
         Files.writeString(filePath.toPath(), INITIAL_BORDER_STATUS);
      } catch (IOException e) {
         LOGGER.error("Couldn't resume terminals: " + e.getMessage());
      }
   }

   @Override
   public void initialize(URL url, ResourceBundle resourceBundle) {
      Vehicle.setSimulationController(this);
      initVehicles();

      pauseBtn.setOnMouseClicked(event -> {
         pauseBtn.setDisable(true);
         resumeBtn.setDisable(false);
         pauseTerminals();
      });

      resumeBtn.setOnMouseClicked(event -> {
         resumeBtn.setDisable(true);
         pauseBtn.setDisable(false);
         resumeTerminals();
      });

      restartBtn.setOnMouseClicked(event -> {
         pauseBtn.setDisable(true);
         restartBtn.setDisable(true);
         startBtn.setDisable(false);
         simulationFinishedLabel.setVisible(false);
         elapsedSecondsLabel.setText("");
         reportTextArea.setText("");
         reportTextArea.setVisible(false);
         customsReportBtn.setVisible(false);
         policeReportBtn.setVisible(false);
         bothReportBtn.setVisible(false);
         seconds.set(0);
         initVehicles();
         backgroundExecutor = Executors.newSingleThreadExecutor();
      });

      policeReportBtn.setOnMouseClicked(event -> {
         reportTextArea.setText(getReport(policeReport, "Police"));
         reportTextArea.setVisible(true);
      });

      customsReportBtn.setOnMouseClicked(event -> {
         reportTextArea.setText(getReport(customsReport, "Customs"));
         reportTextArea.setVisible(true);
      });

      bothReportBtn.setOnMouseClicked(event -> {
         String report = "";

         report += getReport(policeReport, "Police");
         report += "\n" + getReport(customsReport, "Customs");

         reportTextArea.setText(report);
         reportTextArea.setVisible(true);
      });
   }

   private void initVehicles() {
      List<Vehicle> vehicleList = new ArrayList<>();

      for (int i = 0; i < NUMBER_OF_BUSES; i++) {
         vehicleList.add(new Bus());
      }

      for (int i = 0; i < NUMBER_OF_TRUCKS; i++) {
         vehicleList.add(new Truck());
      }

      for (int i = 0; i < NUMBER_OF_CARS; i++) {
         vehicleList.add(new Car());
      }

      Collections.shuffle(vehicleList);
      fillMainLane(vehicleList);
      fillSideLane(vehicleList);
      vehicles = new ConcurrentLinkedQueue<>(vehicleList);
   }

   private void fillMainLane(List<Vehicle> vehicleList) {
      List<VehicleButton> mainLaneButtons = new ArrayList<>();
      for (int i = 0; i < NUMBER_OF_VEHICLES_IN_MAIN_LANE; i++) {
         addToLane(vehicleList, i, mainLane, mainLaneButtons, true);
      }
   }

   private void fillSideLane(List<Vehicle> vehicleList) {
      List<VehicleButton> sideLaneButtons = new ArrayList<>();
      for (int i = NUMBER_OF_VEHICLES_IN_MAIN_LANE; i < vehicleList.size(); i++) {
         addToLane(vehicleList, i, sideLane, sideLaneButtons, false);
      }
   }

   private void addToLane(List<Vehicle> vehicles, int index, VBox lane, List<VehicleButton> laneButtons, boolean bigger) {
      var vehicle = vehicles.get(index);
      VehicleButton vehicleButton;
      if (vehicle instanceof Car) {
         vehicleButton = new CarButton(vehicle);
      } else if (vehicle instanceof Bus) {
         vehicleButton = new BusButton(vehicle);
      } else {
         vehicleButton = new TruckButton(vehicle);
      }

      if (bigger) {
         vehicleButton.getStyleClass().remove("vehicle");
         vehicleButton.getStyleClass().add("mainLane_vehicle");
         vehicleButton.setBigIcon();
      } else {
         vehicleButton.setSmallIcon();
      }

      vehicle.setIconClass(vehicleButton.getBigIconClass());
      lane.getChildren().add(vehicleButton);
      laneButtons.add(vehicleButton);
      vehicle.setButton(vehicleButton);
   }

   public void start() {
      startBtn.setDisable(true);
      pauseBtn.setDisable(false);
      restartBtn.setDisable(true);
      backgroundExecutor.submit(this::startSimulation);
   }

   private void startSimulation() {
      File filePath = new File(STATUS_FILE);
      resumeTerminals();
      FileAlterationObserver observer = new FileAlterationObserver(filePath.getParentFile(), pathname -> pathname.equals(filePath));
      observer.addListener(new StatusFileListener());
      FileAlterationMonitor monitor = new FileAlterationMonitor(1000);
      monitor.addObserver(observer);
      FileMonitorService fileMonitorService = new FileMonitorService(monitor);
      fileMonitorService.start();
      startTimer();

      while (!vehicles.isEmpty()) {
         var vehicle = vehicles.peek();

         // pauseForEffect(500);

         if (vehicle instanceof Car || vehicle instanceof Bus) {
            if (Vehicle.policeTerminal1.isWorking() && Vehicle.policeTerminal1.tryAcquire(vehicle)) {
               String msg = "Processing at police terminal 1: " + vehicle;
               LOGGER.info(msg);
               Platform.runLater(() -> logTextArea.appendText("\n" + msg));
               vehicles.remove().start();
               Platform.runLater(() -> {
                  updateLanes();
                  firstPoliceTerminalBtn.acquireVehicle(vehicle);
               });
            } else if (Vehicle.policeTerminal2.isWorking() && Vehicle.policeTerminal2.tryAcquire(vehicle)) {
               String msg = "Processing at police terminal 2: " + vehicle;
               LOGGER.info(msg);
               Platform.runLater(() -> logTextArea.appendText("\n" + msg));
               vehicles.remove().start();
               Platform.runLater(() -> {
                  updateLanes();
                  secondPoliceTerminalBtn.acquireVehicle(vehicle);
               });
            }
         } else if (vehicle instanceof Truck) {
            if (Vehicle.policeTerminalForTrucks.isWorking() && Vehicle.policeTerminalForTrucks.tryAcquire(vehicle)) {
               String msg = "Processing at police terminal for trucks: " + vehicle;
               LOGGER.info(msg);
               Platform.runLater(() -> logTextArea.appendText("\n" + msg));
               vehicles.remove().start();
               Platform.runLater(() -> {
                  updateLanes();
                  policeTerminalForTrucksBtn.acquireVehicle(vehicle);
               });
            }
         }
      }

      try {
         countDownLatch.await();
      } catch (InterruptedException e) {
         LOGGER.error("Failed while waiting for threads to complete: " + e.getMessage());
      }

      timerExecutor.shutdownNow();
      Optional<String> optionalFilename = Vehicle.serializePassengersWithIllegalDocuments();
      optionalFilename.ifPresentOrElse(s -> policeReport = Vehicle.deserializePassengersWithIllegalDocuments(s),
              () -> policeReport = List.of());

      Optional<String> optionalTxtFilename = Vehicle.writeReportAsTextDocument();
      optionalTxtFilename.ifPresentOrElse(s -> customsReport = Vehicle.readReportFromTextDocument(s),
              () -> customsReport = List.of());

      Platform.runLater(() -> {
         simulationFinishedLabel.setVisible(true);
         policeReportBtn.setVisible(true);
         customsReportBtn.setVisible(true);
         bothReportBtn.setVisible(true);
         restartBtn.setDisable(false);
      });

      backgroundExecutor.shutdown();
   }

   private void startTimer() {
      timerExecutor = Executors.newSingleThreadScheduledExecutor();
      timerExecutor.scheduleAtFixedRate(this::updateTimerLabel, 1, 1, TimeUnit.SECONDS);
   }

   private void updateTimerLabel() {
      int updatedSeconds = seconds.incrementAndGet();
      Platform.runLater(() -> elapsedSecondsLabel.setText(String.valueOf(updatedSeconds)));
   }

   private void updateLanes() {
      var mainButtons = mainLane.getChildren();
      var sideButtons = sideLane.getChildren();
      if (!mainButtons.isEmpty()) {
         mainButtons.remove(0);
      }

      if (!sideButtons.isEmpty()) {
         var vehicleButton = (VehicleButton) sideButtons.remove(0);
         vehicleButton.getStyleClass().remove("vehicle");
         vehicleButton.getStyleClass().add("mainLane_vehicle");
         vehicleButton.enlargeIcon();
         mainButtons.add(vehicleButton);
      }
   }

   public void countDown() {
      countDownLatch.countDown();
   }

   static class StatusFileListener extends FileAlterationListenerAdaptor {
      private static final Logger LOGGER = LoggerFactory.getLogger(StatusFileListener.class);
      private String previousStatus = "11111";

      @Override
      public void onFileChange(File file) {
         try {
            boolean isFirstPoliceTerminalActivePrevious = previousStatus.charAt(0) == '1';
            boolean isSecondPoliceTerminalActivePrevious = previousStatus.charAt(1) == '1';
            boolean isPoliceTerminalForTrucksActivePrevious = previousStatus.charAt(2) == '1';
            boolean isCustomsTerminalActivePrevious = previousStatus.charAt(3) == '1';
            boolean isCustomsTerminalForTrucksActivePrevious = previousStatus.charAt(4) == '1';

            String currentStatus = Files.readString(file.toPath());
            boolean isFirstPoliceTerminalActiveCurrent = currentStatus.charAt(0) == '1';
            boolean isSecondPoliceTerminalActiveCurrent = currentStatus.charAt(1) == '1';
            boolean isPoliceTerminalForTrucksActiveCurrent = currentStatus.charAt(2) == '1';
            boolean isCustomsTerminalActiveCurrent = currentStatus.charAt(3) == '1';
            boolean isCustomsTerminalForTrucksActiveCurrent = currentStatus.charAt(4) == '1';

            if (isFirstPoliceTerminalActivePrevious != isFirstPoliceTerminalActiveCurrent) {
               if (isFirstPoliceTerminalActiveCurrent) {
                  LOGGER.warn("First Police Terminal turned ON.");
                  Vehicle.policeTerminal1.resume();
               } else {
                  LOGGER.warn("First Police Terminal turned OFF.");
                  Vehicle.policeTerminal1.block();
               }
            }

            if (isSecondPoliceTerminalActivePrevious != isSecondPoliceTerminalActiveCurrent) {
               if (isSecondPoliceTerminalActiveCurrent) {
                  LOGGER.warn("Second Police Terminal turned ON.");
                  Vehicle.policeTerminal2.resume();
               } else {
                  LOGGER.warn("Second Police Terminal turned OFF.");
                  Vehicle.policeTerminal2.block();
               }
            }

            if (isPoliceTerminalForTrucksActivePrevious != isPoliceTerminalForTrucksActiveCurrent) {
               if (isPoliceTerminalForTrucksActiveCurrent) {
                  LOGGER.warn("Police Terminal for Trucks turned ON.");
                  Vehicle.policeTerminalForTrucks.resume();
               } else {
                  LOGGER.warn("Police Terminal for Trucks turned OFF.");
                  Vehicle.policeTerminalForTrucks.block();
               }
            }

            if (isCustomsTerminalActivePrevious != isCustomsTerminalActiveCurrent) {
               if (isCustomsTerminalActiveCurrent) {
                  LOGGER.warn("Customs Terminal turned ON.");
                  Vehicle.customsTerminal.resume();
               } else {
                  LOGGER.warn("Customs Terminal turned OFF.");
                  Vehicle.customsTerminal.block();
               }
            }

            if (isCustomsTerminalForTrucksActivePrevious != isCustomsTerminalForTrucksActiveCurrent) {
               if (isCustomsTerminalForTrucksActiveCurrent) {
                  LOGGER.warn("Customs Terminal for Trucks turned ON.");
                  Vehicle.customsTerminalForTrucks.resume();
               } else {
                  LOGGER.warn("Customs Terminal for Trucks turned OFF.");
                  Vehicle.customsTerminalForTrucks.block();
               }
            }

            // Update the previousStatus to the currentStatus
            previousStatus = currentStatus;
         } catch (IOException e) {
            LOGGER.error("status.txt file couldn't be read: " + e.getMessage());
         }
      }
   }

   static class FileMonitorService extends Service<Void> {
      private final FileAlterationMonitor monitor;

      FileMonitorService(FileAlterationMonitor monitor) {
         this.monitor = monitor;
      }

      @Override
      protected Task<Void> createTask() {
         return new Task<>() {
            @Override
            protected Void call() throws Exception {
               // Start the monitor
               monitor.start();

               // Keep the monitor running in the background
               while (!isCancelled()) {
                  // Check for the cancellation flag periodically
                  Thread.sleep(1000);
               }

               // Stop the monitor and release resources
               monitor.stop();

               return null;
            }
         };
      }
   }
}
