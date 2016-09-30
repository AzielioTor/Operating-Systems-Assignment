/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assignment;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aziel Shaw ~ 14847095
 */
public class Buffer {
    // SleepTime, NumProducers, NumConsumers
    public static void main(String[] args) {
        int SLEEPTIME = 0;
        int numProducers = 0;
        int numConsumers = 0;
        CircularBuffer<BufferItem> cb = new CircularBuffer(Constants.BUFFER_SIZE);
        
        try {
            SLEEPTIME = Integer.parseInt(args[0]);
            numProducers = Integer.parseInt(args[1]);
            numConsumers = Integer.parseInt(args[2]);
        } catch(Exception e) {
            System.out.println("Invalid command line arguments: " + e.getMessage());
            System.out.println("Initializing with default values. SleepTime: 20000" + 
                    " Number of producers and Consumers: 5");
            SLEEPTIME = 20000;
            numProducers = 5;
            numConsumers = 5;
        }
        
        System.out.println("SleepTime = " + SLEEPTIME);
        System.out.println("Number of producers: " + numProducers);
        System.out.println("Number of consumers: " + numConsumers);
        
        Thread[] producers = new Thread[numProducers];
        Thread[] consumers = new Thread[numConsumers];
        
        for(Thread p : producers) {
            p = new Thread(new Producer(Constants.MAX_INT_NUM, cb));
            p.start();
        }
        for(Thread c : consumers) {
            c = new Thread(new Consumer(cb));
            c.start();
        }
        
        try {
            Thread.sleep(SLEEPTIME);
        } catch (InterruptedException ex) {
            Logger.getLogger(Buffer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Something went wrong... " + ex.getMessage());
            System.exit(0);
        }
        System.exit(0);
    }
    
    public static class Producer implements Runnable {

        private int RAND_MAX;
        private Random rand;
        private CircularBuffer buffer;
        private int RAND_TIME = 5000;

        public Producer(int randMax, CircularBuffer buffer) {
            this.RAND_MAX = randMax;
            this.rand = new Random();
            this.buffer = buffer;;
        }

        private int makeNumber() {
            return rand.nextInt(RAND_MAX);
        }

        public void insertItem() {
            BufferItem item = new BufferItem(makeNumber());
            this.buffer.insert_item(item);
        }

        @Override
        public void run() {
            while(true) {
                try {
                    Thread.sleep(Math.abs(this.rand.nextInt(RAND_TIME)));
                } catch (InterruptedException ex) {
                    System.out.println("Something went wrong: " + ex.getMessage());
                }
                this.insertItem();
            }
        }
    }

    public static class Consumer implements Runnable {

        private CircularBuffer buffer;
        private Random rand;
        private int RAND_TIME = 5000;

        public Consumer(CircularBuffer buffer) {
            this.buffer = buffer;
            this.rand = new Random();
        }

        private void removeItem() {
            buffer.remove_item();
        }

        @Override
        public void run() {
            while(true) {
                try {
                    Thread.sleep(Math.abs(this.rand.nextInt(RAND_TIME)));
                } catch (InterruptedException ex) {
                    System.out.println("Something went wrong: " + ex.getMessage());
                }
                this.removeItem();
            }
        }
    }

    public static class BufferItem {

        private Integer data;

        public BufferItem(String s) throws NumberFormatException {
            this.data = new Integer(s);
        }

        public BufferItem(int value) {
            this.data = new Integer(value);
        }

        @Override
        public String toString() {
            return data.intValue() + "";
        }
    }

    public static class CircularBuffer<E> {
        // Buffer fields
        final int BUFFER_SIZE;
        int tail;
        int head;
        E[] circularBuffer;
        // Semaphore locking fields
        Semaphore spaces;
        Semaphore items;
        Semaphore lock;

        public CircularBuffer(int buffSize) {
            // Initialize Buffer
            BUFFER_SIZE = buffSize;
            circularBuffer = (E[]) new Object[BUFFER_SIZE];
            this.tail = 0;
            this.head = 0;
            // Initialize Semaphores
            spaces = new Semaphore(BUFFER_SIZE);
            items = new Semaphore(0);
            lock = new Semaphore(1);
        }

        public boolean isFull() {
            if(tail == head && circularBuffer[tail] != null)
                return true;
            return false;
        }

        public boolean isEmpty() {
            if(tail == head && circularBuffer[tail] == null)
                return true;
            return false;
        }

        public int insert_item(E item) {
            try {
                spaces.acquire();
                lock.acquire();
                try {
                    if(!isFull()) {
                        circularBuffer[head] = item;
                        head = (head + 1) % (BUFFER_SIZE);
                        items.release();
                        System.out.print("Producer Thread Producing: ");
                        System.out.println(item);
                    } else {
                        System.out.println("Error: Full");
                        return -1;
                    }
                } finally {
                    lock.release();
                }
            } catch (Exception ex) {
                System.out.println("Interrupted");
    //            Logger.getLogger(CircularBuffer.class.getName()).log(Level.SEVERE, null, ex);
    //            lock.release();
                return -1;
            } 
            return 0;
        }

        public int remove_item() {
            try {
                items.acquire();
                lock.acquire();
                try {
                    if(!isEmpty()) {
                        System.out.println("Consumer Removing Item: " + circularBuffer[tail].toString());
                        circularBuffer[tail] = null;
                        tail = (tail + 1) % (BUFFER_SIZE);
                        spaces.release();
                    } else {
                        System.out.println("Error: Empty");
                        return -1;
                    }
                } finally {
                    lock.release();
                }
            } catch(Exception ex) {
                return -1;
            }
            return 0;
        }
    }
}
