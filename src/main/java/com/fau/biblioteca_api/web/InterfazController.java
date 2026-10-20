package com.fau.biblioteca_api.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InterfazController {

    @GetMapping({
            "/",
            "/{p1:^(?!api$)[^.]*$}",
            "/{p1:^(?!api$)[^.]*$}/{p2:[^.]*$}",
            "/{p1:^(?!api$)[^.]*$}/{p2:[^.]*$}/{p3:[^.]*$}"
    })
    public String interfaz() {
        return "forward:/index.html";
    }
}
