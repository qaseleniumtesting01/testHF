// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.net.scanner.NetListsTest;


/**
 @see DatabaseUserSearcher
 @since 17.08.2019 (11:19) */
public class DatabaseUserSearcherTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
    InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.TYPE_SEARCHDB);
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testTestToString() {
        String toStr = informationFactory.toString();
        Assert.assertTrue(toStr.contains("DatabaseUserSearcher{"), toStr);
    }
    
    @Test
    public void testGetInfo() {
        try {
            String gettedInfo = informationFactory.getInfo();
            System.out.println("gettedInfo = " + gettedInfo);
        }
        catch (TODOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetUserPCFromDB() {
        String kudrPC = ((DatabaseInfo) informationFactory).getUserByPCNameFromDB("do0213");
        System.out.println("kudrPC = " + kudrPC);
    }
    
    @Test
    public void testGetCurrentPCUsers() {
        String do0045 = ((DatabaseInfo) informationFactory).getCurrentPCUsers("do0045");
        Assert.assertTrue(do0045.contains("\\\\do0045\\c$\\users"), do0045);
    }
    
    @Test
    public void testGetInfoAbout() {
        try {
            String infoAbout = informationFactory.getInfoAbout("do0213");
            System.out.println("infoAbout = " + infoAbout);
        }
        catch (TODOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}