/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/


/*
This is the code for the test controller, for testing
    1. the interactions between the Network Configuration manager and the ACA.
or
    2. the Alcor HTTP APIs.

Params:
0. test_against_ncm, executes the ncm_test if set to true, or executes the alcor_http_api_test if set to false.
*/
package com.futurewei.alcor.pseudo_controller;

import com.futurewei.alcor.pseudo_controller.alcor_http_api_test.alcor_http_api_test;
import com.futurewei.alcor.pseudo_controller.ncm_test.ncm_test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


import javax.annotation.PostConstruct;

@SpringBootApplication
public class pseudo_controller {

    @Value("${test_against_ncm:true}")
    Boolean test_against_ncm;

    @Autowired
    ncm_test n;

    @Autowired
    alcor_http_api_test h;

    public static void main(String[] args) {
        SpringApplication.run(pseudo_controller.class, args);
    }

    @PostConstruct
    private void runTest(){
        System.out.println("Running pseudo controller code!");

        if (test_against_ncm){
            n.run_test_against_ncm();
        }else{
            h.run_test_against_alcor_apis();
        }
        System.exit(0);
    }
}
