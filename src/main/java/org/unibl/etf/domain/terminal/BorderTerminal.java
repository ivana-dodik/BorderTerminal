package org.unibl.etf.domain.terminal;


import org.unibl.etf.domain.vehicle.Vehicle;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class BorderTerminal {
   private final Semaphore semaphore;
   private Vehicle vehicle;
   private CountDownLatch latch;

   public BorderTerminal() {
      semaphore = new Semaphore(1);
      latch = new CountDownLatch(0);
   }

   public boolean tryAcquire(Vehicle vehicle) {
      boolean acquired = semaphore.tryAcquire();
      if (acquired) {
         this.vehicle = vehicle;
      }

      return acquired;
   }

   public void acquire(Vehicle vehicle) throws InterruptedException {
      if (latch.getCount() > 0) {
         latch.await();
      }
      semaphore.acquire();
      this.vehicle = vehicle;
   }

   public void release() {
      vehicle = null;
      semaphore.release();
   }

   public boolean hasVehicle(Vehicle vehicle) {
      return this.vehicle != null && this.vehicle.equals(vehicle);
   }

   public void block() {
      latch = new CountDownLatch(1); // Set the latch to 1, which will block the semaphore
   }

   public void resume() {
      latch.countDown(); // Release the latch to resume the semaphore
   }

   public boolean isWorking() {
      return latch.getCount() <= 0;
   }
}
