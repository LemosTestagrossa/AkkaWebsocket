# Idea
To receive via websocket a CSV document, split it, and distribute the processing of each line between 10 workers per node.

To send via websocket an ACK message each time a worker finish processing the line it was given to.

# Implementation
We used Play because it is nicer than Akka HTTP.

We used Akka Cluster because we needed Distributed PubSub.

We used Distributed PubSub because we needed to coordinate the workers with ease.

# Demo
Imagine you have two companies, CocaCola and Pepsico.

Both want to use your SaaS platform.

They are going to be sending CSV files via websocket, and expect for each line to take between 1 second and an hour to compute.

Imagine that the lines of the CSV files trigger some heavy computation.

Because of this, each company wants so know in real-time when each lines has been processed. 
But they do not want to know about the other company real-time events.
So we should segregate the streams both company-wise and document-wise.

Finally the documents may have a revisionId attribute which would allow you to reject them if you have already processed them before.

Best of luck. 
  


![](https://i.imgur.com/KKNYdkq.png)

# How to run Demo 1:

In different 4 consoles, run:

console [1]
```bash 
bash demo/1/seed.sh
```
console [2]
```bash 
bash demo/1/node1.sh
```
console [3]
```bash 
bash demo/1/jsSubscriber.sh
```
console [4]
```bash 
bash demo/1/jsProducer.sh
```

![](https://i.imgur.com/c0vVHAw.png)
# How to run Demo 2:

In different 4 consoles, run:

First run these, you should see no output yet.

console [1]
```bash 
bash demo/1/seed.sh
```
console [2]
```bash 
bash demo/1/node1.sh
```
console [3]
```bash 
bash demo/2/jsProducerOfDocument1.sh
```

Now try this:

console [4]
```bash 
bash demo/2/jsSubscriberOfDocument2.sh
```

It should not show results, because it is subscribed to another company, another document.

Segregation of streams via url. Is working.

Now let's create a stream for the url consumed at console [4]

console [5]
```bash 
bash demo/1/jsProducerOfDocument2.sh
```

Now you should see results at console [4]

