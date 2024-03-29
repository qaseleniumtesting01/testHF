package ru.vachok.networker.data.synchronizer;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 @see SyncDataTest */
public abstract class SyncData implements DataConnectTo {
    
    
    private static final String UPUNIVERSAL = "DBUploadUniversal";
    
    public static final String INETSYNC = "InternetSync";
    
    static final DataConnectTo CONNECT_TO_REGRU = DataConnectTo.getRemoteReg();
    
    static final DataConnectTo CONNECT_TO_LOCAL = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
    
    static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, SyncData.class.getSimpleName());
    
    private static final String DOWNLOADER = "DBRemoteDownloader";
    
    public static final String BACKUPER = "BackupDB";
    
    private String idColName = ConstantsFor.DBCOL_IDREC;
    
    abstract String getDbToSync();
    
    public abstract void setDbToSync(String dbToSync);
    
    @Contract(pure = true)
    private String getIdColName() {
        return idColName;
    }
    
    public abstract void setOption(Object option);
    
    public void setIdColName(String idColName) {
        this.idColName = idColName;
    }
    
    public abstract String syncData();
    
    public abstract void superRun();
    
    @Override
    public abstract int uploadCollection(Collection stringsCollection, String tableName);
    
    @Override
    public boolean dropTable(String dbPointTable) {
        throw new TODOException("ru.vachok.networker.data.synchronizer.SyncData.dropTable( boolean ) at 20.09.2019 - (20:37)");
    }
    
    @Override
    public int createTable(String dbPointTable, List<String> additionalColumns) {
        throw new TODOException("ru.vachok.networker.data.synchronizer.SyncData.createTable( int ) at 04.11.2019 - (13:49)");
    }
    
    @Override
    public MysqlDataSource getDataSource() {
        MysqlDataSource source = CONNECT_TO_LOCAL.getDataSource();
        source.setDatabaseName(FileNames.DIR_INETSTATS);
        return source;
    }
    
    @Override
    public Connection getDefaultConnection(String dbName) {
        try {
            MysqlDataSource source = DataConnectTo.getDefaultI().getDataSource();
            source.setDatabaseName(dbName);
            return source.getConnection();
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 76 ***");
            return DataConnectTo.getDefaultI().getDefaultConnection(dbName);
        }
    }
    
    int getLastLocalID(String syncDB) {
        DataConnectTo dctInst = DataConnectTo.getInstance(DataConnectTo.TESTING);
        MysqlDataSource source = dctInst.getDataSource();
        return getDBID(source, syncDB);
    }
    
    private int getDBID(@NotNull MysqlDataSource source, String syncDB) {
        messageToUser.info(this.getClass().getSimpleName(), "Searchin for ID Rec", source.getURL());
        try (Connection connection = source.getConnection()) {
            final String sql = String.format("select %s from %s ORDER BY %s DESC LIMIT 1", getIdColName(), syncDB, getIdColName());
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    int retInt = 0;
                    while (resultSet.next()) {
                        if (resultSet.last()) {
                            retInt = resultSet.getInt(getIdColName());
                        }
                    }
                    return retInt;
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 169 ***");
            return -666;
        }
    }
    
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Contract("_ -> new")
    public static @NotNull SyncData getInstance(@NotNull String type) {
        switch (type) {
            case DOWNLOADER:
                return new DBRemoteDownloader(0);
            case UPUNIVERSAL:
                return new DBUploadUniversal(DataConnectTo.DBNAME_VELKOM_POINT);
            case INETSYNC:
                return new InternetSync("10.200.213.85");
            default:
                return new InternetSync(type);
        }
        
    }
    
    abstract Map<String, String> makeColumns();
    
    int getLastRemoteID(String syncDB) {
        return getDBID(DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDataSource(), syncDB);
    }
    
}
