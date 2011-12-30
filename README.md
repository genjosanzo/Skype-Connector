
WELCOME
=======
Congratulations you have just created a new Mule Cloud connector!

Now you need to make sure that you update you pom.xml to use version
${muleVersion} of Mule. This will ensure you are compiling against the correct
version of Mule and you will avoid issues arising from not being about to find
configuration schemas for this module.

This wizard created a number of new classes and resources useful for Mule
cloud connectors.  Each of the created files contains documentation and TODO
items where necessary.  Here is an overview of what was created.

./pom.xml:
A maven project descriptor that describes how to build this module. It also
contains  additional information about how to share the connector on MuleForge.

./assembly.xml:
A maven assembly descriptor that defines how this module will be packaged
when you make a release.

./LICENSE.txt:
The open source license text for this project.

BUILDING
=======
To build the Skype-Connector you first need to purchase a skypekit license. You can obtain one at the following address:
https://developer.skype.com/skypekit

Within the sdk you will find all the documentation you might need. Here we will only resume the steps necessary to build the connector:

1)Install the java skype API in your maven repository

cd ${skype-kit-sdk-path}/interfaces/skype/java/api
ant
mvn install:install-file -Dfile=sid-java-wrapper.jar -DgroupId=com.skype -DartifactId=sid-java-wrapper -Dversion=4.0.2.138 -Dpackaging=jar

2)Install the java skype client in your maven repository

cd ${skype-kit-sdk-path}/interfaces/skype/java/client
ant
mvn install:install-file -Dfile=skypekitclient.jar -DgroupId=com.skype -DartifactId=skypekitclient -Dversion=4.0.2.138 -Dpackaging=jar

3)Download the skypekit runtime for your platform and copy it within the src/main/resources/ folder.

4)Request your skype private/public key and copy it within the src/main/resources/ folder.

TESTING
=======

This  project also contains test classes that can be run as part of a test
suite.

ADDITIONAL RESOURCES
====================
Everything you need to know about getting started with Mule can be found here:
http://www.mulesoft.org/documentation/display/MULE3INTRO/Home

There further useful information about extending Mule here:
http://www.mulesoft.org/documentation/display/MULE3USER/Introduction+to+Extending+Mule

For information about working with Mule inside and IDE with maven can be
found here:
http://www.mulesoft.org/documentation/display/MULE3INTRO/Setting+Up+Eclipse

Remember if you get stuck you can try getting help on the Mule user list:
http://www.mulesoft.org/email-lists

Also, MuleSoft, the company behind Mule, offers 24x7 support options:
http://www.mulesoft.com/enterprise-subscriptions-and-support

Enjoy your Mule ride!

The Mule Team
