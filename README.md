Camel Servlet and Apache Tomcat example
=======================================

An example which proxies request to [Camel Rest Servlet Example](https://github.com/smparekh/camel-example-servlet-rest-tomcat)

It uses a simple header 'Auth-Key' = 'RedHat' to authorize the request.

You will need to package this example first:
  mvn package

To run the example deploy it in Apache Tomcat by copying the .war to the
deploy folder of Apache Tomcat.

And then hit this url from a webbrowser which has further
instructions (use correct version number)
  http://localhost:8080/proxyservice/

If you hit any problems please let me know!
---------------------------------------