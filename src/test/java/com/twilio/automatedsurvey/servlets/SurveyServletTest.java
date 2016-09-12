package com.twilio.automatedsurvey.servlets;

import com.twilio.automatedsurvey.survey.Question;
import com.twilio.automatedsurvey.survey.Survey;
import com.twilio.automatedsurvey.survey.SurveyRepository;
import com.twilio.twiml.TwiMLException;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SurveyServletTest {

    @Test
    public void shouldWelcomeAndRedirectRequest() throws TwiMLException, IOException {
        SurveyRepository surveyRepo = mock(SurveyRepository.class);
        when(surveyRepo.add(any(Survey.class))).thenReturn(new Survey("test"));

        ResponseWriter responseWriter = mock(ResponseWriter.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        SurveyServlet surveyServlet = new SurveyServlet(surveyRepo, responseWriter);

        surveyServlet.doGet(MockedHttpServletRequestFactory.getMockedRequestWithParameters(new HashMap<>()), response);

        verify(responseWriter, times(1)).writeIn(eq(response), anyString());
    }

    @Test
    public void shouldAllowTheUserToAnswerAQuestion() throws IOException {
        SurveyRepository surveyRepository = mock(SurveyRepository.class);
        Survey mockedSurvey = mock(Survey.class);
        when(surveyRepository.find(anyLong())).thenReturn(Optional.of(mockedSurvey));
        when(mockedSurvey.getNextQuestion(any(Question.class))).thenReturn(Optional.of(mock(Question.class)));

        SurveyServlet surveyServlet = new SurveyServlet(surveyRepository,
                mock(ResponseWriter.class));

        HttpServletRequest request = MockedHttpServletRequestFactory.getMockedRequestWithParameters(new HashMap<String, String>() {{
            put("survey", "1");
            put("question", "1");
            put("RecordingUrl", "the answer");
        }});

        surveyServlet.doPost(request, mock(HttpServletResponse.class));

        verify(mockedSurvey, times(1)).answerCall(request.getParameterMap());
    }
}
