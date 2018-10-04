package ru.vachok.money.ctrls;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.money.components.AppFooter;
import ru.vachok.money.components.AppVersion;
import ru.vachok.money.config.AppCtx;

import javax.servlet.http.HttpServletRequest;


/**
 @since 27.09.2018 (0:01) */
@Controller
public class SysInfoCtrl {

    @GetMapping ("/sysinfo")
    public String gettingInfo(Model model, HttpServletRequest request) {
        AppVersion appVersion = new AppVersion();
        model.addAttribute("title", appVersion.getAppVBuild());
        model.addAttribute("ctxinfo", AppCtx.getCtxInfoFromRequest(request));
        model.addAttribute("ver", appVersion.toString());
        model.addAttribute("footer", new AppFooter().getTheFooter());
        return "sysinfo";
    }
}