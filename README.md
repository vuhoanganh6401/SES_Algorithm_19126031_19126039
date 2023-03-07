# Schiper-Eggli-Sandoz Algorithm

Exercise 1B for Distributed Algorithms (IN4150), implementing the Schiper-Eggli-Sandoz algorithm for causal ordering
of point-to-point messages.

To build the .jar file, execute from the current directory,

```bash
mvn clean package -Dmaven.test.skip=true 
```

This will skip unit tests, which we implemented using JUnit. To run tests, remove the added flag.
Note, to run tests in IDEs the tests still need to be compiled with the command above and the flag removed.

The build will generate a .jar files in the target/ directory. To execute the generated .jar file,

```bash
java -Djava.security.policy=java.policy -jar target/DA-Schiper-Eggli-Sandoz.jar
```

## Program

The program will create a registry on port 1098 and 3 processes (P1, P2, and P3), which are all bound to the registry. 
Next, processes exchange messages, where first P1 sends a message to P2 with a delay of 2000ms. Then P1 sends a message
to P3 with no delay, followed by P3 sending a message to P1. P2 receives the message from P3 first, buffers it, as it 
is missing the message from P1, which it knows from the buffer sent in the message from P3. Once P2 receives the message
from P1, it gets successfuly delivered and P2 checks its message buffer and delivers the buffered message from P3.

Message delays are implemented by creating a new thread, when a message has a delay. Said thread will then sleep the 
delay time before sending the message.
