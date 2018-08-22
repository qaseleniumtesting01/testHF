package ru.vachok.networker.web;


import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.logic.FtpCheck;
import ru.vachok.networker.logic.GoogleCred;
import ru.vachok.networker.logic.SaverByOlder;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static ru.vachok.networker.web.ApplicationConfiguration.logger;


/**
 The type Index controller.
 */
@Controller
public class IndexController {

   private static final Map<String, String> SHOW_ME = new ConcurrentHashMap<>();

   private static final int EXPIRY = 90;

   private MessageToUser messageToUser = new MessageCons();

   private Logger logger = ApplicationConfiguration.logger();
   private static final Connection c = new RegRuMysql().getDefaultConnection("u0466446_liferpg");


   /**
    Map to show map.

    @param httpServletRequest  the http servlet request
    @param httpServletResponse the http servlet response
    @return the map
    @throws IOException the io exception
    */
   @RequestMapping ("/docs")
   @ResponseBody
   public Map<String, String> mapToShow(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
      ExecutorService executorService = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor());
      Runnable r = new SaverByOlder(SHOW_ME);
      SHOW_ME.put("addr", httpServletRequest.getRemoteAddr());
      SHOW_ME.put("host", httpServletRequest.getRequestURL().toString());
      SHOW_ME.forEach((x, y) -> messageToUser.info(this.getClass().getSimpleName(), x, y));
      Stream<String> stringStream = addrInLocale(httpServletRequest, httpServletResponse);
      stringStream.forEach(x -> SHOW_ME.put("addrInLocale", x));
      SHOW_ME.put("status", httpServletResponse.getStatus() + " " + httpServletResponse.getBufferSize() + " buff");
      String s = httpServletRequest.getQueryString();
      if(s!=null){
         SHOW_ME.put(this.toString(), s);
         if(s.contains("go")) httpServletResponse.sendRedirect("http://ftpplus.vachok.ru/docs");
         executorService.execute(r);
      }
      executorService.execute(r);
      return SHOW_ME;
   }

   /**
    Addr in locale stream.

    @param httpServletRequest  the http servlet request
    @param httpServletResponse the http servlet response
    @return the stream
    @throws IOException the io exception
    */
   @RequestMapping ("/vir")
   @ResponseBody
   public Stream<String> addrInLocale(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
      String re = "redirect:https://vachok.testquality.com/project/3260/plan/6672/test/86686";
      Cookie cooki = new Cookie("hi", re);
      httpServletResponse.addCookie(cooki);
      ServletInputStream in = httpServletRequest.getInputStream();
      byte[] bs = new byte[0];
      while(in.isReady()){
         in.read(bs);
      }

      messageToUser.info("HTTP Servlets Controller", httpServletRequest.getServletPath() + re, "1 КБ resp: " + new String(bs, StandardCharsets.UTF_8));
      File[] files = new File("g:\\myEX\\").listFiles();
      int length = files.length;
      List<String> namesFile = new ArrayList<>();
      for(int i = 0; i < 10; i++){
         File file = files[new Random().nextInt(length)];
         namesFile.add(file.getAbsolutePath());
      }
      String s = LocalDateTime.of(2018, 10, 14, 7, 0).format(DateTimeFormatter.ofPattern("dd/MM/yy"));
      String command = "\"C:\\ProgramData\\Microsoft\\Windows\\Start Menu\\Programs\\wmplayer.exe\"";
      Runtime.getRuntime().exec(command);
      Map<String, String> getEnv = System.getenv();
      getEnv.forEach((x, y) -> namesFile.add(x + "////" + y));
      namesFile.add(re);
      namesFile.add(new String(bs, StandardCharsets.UTF_8));
      namesFile.add(s);
      namesFile.add(httpServletRequest.toString());
      namesFile.add(httpServletRequest.getSession().getServletContext().getServerInfo());
      namesFile.add(httpServletRequest.getSession().getServletContext().getServletContextName());
      namesFile.add(httpServletRequest.getSession().getServletContext().getVirtualServerName());
      namesFile.add(httpServletRequest.getSession().getServletContext().getContextPath());
      namesFile.add(Arrays.toString(httpServletResponse.getHeaderNames().toArray()));
      return namesFile.stream().sorted();
   }


   /**
    Exit app.

    @param httpServletRequest the http servlet request
    @throws IOException the io exception
    */
   @RequestMapping ("/stop")
   public void exitApp(HttpServletRequest httpServletRequest, HttpServletResponse response) throws IOException {
      String s = httpServletRequest.getRequestURL().toString();
      messageToUser.infoNoTitles(s);
      String q = httpServletRequest.getQueryString();
      if(q!=null){
         messageToUser.infoNoTitles(q);
         if(q.contains("full")) Runtime.getRuntime().exec("shutdown /p /f");
         if(q.contains("restart")) Runtime.getRuntime().exec("shutdown /r /f");
      }
      else{
         response.sendRedirect("http://10.10.111.57/e");
         System.exit(0);
      }
   }


   /**
    Index string.

    @param model   the model
    @param request the request
    @return the string
    */
   @RequestMapping (value = {"/", "/index"}, method = RequestMethod.GET)
   public String index(Model model, HttpServletRequest request) {
      String lastestSpeedInDB = getLastestSpeedInDB();
      String moneyGet = "";
      model.addAttribute("getMoney" , moneyGet);
      model.addAttribute("speed" , lastestSpeedInDB);
      long time = request.getSession().getCreationTime();
      String remoteAddr = request.getRemoteAddr();
      String q = request.getQueryString();
      new Visitor(time, remoteAddr);
      if(q!=null){
         messageToUser.infoNoTitles(q);
         if(q.contains("ftp")) new FtpCheck();
      }
      logger().info(new Date(time) + " was - " + remoteAddr);
      String message = null;
      try {
         message = "Привет землянин... Твоя сессия идёт " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - request.getSession().getCreationTime()) + " сек...<p>" + request.getSession().getMaxInactiveInterval() + " getMaxInactiveInterval<p>" + request.getSession().getId() + " ID сессии\n" + "запрошен URL: " + request.getRequestURL().toString() + " ; " + request.getSession().getServletContext().getServerInfo() + " servlet info; " + TimeUnit.MILLISECONDS.toDays(request.getSession().getCreationTime() - 1515233487000L) + " амбрелла...; ";
      } catch (Exception e) {
         ApplicationConfiguration.logger().error(e.getMessage() , e);
      }
      Cookie[] requestCookies = request.getCookies();
      File dirCOOK = new File("cook");
      boolean mkdir = dirCOOK.mkdir();
      Enumeration<String> attributeNames = request.getSession().getAttributeNames();
      StringBuilder sb = new StringBuilder();
      while(attributeNames.hasMoreElements()) sb.append(attributeNames.nextElement());
      if(requestCookies!=null){
         setCookies(requestCookies, dirCOOK, remoteAddr, mkdir, sb, model);
      }
      Stream<String> googleCred = new GoogleCred().getCred();
      model.addAttribute("message", message);
      logger().info("dirCOOK = " + dirCOOK.getAbsolutePath());
      String timeLeft = "Время - деньги ... ";
      LocalTime localDateTimeNow = LocalTime.now();
      LocalTime endLocalDT = LocalTime.parse("17:30");
      long totalDay = endLocalDT.toSecondOfDay() - LocalTime.parse("08:30").toSecondOfDay();
      long l = endLocalDT.toSecondOfDay() - localDateTimeNow.toSecondOfDay();
      model.addAttribute("date", new Date().toString());
      model.addAttribute("timeleft" , timeLeft + "" + l + "/" + totalDay + " sec left");
      model.addAttribute("google", Arrays.toString(googleCred.toArray()).replaceAll(", ", "<p>"));
      return "index";
   }


   private String getAttr( HttpServletRequest request ) {
      Enumeration<String> attributeNames = request.getServletContext().getAttributeNames();
      StringBuilder stringBuilder = new StringBuilder();
      while (attributeNames.hasMoreElements()) {
         stringBuilder.append(attributeNames.nextElement());
         stringBuilder.append("<p>");
         stringBuilder.append("\n");
      }
      return stringBuilder.toString();
   }


   private String getLastestSpeedInDB() {
      StringBuilder stringBuilder = new StringBuilder();
      try (PreparedStatement p = c.prepareStatement("select * from speed ORDER BY  speed.TimeStamp DESC LIMIT 0 , 1"); ResultSet r = p.executeQuery()) {

         while (r.next()) {
            stringBuilder.append(r.getDouble("speed")).append(" speed, ").append(r.getInt("road")).append(" road, ").append(r.getDouble("TimeSpend")).append(" min spend, ").append(r.getString("TimeStamp")).append(" NOW: ").append(new Date().toString());
         }
      } catch (SQLException e) {
         ApplicationConfiguration.logger().error(e.getMessage() , e);
      }
      return stringBuilder.toString();
   }


   private void setCookies(Cookie[] requestCookies, File dirCOOK, String remoteAddr, boolean mkdir, StringBuilder sb, Model model) {
      for(Cookie cookie : requestCookies){
         try{
            cookie.setDomain(InetAddress.getLocalHost().getHostName());
         }
         catch(UnknownHostException e){
            logger.error(MessageFormat.format("{0}\n{1}", e.getMessage(), Arrays.toString(e.getStackTrace()).replaceAll(", ", "\n")));
         }
         cookie.setMaxAge(EXPIRY);
         cookie.setPath(dirCOOK.getAbsolutePath());
         Runtime runtime = Runtime.getRuntime();
         cookie.setValue(remoteAddr + runtime.availableProcessors() + " processors\n" + runtime.freeMemory() + "/" + runtime.totalMemory() + " memory\n" + model.asMap().toString().replaceAll(", ", "\n"));
         cookie.setComment(remoteAddr + " ip\n" + sb.toString());
         if(mkdir){
            logger().info(dirCOOK.getAbsolutePath());
         }
         try(FileOutputStream outputStream = new FileOutputStream(dirCOOK.getAbsolutePath() + "\\cook" + System.currentTimeMillis() + ".txt")){
            String s = "Domain: " + cookie.getDomain() + " name: " + cookie.getName() +
                  " comment: " + cookie.getComment() + "\n" + cookie.getPath() + "\n" + cookie.getValue() + "\n" + new Date(System.currentTimeMillis());
            byte[] bytes = s.getBytes();
            outputStream.write(bytes, 0, bytes.length);
         }
         catch(IOException e){
            logger.error(MessageFormat.format("{0}\n{1}", e.getMessage(), Arrays.toString(e.getStackTrace()).replaceAll(", ", " ")));
         }
      }
   }

   @GetMapping ("/f")
   public String f() {
      return "redirect:http://10.10.111.57:8881/ftp";
   }

}
