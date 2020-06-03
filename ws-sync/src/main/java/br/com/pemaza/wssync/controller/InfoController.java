package br.com.pemaza.wssync.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoController {
    @Value("${build.version}")
    String version;

    @RequestMapping(value = "/version", method = RequestMethod.GET)
    public String getVersion() {
        return version;
    }
}
