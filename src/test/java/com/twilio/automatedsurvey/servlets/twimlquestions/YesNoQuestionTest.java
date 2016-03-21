package com.twilio.automatedsurvey.servlets.twimlquestions;

import com.twilio.automatedsurvey.survey.Question;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.hamcrest.Matchers.hasXPath;
import static org.junit.Assert.assertThat;

public class YesNoQuestionTest {

    @Test
    public void shouldReturnYesNoQuestionTwiMLRepresentation() throws IOException, SAXException, ParserConfigurationException {
        YesNoQuestion question = new YesNoQuestion(new Question("Is that a question?", Question.QuestionTypes.yesno));

        String xml = question.toEscapedXML();
        Document document = XMlTestHelper.createDocumentFromXml(xml);
        Node response = document.getElementsByTagName("Response").item(0);
        assertThat(response, hasXPath("/Response/Say[text() = 'For the next question, press 1 for yes, and 0 for no. " +
                "Then press the pound key.']"));
        assertThat(response, hasXPath("/Response/Say[text() = 'Is that a question?']"));
        assertThat(response, hasXPath("/Response/Pause"));
        assertThat(response, hasXPath("/Response/Gather"));
    }
}