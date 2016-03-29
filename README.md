# Automated Survey Servlets
[![Build Status](https://travis-ci.org/TwilioDevEd/clicktocall-servlets.svg?branch=master)](https://travis-ci.org/TwilioDevEd/clicktocall-servlets)

An example application implementing Click to Call using Twilio.
An example application that implements an Automated Survey using Twilio.

[Read the full tutorial here](https://www.twilio.com/docs/tutorials/walkthrough/automated-survey/java/servlet)!

## Run the application

1. Clone the repository and `cd` into it.

1. The application uses Gradle to manage dependencies.

1. Edit the persistence configuration file `src/main/resources/persistence.xml` to match your configuration.

1. Configure Twilio to call your webhooks.

   You will need to configure Twilio to call your application when SMS and calls are received.

   You will need to provision at least one Twilio number with SMS and voice capabilities
   so the application's users can trigger the survey. You can buy a number [right
   here](//www.twilio.com/user/account/phone-numbers/search). Once you have
   a number you need to configure your number to work with your application. Open
   [the number management page](//www.twilio.com/user/account/phone-numbers/incoming)
   and open a number's configuration by clicking on it.

   ![Configure Voice](http://howtodocs.s3.amazonaws.com/twilio-number-config-all-med.gif)

1. Run the application using Gradle Gretty plugin.

   ```bash
   $ ./gradlew appRun
   ```

   This will run the embedded Jetty application server that uses port 8080.

1. Expose the application to the wider Internet using [ngrok](https://ngrok.com/)

   ```bash
   $ ngrok http 8080
   ```

   Once you have started ngrok, update your Twilio's number SMS and voice URL
   setting to use your ngrok hostname, so it will look something like
   this:

   ```
   http://<your-ngrok-subdomaon/automated-survey-servlets/survey
   ```

### Dependencies

This application uses this Twilio helper library.

* [twilio-java](//github.com/twilio/twilio-java)

### Run the tests

1. Run at the top-level directory.

   ```bash
   $ ./gradlew test
   ```

## Meta

* No warranty expressed or implied. Software is as is. Diggity.
* [MIT License](http://www.opensource.org/licenses/mit-license.html)
* Lovingly crafted by Twilio Developer Education.