// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ModelAttributeNames;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


/**
 @see CommonCTRL
 @since 17.06.2019 (10:57) */
@SuppressWarnings("ALL") public class CommonCTRLTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
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
    public void testCommonGET() {
        Model model = new ExtendedModelMap();
        CommonSRV commonSRV = new CommonSRV();
        CommonCTRL ctrl = new CommonCTRL(new CommonSRV());
        ctrl.setCommonSRV(commonSRV);
        String commonGETStr = ctrl.commonGET(model);
        assertTrue(commonGETStr.equals(ModelAttributeNames.COMMON));
        assertEquals(model.asMap().get(ModelAttributeNames.COMMON), commonSRV);
        assertTrue(model.asMap().size() >= 3);
    }
    
    /**
     @see CommonCTRL#commonArchPOST(CommonSRV, Model)
     */
    @Test
    public void testCommonArchPOST() {
        Model model = new ExtendedModelMap();
        CommonSRV commonSRV = new CommonSRV();
        CommonCTRL ctrl = new CommonCTRL(new CommonSRV());
    
        commonSRV.setPerionDays("100");
        commonSRV.setPathToRestoreAsStr("\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Общая\\_IT_FAQ\\");
        Future<String> capFuture = AppComponents.threadConfig().getTaskExecutor().submit(()->ctrl.commonArchPOST(commonSRV, model));
        String commonArchPOSTStr = "";
        try {
            commonArchPOSTStr = capFuture.get(30, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        assertEquals(commonArchPOSTStr, ModelAttributeNames.COMMON);
        assertTrue(new File("CommonSRV.reStoreDir.results.txt").lastModified() > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
        
        commonSRV.setNullToAllFields();
        model.asMap().clear();
    
        commonSRV.setPerionDays("100");
        commonSRV.setPathToRestoreAsStr("\\\\srv-fs\\Common_new\\14_ИТ_служба\\Общая\\График отпусков 2019г  IT.XLSX");
        capFuture = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(()->ctrl.commonArchPOST(commonSRV, model));
        try {
            commonArchPOSTStr = capFuture.get(20, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        assertEquals(commonArchPOSTStr, ModelAttributeNames.COMMON);
        assertEquals(model.asMap().get("title"), "\\\\srv-fs\\Common_new\\14_ИТ_служба\\Общая\\График отпусков 2019г  IT.XLSX (100 дн.) ");
        assertTrue(new File("CommonSRV.reStoreDir.results.txt").lastModified() > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
    }
    
    /**
     @see CommonCTRL#commonSearch(CommonSRV, Model)
     */
    @Test
    public void testCommonSearch() {
        Model model = new ExtendedModelMap();
        CommonSRV commonSRV = new CommonSRV();
        CommonCTRL ctrl = new CommonCTRL(new CommonSRV());
        String commonSearchStr = ctrl.commonSearch(commonSRV, model);
        assertEquals(commonSearchStr, ModelAttributeNames.COMMON);
        assertEquals(model.asMap().get(ModelAttributeNames.COMMON), commonSRV);
        assertEquals(model.asMap().get("title"), ": - идёт поиск");
    }
}