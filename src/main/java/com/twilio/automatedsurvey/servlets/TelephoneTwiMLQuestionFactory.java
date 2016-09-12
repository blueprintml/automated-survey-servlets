package com.twilio.automatedsurvey.servlets;

import com.twilio.automatedsurvey.servlets.twimlquestions.AbstractTwiMLQuestionFactory;
import com.twilio.automatedsurvey.survey.Question;
import com.twilio.twiml.Gather;
import com.twilio.twiml.Method;
import com.twilio.twiml.Pause;
import com.twilio.twiml.Record;
import com.twilio.twiml.Say;
import com.twilio.twiml.VoiceResponse;

public class TelephoneTwiMLQuestionFactory extends AbstractTwiMLQuestionFactory {

    @Override
    public VoiceResponse build(Long surveyId, Question question) {
        switch (question.getType()){
            case voice:
                return  buildVoiceMessage(surveyId, question,
                        "Record your answer after the beep and press the pound key when you are done.");
            case numeric:
                return buildNumericMessage(surveyId, question,
                        "For the next question select a number with the dial pad " +
                        "and then press the pound key");
            case yesno:
                return buildNumericMessage(surveyId, question,
                        "For the next question, press 1 for yes, and 0 for no. Then press the pound key.");
            default:
                throw new RuntimeException("Invalid question type");
        }
    }

    private VoiceResponse buildVoiceMessage(Long surveyId, Question question, String message) {
        return new VoiceResponse.Builder()
                .say(new Say.Builder(message).build())
                .pause(new Pause.Builder().build())
                .say(new Say.Builder(question.getBody()).build())
                .record(new Record.Builder()
                        .transcribe(true)
                        .transcribeCallback("survey?survey="+ surveyId +"&amp;question="+question.getId())
                        .action("survey?survey="+ surveyId +"&amp;question="+question.getId())
                        .method(Method.POST)
                        .maxLength(6)
                        .build()
                )
                .build();
    }

    private VoiceResponse buildNumericMessage(Long surveyId, Question question, String message) {
        return new VoiceResponse.Builder()
                .say(new Say.Builder(message).build())
                .pause(new Pause.Builder().build())
                .say(new Say.Builder(question.getBody()).build())
                .gather(new Gather.Builder()
                        .action("survey?survey=" + surveyId + "&amp;question=" + question.getId())
                        .method(Method.POST)
                        .finishOnKey("#")
                        .build()
                )
                .build();
    }
}
