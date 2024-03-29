package ru.vachok.networker.sysinfo;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.util.Collections;


public class DatabaseInfoTest {
    
    
    private static final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(DatabaseInfoTest.class.getSimpleName(), System.nanoTime());
    
    private DatabaseInfo databaseInfo;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @BeforeMethod
    public void initField() {
        this.databaseInfo = new DatabaseInfo();
    }
    
    @Test
    public void testGetInfoAbout() {
        String databaseInfoInfoAbout = databaseInfo.getInfoAbout("mysql.slow_log");
        Assert.assertTrue(databaseInfoInfoAbout.contains("host: "), databaseInfoInfoAbout);
        Assert.assertTrue(databaseInfoInfoAbout.contains(ConstantsFor.TIME), databaseInfoInfoAbout);
        Assert.assertTrue(databaseInfoInfoAbout.contains("rows sent: "), databaseInfoInfoAbout);
        Assert.assertTrue(databaseInfoInfoAbout.contains(ConstantsFor.EXAMINED), databaseInfoInfoAbout);
        Assert.assertTrue(databaseInfoInfoAbout.contains("sql: "), databaseInfoInfoAbout);
        databaseInfoInfoAbout = databaseInfo.getInfoAbout("inetstats");
        Assert.assertTrue(databaseInfoInfoAbout.contains("Table: 10_200_213_85, engine: MyISAM, rows:"), databaseInfoInfoAbout);
        Assert.assertTrue(databaseInfoInfoAbout
                .contains("Table: inetstats, engine: MEMORY, rows: 0, data: 0 kilobytes, comment: Текущая интернет статистика."), databaseInfoInfoAbout);
    }
    
    @Test
    public void testSetClassOption() {
        databaseInfo.setClassOption("test");
        Assert.assertTrue(databaseInfo.toString().contains("test"));
    }
    
    @Test
    public void testGetInfo() {
        try {
            String dbInfo = databaseInfo.getInfo();
            System.out.println("uploadCollectionResult = " + dbInfo);
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void testUploadCollection() {
        try {
            int uploadCollectionResult = databaseInfo.uploadCollection(Collections.EMPTY_LIST, "test.test");
            System.out.println("uploadCollectionResult = " + uploadCollectionResult);
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void testDropTable() {
        try {
            boolean isDropTable = databaseInfo.dropTable("test.test");
            Assert.assertTrue(isDropTable);
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void testGetDataSource() {
        MysqlDataSource infoDataSource = databaseInfo.getDataSource();
        Assert.assertEquals(infoDataSource.getURL(), "jdbc:mysql://srv-inetstat.eatmeat.ru:3306/information_schema");
    }
    
    @Test
    public void testTestToString() {
        String toString = databaseInfo.toString();
        Assert.assertEquals(toString, "DatabaseInfo{option=null}");
    }
}