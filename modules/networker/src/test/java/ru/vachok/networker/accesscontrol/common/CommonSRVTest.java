// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;


/**
 @since 17.06.2019 (15:02) */
@SuppressWarnings("ALL") public class CommonSRVTest {
    
    
    @Test
    public void testSearchByPat() {
        String searchInCommonResult = new CommonSRV().searchByPat("График отпусков:14_ИТ_служба\\Общая");
        String searchInCommonResult1 = new CommonSRV().searchByPat(":");
        assertTrue(searchInCommonResult.contains("График отпусков 2019г  IT.XLSX"), searchInCommonResult);
        assertTrue(searchInCommonResult.contains("График отпусков 2019г. SA.xlsx"), searchInCommonResult);
        assertTrue(searchInCommonResult1.contains("Bytes in stream:"));
    }
    
    @Test
    public void testReStoreDir() { //todo 23.06.2019 (1:18) optimize IT!
        final CommonSRV commSrv = new CommonSRV();
        String reStoreDirResult = commSrv.reStoreDir();
        assertTrue(reStoreDirResult.contains("TERMINATE"));
        commSrv.setPathToRestoreAsStr("\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Общая\\testClean\\testClean0.virus");
        commSrv.reStoreDir();
    }
}