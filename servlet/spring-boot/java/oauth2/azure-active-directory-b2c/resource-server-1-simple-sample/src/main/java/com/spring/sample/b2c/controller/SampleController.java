// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.spring.sample.b2c.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SampleController {

    @ResponseBody
    @GetMapping(value = { "/hello" })
    public String hello() {
        return "Hello, this is aad-b2c-resource-server-simple-sample.";
    }
}
