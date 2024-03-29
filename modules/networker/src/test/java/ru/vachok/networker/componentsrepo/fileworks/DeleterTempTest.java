// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.fileworks;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.nio.file.Paths;


/**
 @see DeleterTemp
 @since 07.08.2019 (0:45) */
public class DeleterTempTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private DeleterTemp deleterTemp = new DeleterTemp(Paths.get("."));
    
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
    public void testToString() {
        String toStr = deleterTemp.toString();
        Assert.assertTrue(toStr.contains("DeleterTemp["), toStr);
    }
    
    @Test(enabled = false)
    public void testRun() {
        deleterTemp.run();
        Assert.assertTrue(new File("err").isDirectory());
        File[] errFiles = new File("err").listFiles();
        Assert.assertNotNull(errFiles);
        Assert.assertTrue(errFiles.length == 0);
    }
}