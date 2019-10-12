package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class TesterDB65SQLTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(TesterDB65SQLTest.class.getSimpleName(), System
        .nanoTime());
    
    private DataConnectTo dataConnectTo;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @BeforeMethod
    public void initDTC() {
        this.dataConnectTo = new TesterDB65SQL("test.test");
    }
    
    @Test
    public void testGetDataSource() {
        
        MysqlDataSource source = dataConnectTo.getDataSource();
        String urlInSource = source.getURL();
        Assert.assertEquals(urlInSource, "jdbc:mysql://server202.hosting.reg.ru:3306/test.test");
    }
    
    @Test
    @Ignore
    public void testToLocalVM() {
        try (Connection connection = dataConnectTo.getDefaultConnection("velkom")) {
            Assert.assertEquals(connection.getMetaData().getURL(), "jdbc:mysql://srv-mysql.home:3306/velkom");
            try (PreparedStatement preparedStatement = connection.prepareStatement("select * from velkom.home")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        System.out.println("resultSet = " + resultSet.getString("site"));
                    }
                }
            }
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (InvokeIllegalException e) {
            if (UsefulUtilities.thisPC().toLowerCase().contains("do0")) {
                Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
            else {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
        }
    }
}