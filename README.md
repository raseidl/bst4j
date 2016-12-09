<p align="center">
  <a href="https://bespoken.tools/">
    <img alt="bst4j" src="https://bespoken.tools/assets/bst-cli-9f9b8d685e450d33985b23d86505ffd7217635305f126625bc992b0865ff7a4d.png" width="546">
  </a>
</p>

<p align="center">
  Switch to BEAST mode<br>
  Rampage through code/test iterations for Alexa and Lambda development<br>
  For Java!
</p>

<p align="center">
    <a href="https://www.javadoc.io/doc/tools.bespoken/bst4j">
        <img src="https://www.javadoc.io/badge/tools.bespoken/bst4j.svg" alt="Javadocs">
    </a>
</p>

---
This an Early Access version of our bst tools for Java. It provides:
* Logging
* Monetization

## Getting Started
### Maven
To include in a Maven project, add the following to your pom.xml:  
```
<dependency>
    <groupId>tools.bespoken</groupId>
    <artifactId>bst4j</artifactId>
    <version>RELEASE</version>
</dependency>
```
More details to come.

### Gradle
```
compile 'tools.bespoken:bst4j:+'
```

## Logless
JavaDocs for Logless are [here.](https://static.javadoc.io/tools.bespoken/bst4j/0.1.18/tools/bespoken/logless/Logless.html)

### Logless Lambda
To capture logs from a Speechlet running inside a Lambda, simply call:  
```
    public HelloWorldSpeechletRequestStreamHandler() {
        super(Logless.capture("292fbf19-61fd-4ec6-8a8d-60fea5193904", new HelloWorldSpeechlet()),
                supportedApplicationIds);
    }
```
This constructor wraps the creation of the Speechlet before it is passed to the Lambda handler.

An example can be found in our bst4jSample project [here](https://github.com/bespoken/bst4jSample/blob/master/src/main/java/tools/bespoken/sample/HelloWorldSpeechletRequestStreamHandler.java#L39)

### Logless Servlet
To capture logs from a Speechlet running inside a Java servlet (i.e., as a standalone server), simply call:  
```
    Speechlet wrapper = Logless.capture("292fbf19-61fd-4ec6-8a8d-60fea5193904", 
        new HelloWorldSpeechlet());
    context.addServlet(new ServletHolder(createServlet(wrapper)), "/");
```

An example can be found in our bst4jSample project [here.](https://github.com/bespoken/bst4jSample/blob/master/src/main/java/tools/bespoken/sample/Launcher.java#L58)

## BSTMonetize
JavaDocs for BSTMonetize are [here.](https://static.javadoc.io/tools.bespoken/bst4j/0.1.18/tools/bespoken/client/BSTMonetize.html)

To use it, just call [BSTMonetize.injectSSML](https://static.javadoc.io/tools.bespoken/bst4j/0.1.18/tools/bespoken/client/BSTMonetize.html#injectSSML-java.lang.String-java.lang.String-):
```
    BSTMonetize monetize = new BSTMonetize("MySkillID");
     SpeechletResponse.newSpeechletResponse(monetize.injectSSML(
          "<speak>Hi! Now a word from our sponsor {ad}! What do you want to do now?</speak>",
          "<speak>Hi!What do you want to do now?</speak>")
     ).asSsmlOutputSpeech(), repromptSpeech, card);
```

The call to injectSSML will replace the {ad} token with an <audio> tag for the advertisement.

If no ad is available to be served, the fallback SSML (the second parameter) will be used.

Notice that we use different wording for the example with an ad - it prefaces it with "Now a word from our sponsor." 
It is not necessary, but is a nice user experience consideration.

## Reference
Detailed API documentation is at [javadoc.io.](https://www.javadoc.io/doc/tools.bespoken/bst4j/)
