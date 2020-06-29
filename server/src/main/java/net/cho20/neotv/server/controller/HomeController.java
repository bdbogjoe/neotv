package net.cho20.neotv.server.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class HomeController {

    @RequestMapping(value = "/")
    public RedirectView index(HttpServletRequest request) {
        RedirectView view = new RedirectView();
        if(request.getRequestURI().contains("neotv")){
            view.setContextRelative(false);
            view.setUrl("https://neotv-ui.cho20.synology.me");
        }else{
            view.setContextRelative(true);
            view.setUrl("/rest/");
        }
        return view;
    }

}
