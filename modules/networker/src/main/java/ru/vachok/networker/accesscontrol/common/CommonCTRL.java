package ru.vachok.networker.accesscontrol.common;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.services.TimeChecker;

import java.util.Date;

/**
 Контроллер для /AT_NAME_COMMON
 <p>

 @since 05.12.2018 (9:04) */
@Controller
public class CommonCTRL {

    private static final String AT_NAME_COMMON = "common";

    @Autowired
    private CommonSRV commonSRV;

    @GetMapping("/common")
    public String commonGET(Model model) {

        model.addAttribute(ConstantsFor.TITLE, new Date(new TimeChecker().call().getReturnTime()));
        model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getFooterUtext());
        model.addAttribute(AT_NAME_COMMON, commonSRV);

        return AT_NAME_COMMON;
    }

    @PostMapping("/commonarch")
    public String commonArchPOST(@ModelAttribute CommonSRV commonSRV, Model model) {
        this.commonSRV = commonSRV;
        model.addAttribute(AT_NAME_COMMON, commonSRV);
        model.addAttribute(ConstantsFor.TITLE, commonSRV.getDelFolderPath() + " (" + commonSRV.getPerionDays() + " дн.) ");
        model.addAttribute("result", commonSRV.reStoreDir());
        model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getFooterUtext());
        return AT_NAME_COMMON;
    }
}