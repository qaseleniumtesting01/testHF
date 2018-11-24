package ru.vachok.money;


import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.messenger.email.ESender;
import ru.vachok.money.components.AppVersion;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.config.ThreadConfig;
import ru.vachok.money.other.SystemTrayHelper;
import ru.vachok.mysqlandprops.EMailAndDB.SpeedRunActualize;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Objects;

import static org.springframework.boot.SpringApplication.run;


@EnableAutoConfiguration
@SpringBootApplication
public class MoneyApplication {


    /*Fields*/
    private static final SpringApplication SPRING_APPLICATION = new SpringApplication();

    public static void main(String[] args) {
        new SystemTrayHelper().addTrayDefaultMinimum();
        SPRING_APPLICATION.setLogStartupInfo(true);
        run(MoneyApplication.class, args);
        startSchedule();
    }

    private static void startSchedule() {
        ThreadPoolTaskExecutor defaultExecutor = new ThreadConfig().getDefaultExecutor();
        defaultExecutor.execute(new SpeedRunActualize());
        String msg = defaultExecutor.getThreadPoolExecutor().toString();
        defaultExecutor.createThread(() -> {
            new ESender("143500@gmail.com")
                .info(ConstantsFor.APP_NAME,
                    ConstantsFor.APP_NAME + ". Started at " + new Date(ConstantsFor.START_STAMP),
                    new AppVersion().toString() + "\n" + msg + "\n" + filesRnd());
        }).start();

        AppComponents.getLogger().warn(msg);
    }

    private static String filesRnd() {
        String localPc = ConstantsFor.localPc();
        if(localPc.equalsIgnoreCase("home")){
            File[] files = new File("g:\\myEX\\").listFiles();
            StringBuilder stringBuilder = new StringBuilder();
            for(int i = 0; i < 20; i++){
                stringBuilder
                    .append(files[new SecureRandom().nextInt(Objects.requireNonNull(files).length)].getName())
                    .append("\n");
            }
            try(FileOutputStream fileOutputStream = new FileOutputStream(new File("g:\\myEX\\files.txt"))){
                fileOutputStream.write(stringBuilder.toString().getBytes());
            }
            catch(IOException e){
                LoggerFactory.getLogger(MoneyApplication.class.getSimpleName()).error(e.getMessage(), e);
            }
            return stringBuilder.toString();
        }
        return localPc;
    }
}