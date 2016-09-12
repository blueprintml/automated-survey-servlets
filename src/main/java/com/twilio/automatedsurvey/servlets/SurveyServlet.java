package com.twilio.automatedsurvey.servlets;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.twilio.automatedsurvey.survey.Question;
import com.twilio.automatedsurvey.survey.Survey;
import com.twilio.automatedsurvey.survey.SurveyRepository;
import com.twilio.automatedsurvey.survey.loader.SurveyLoader;
import com.twilio.twiml.Body;
import com.twilio.twiml.Message;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.Method;
import com.twilio.twiml.Redirect;
import com.twilio.twiml.Say;
import com.twilio.twiml.TwiML;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

@Singleton
public class SurveyServlet extends HttpServlet {


    private static Logger LOGGER = LoggerFactory.getLogger(SurveyServlet.class);

    private SurveyRepository surveyRepo;
    private ResponseWriter responseWriter;

    @Inject
    public SurveyServlet(SurveyRepository surveyRepo, ResponseWriter responseWriter) {
        this.surveyRepo = surveyRepo;
        this.responseWriter = responseWriter;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);

        try {
            if (isSmsAnswer(request)) {
                String survey = session.getAttribute("lastSurvey").toString();
                String lastQuestion = session.getAttribute("lastQuestion").toString();

                redirectToAnswerEndpoint(response, survey, lastQuestion);
            } else {
                Survey newSurvey = createSurveyInstance();

                String message = String.format("Welcome to the %s survey", newSurvey.getTitle());
                if (isSms(request)) {
                    MessagingResponse messagingResponse = new MessagingResponse.Builder()
                            .message(new Message.Builder().body(new Body(message)).build())
                            .redirect(new Redirect.Builder()
                                    .url(String.format("question?survey=%s", newSurvey.getId()))
                                    .method(Method.GET)
                                    .build()
                            )
                            .build();
                    String toXml = messagingResponse.toXml();
                    LOGGER.info("response: {}", toXml);
                    responseWriter.writeIn(response, toXml);
                } else {
                    VoiceResponse voiceResponse = new VoiceResponse.Builder()
                            .say(new Say.Builder(message).build())
                            .redirect(new Redirect.Builder()
                                    .url(String.format("question?survey=%s", newSurvey.getId()))
                                    .method(Method.GET)
                                    .build()
                            )
                            .build();

                    String toXml = voiceResponse.toXml();
                    LOGGER.info("response: {}", toXml);
                    responseWriter.writeIn(response, toXml);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    private Survey createSurveyInstance() {
        SurveyLoader loader = new SurveyLoader("survey.json");
        return surveyRepo.add(loader.load());
    }

    private void redirectToAnswerEndpoint(HttpServletResponse response, String survey, String lastQuestion) throws TwiMLException, IOException {
        MessagingResponse messagingResponse = new MessagingResponse.Builder()
                .redirect(new Redirect.Builder()
                        .url(String.format("survey?survey=%s&question=%s", survey, lastQuestion))
                        .method(Method.POST)
                        .build()
                )
                .build();
        this.responseWriter.writeIn(response, messagingResponse.toXml());
    }

    private boolean isSmsAnswer(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        return session.getAttribute("lastSurvey") != null;
    }

    private boolean isSms(HttpServletRequest request) {
        return request.getParameter("MessageSid") != null;
    }

    @Override
    @Transactional
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Long surveyId = Long.parseLong(request.getParameter("survey"));

            Survey survey = surveyRepo.find(surveyId).orElseThrow(() -> new RuntimeException("Survey was not found"));

            Question answeredQuestion = isSms(request) ? survey.answerSMS(request.getParameterMap()) :
                    survey.answerCall(request.getParameterMap());

            surveyRepo.update(survey);

            Optional<Question> nextQuestion = survey.getNextQuestion(answeredQuestion);

            TwiML twiMLResponse = nextQuestion.map((Question q) -> buildRedirectTwiMLMessage(surveyId, q, request))
                    .orElse(buildThankYouTwiMLResponse(survey.getTitle(), request));


            String toXml = twiMLResponse.toXml();
            LOGGER.info("response: {}", toXml);
            responseWriter.writeIn(response, toXml);
        } catch (TwiMLException e) {
            throw new RuntimeException(e);
        }
    }

    private TwiML buildThankYouTwiMLResponse(String surveyTitle, HttpServletRequest request) {
        final String realMessage = String.format("Thank you for taking the %s survey. Good bye.", surveyTitle);
        if (isSms(request)) {
            return new MessagingResponse.Builder()
                    .message(new Message.Builder().body(new Body(realMessage)).build())
                    .build();
        } else {
            return new VoiceResponse.Builder()
                    .say(new Say.Builder(realMessage).build())
                    .build();
        }
    }

    private TwiML buildRedirectTwiMLMessage(Long surveyId, Question q, HttpServletRequest request) {
        final String url = String.format("question?survey=%s&question=%s", surveyId, q.getId());
        final Redirect redirect = new Redirect.Builder().url(url).method(Method.GET).build();
        if (isSms(request)) {
            return new MessagingResponse.Builder()
                    .redirect(redirect)
                    .build();
        } else {
            return new VoiceResponse.Builder()
                    .redirect(redirect)
                    .build();
        }
    }


}
