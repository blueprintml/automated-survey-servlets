package com.twilio.automatedsurvey.servlets.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.GuiceServletContextListener;

import java.util.HashMap;

public class AutomatedSurveyGuiceServletConfig extends GuiceServletContextListener {

    @Override
    protected Injector getInjector() {

        JpaPersistModule persistModule = new JpaPersistModule("jpaUnit");
        persistModule.properties(new HashMap<String, String>() {{
            put("javax.persistence.jdbc.url", "jdbc:sqlite:survey.sqlite");
            put("javax.persistence.jdbc.user", "");
            put("javax.persistence.jdbc.password", "");
        }});

        return Guice.createInjector(persistModule, new AutomatedSurveyServletModule());
    }
}