// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.SwitchesWiFi;
import ru.vachok.networker.services.MessageLocal;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.*;


public class SquidAvailabilityChecker implements Callable<String>, Runnable {
    
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    @Override public void run() {
        try {
            call();
        }
        catch (Exception e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".run", e));
        }
    }
    
    @Override public String call() throws Exception {
        return squidCheck();
    }
    
    private String squidCheck() throws ExecutionException, InterruptedException, IOException, TimeoutException {
        SSHFactory.Builder builder = new SSHFactory.Builder("srv-nat.eatmeat.ru", "ls", getClass().getSimpleName());
        
        InetAddress srvNatInetAddress = InetAddress.getByAddress(InetAddress.getByName(SwitchesWiFi.IPADDR_SRVNAT).getAddress());
        ExecutorService executorService = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor());
        Future<String> submitSSH = executorService.submit(builder.build());
        builder.setCommandSSH("sudo ps ax | grep squid && exit");
        
        String callChk = submitSSH.get(ConstantsFor.DELAY, TimeUnit.SECONDS);
        
        if (callChk.contains("ssl_crtd")) {
            messageToUser.info(FileSystemWorker.writeFile(getClass().getSimpleName() + ".log", callChk));
        }
        else if (srvNatInetAddress.isReachable(1000)) {
            builder.setCommandSSH("sudo squid && sudo ps ax | grep squid && exit");
            submitSSH = executorService.submit(builder.build());
            callChk = submitSSH.get(ConstantsFor.DELAY, TimeUnit.SECONDS);
        }
        else {
            messageToUser.error(FileSystemWorker.writeFile(getClass().getSimpleName() + ".log", callChk));
        }
        return callChk;
    }
    
}
