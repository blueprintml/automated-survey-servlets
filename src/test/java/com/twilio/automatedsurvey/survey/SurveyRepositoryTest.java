package com.twilio.automatedsurvey.survey;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.twilio.automatedsurvey.IntegrationTestHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class SurveyRepositoryTest {

    private SurveyRepository surveyRepository;
    private IntegrationTestHelper integrationTestHelper;

    @Before
    public void setup() {
        JpaPersistModule testPersistModule = configPersistModule();
        Injector injector = Guice.createInjector(testPersistModule);
        initPersistService(injector);
        surveyRepository = injector.getInstance(SurveyRepository.class);
        integrationTestHelper = injector.getInstance(IntegrationTestHelper.class);

        integrationTestHelper.cleanTable(Question.class);
        integrationTestHelper.cleanTable(Survey.class);
    }

    private JpaPersistModule configPersistModule() {
        JpaPersistModule testPersistModule = new JpaPersistModule("jpaUnit");

        final String databaseUrl = "jdbc:sqlite::memory:";
        final String databaseUser = "";
        final String databasePassword = "";

        testPersistModule.properties(new HashMap<String, String>(){{
            put("javax.persistence.jdbc.url", databaseUrl);
            put("javax.persistence.jdbc.user", databaseUser);
            put("javax.persistence.jdbc.password", databasePassword);
        }});
        return testPersistModule;
    }

    private void initPersistService(Injector injector) {
        PersistService instance = injector.getInstance(PersistService.class);
        instance.start();
    }

    @Test
    public void shouldProvideAnIdToAnAddedSurvey() {
        String newSurveyTitle = "new survey";
        Survey addedSurvey = surveyRepository.add(new Survey(newSurveyTitle));

        assertThat(addedSurvey.getId(), is(notNullValue()));
        assertThat(addedSurvey.getTitle(), is(newSurveyTitle));
    }

    @Test
    public void shouldBeAbleToReturnAllSurveysAdded() {
        Survey survey1 = new Survey("new survey1");
        Survey survey2 = new Survey("new survey2");
        givenThatSurveysExists(survey1, survey2);

        List<Survey> surveys = surveyRepository.all();

        assertThat(surveys.size(), is(2));
        assertThat(surveys, hasItem(hasProperty("title", is(survey1.getTitle()))));
        assertThat(surveys, hasItem(hasProperty("title", is(survey2.getTitle()))));
    }

    @Test
    public void shouldInformWhenItWasNotPossibleTooReturnAnSurvey() {
        Optional<Survey> last = surveyRepository.findLast();

        assertThat(last.isPresent(), is(false));
    }

    @Test
    public void shouldReturnLastSurvey() {
        final Survey firstSurvey = new Survey("first survey");
        final Survey lastSurvey = new Survey("last survey");
        givenThatSurveysExists(firstSurvey, lastSurvey);

        Optional<Survey> retrievedLastSurvey = surveyRepository.findLast();

        assertThat(retrievedLastSurvey.isPresent(), is(true));
        Survey testedSurvey = retrievedLastSurvey.get();
        assertThat(testedSurvey.getId(), is(lastSurvey.getId()));
        assertThat(testedSurvey.getTitle(), is(lastSurvey.getTitle()));
    }

    @Test
    public void shouldFindSurveyById() {
        Survey survey = new Survey("one survey");
        givenThatSurveysExists(survey);

        Optional<Survey> surveyFound = surveyRepository.find(survey.getId());

        assertThat(surveyFound.isPresent(), is(true));
        assertThat(surveyFound.get(), is(survey));
    }

    @Test
    public void shouldReturnEmptyWhenSurveyIsNotFound() {
        Optional<Survey> survey = surveyRepository.find(1L);

        assertThat(survey.isPresent(), is(false));
    }

    private void givenThatSurveysExists(Survey... surveys) {
        integrationTestHelper.startTransaction();
        for (Survey survey : surveys) {
            surveyRepository.add(survey);
        }
        integrationTestHelper.finishTransaction();
    }

}
