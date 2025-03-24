
1. 자바 SDK을 자바8로 해야 함

   gradle.properties 파일에서
   org.gradle.java.home=C:\\Program Files\\Android\\jdk\\jdk-8.0.302.8-hotspot\\jdk8u302-b08


2. 안드로이드 SDK 경로 문제

   local.properties 파일에서
   sdk.dir=C:\\Users\\odyssey\\AppData\\Local\\Android\\Sdk


3. build.gradle 파일 문제
    
    lintOptions {
        abortOnError false
    }

4. ShowMsgActivity.java 파일 문제
    super.onCreate(savedInstanceState); 추가





   