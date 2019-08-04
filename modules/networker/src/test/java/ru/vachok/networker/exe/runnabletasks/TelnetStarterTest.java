// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import org.jetbrains.annotations.Contract;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.enums.OtherKnownDevices;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.concurrent.*;


public class TelnetStarterTest {
    
    
    private SocketAddress socketAddress;
    
    private Socket socket;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    @BeforeMethod
    public void setUp() {
        try {
            this.socketAddress = new InetSocketAddress(InetAddress.getByName(OtherKnownDevices.DO0213_KUDR), 9990);
        }
        catch (UnknownHostException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void startServer() {
        Runnable telnetStarter = new TelnetStarter();
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<?> future = executorService.submit(telnetStarter);
        Runnable runnable = new TelnetStarterTest.MyRunnable(future);
        Future<?> runMy = executorService.submit(runnable);
        try {
            runMy.get(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Assert.assertTrue(checkSocket());
        executorService.shutdownNow();
    }
    
    private boolean checkSocket() {
        try (Socket socketVar = new Socket()) {
            this.socket = socketVar;
            byte[] bufBytes = new byte[ConstantsFor.KBYTE];
            socket.connect(socketAddress);
            try (InputStream stream = socket.getInputStream();) {
                do {
                    stream.read(bufBytes);
                } while (stream.available() > 0);
            }
            String serverAns = new String(bufBytes);
            Assert.assertTrue(serverAns.contains("Press ENTER"));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            return false;
        }
        return socket.isConnected();
    }
    
    private static class MyRunnable implements Runnable {
        
        
        private final Future<?> future;
        
        @Contract(pure = true)
        MyRunnable(Future<?> future) {
            this.future = future;
        }
        
        @Override
        public void run() {
            try {
                future.get(3, TimeUnit.SECONDS);
            }
            catch (InterruptedException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
                Thread.currentThread().checkAccess();
                Thread.currentThread().interrupt();
            }
            catch (ExecutionException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
            catch (TimeoutException e) {
                Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
        }
    }
}