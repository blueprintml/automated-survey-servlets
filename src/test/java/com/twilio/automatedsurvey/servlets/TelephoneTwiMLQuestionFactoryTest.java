package com.twilio.automatedsurvey.servlets;

import com.twilio.automatedsurvey.servlets.twimlquestions.AbstractTwiMLQuestionFactory;
import com.twilio.automatedsurvey.servlets.twimlquestions.XMlTestHelper;
import com.twilio.automatedsurvey.survey.Question;
import com.twilio.automatedsurvey.survey.QuestionTypes;
import com.twilio.twiml.TwiML;
import com.twilio.twiml.TwiMLException;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.hamcrest.Matchers.hasXPath;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class TelephoneTwiMLQuestionFactoryTest {

    @Test
    public void shouldReturnVoiceQuestionTwiMLRepresentation() throws ParserConfigurationException, IOException, SAXException, TwiMLException {
        final Question voiceQuestion = new Question("Is that a voiceQuestion?", QuestionTypes.voice);
        TwiML twiMLVoiceQuestion = TelephoneTwiMLQuestionFactory
                .getInstance(mock(HttpServletRequest.class))
                .build(1l, voiceQuestion);

        String xml = twiMLVoiceQuestion.toXml();

        Document xmlDocument = XMlTestHelper.createDocumentFromXml(xml);

        Node response = xmlDocument.getElementsByTagName("Response").item(0);
        assertThat(response, hasXPath("/Response/Say[text() = 'Record your answer after the " +
                "beep and press the pound key when you are done.']"));
        assertThat(response, hasXPath("/Response/Say[text() = 'Is that a voiceQuestion?']"));
        assertThat(response, hasXPath("/Response/Pause"));
        assertThat(response, hasXPath("/Response/Record"));
    }

    @Test
    public void shouldReturnNumericQuestionTwiMLRepresentation() throws IOException, SAXException,
            ParserConfigurationException, TwiMLException {

        final Question numericQuestion = new Question("Is that a question?", QuestionTypes.numeric);
        AbstractTwiMLQuestionFactory questionFactory = TelephoneTwiMLQuestionFactory
                .getInstance(mock(HttpServletRequest.class));
        TwiML twiMLNumericQuestion = questionFactory.build(1L, numericQuestion);

        String xml = twiMLNumericQuestion.toXml();

        Document document = XMlTestHelper.createDocumentFromXml(xml);

        Node responseNode = document.getElementsByTagName("Response").item(0);
        assertThat(responseNode, hasXPath("/Response/Say[text() = 'For the next question select a number with " +
                "the dial pad and then press the pound key']"));
        assertThat(responseNode, hasXPath("/Response/Say[text() = 'Is that a question?']"));
        assertThat(responseNode, hasXPath("/Response/Pause"));
        assertThat(responseNode, hasXPath("/Response/Gather"));
    }

    @Test
    public void shouldReturnYesNoQuestionTwiMLRepresentation() throws IOException, SAXException, ParserConfigurationException, TwiMLException {
        final Question yesNoQuestion = new Question("Is that a yesNoQuestion?", QuestionTypes.yesno);
        AbstractTwiMLQuestionFactory questionFactory = TelephoneTwiMLQuestionFactory
                .getInstance(mock(HttpServletRequest.class));
        TwiML twiMLYesNoQuestion = questionFactory.build(1l, yesNoQuestion);

        String xml = twiMLYesNoQuestion.toXml();
        Document document = XMlTestHelper.createDocumentFromXml(xml);
        Node response = document.getElementsByTagName("Response").item(0);
        assertThat(response, hasXPath("/Response/Say[text() = 'For the next question, press 1 for yes, and 0 for no. " +
                "Then press the pound key.']"));
        assertThat(response, hasXPath("/Response/Say[text() = 'Is that a yesNoQuestion?']"));
        assertThat(response, hasXPath("/Response/Pause"));
        assertThat(response, hasXPath("/Response/Gather"));
    }
}
