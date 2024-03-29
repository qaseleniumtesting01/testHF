// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.util.concurrent.*;


/**
 @since 16.06.2019 (9:00)
 @see SSHFactory
 */
public class SSHFactoryTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private SSHFactory sshFactory = new SSHFactory.Builder("192.168.13.42", "ls", getClass().getSimpleName()).build();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void testDirectCall() {
        try {
            Future<String> submit = Executors.newSingleThreadExecutor().submit(sshFactory);
            try {
                String sshCall = submit.get(ConstantsFor.DELAY, TimeUnit.SECONDS);
                Assert.assertTrue(sshCall.contains("!_passwords.xlsx"), sshCall);
            }
            catch (InterruptedException | ExecutionException | TimeoutException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    
    }
    
    @Test(enabled = false)
    public void testOverABSFactory() {
        Callable<String> sshFactory = new SSHFactory.Builder("192.168.13.42", "sudo ls", this.getClass().getSimpleName()).build();
        try {
            Assert.assertTrue(sshFactory.call().contains(".git<br>"));
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testTestToString() {
        String toStr = sshFactory.toString();
        Assert.assertTrue(toStr.contains("SSHFactory{"), sshFactory.toString());
    }
    
    @Test
    public void testCall() {
        Future<String> future = Executors.newSingleThreadExecutor().submit(sshFactory);
        try {
            String sshFuture = future.get(10, TimeUnit.SECONDS);
            System.out.println("sshFuture = " + sshFuture);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
}