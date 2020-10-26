package net.cho20.neotv.server.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class HomeController {

    private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);

    @RequestMapping(value = "/")
    public RedirectView index(HttpServletRequest request) {
        RedirectView view = new RedirectView();
        boolean prod = request.getHeader("prod")!=null;
        LOG.info("Receiving request on {} , prod : {}", request.getLocalName(), prod);
        if(prod && request.getServletPath().equals("/")){
            view.setContextRelative(false);
            LOG.info("Redirecting to UI");
            view.setUrl("https://neotv-ui.cho20.synology.me");
        }else{
            LOG.info("Redirecting to /rest");
            view.setContextRelative(true);
            if(prod) {
                view.setEncodingScheme("https");
            }
            view.setUrl("/rest/");
        }
        return view;
    }

}
