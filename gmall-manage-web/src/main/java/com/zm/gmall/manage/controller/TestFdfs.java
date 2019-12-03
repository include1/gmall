package com.zm.gmall.manage.controller;

import org.junit.jupiter.api.Test;

public class TestFdfs {
    @Test
    public void test1(){
        String str = "154.454645.a.jpg";
        int i = str.lastIndexOf(".");
        String newStr = str.substring(i+1);
        System.out.println(newStr);
    }
}
