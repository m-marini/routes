# Reactive

```mermaid
sequenceDiagram

activate Client
Client->>+Publisher: new
Publisher-->-Client: return

Client->>+Subscriber: new
Subscriber-->-Client: return

Client->>+Publisher: subscribe
Publisher->>+Subscription: new
Subscription-->-Publisher: return
Publisher-x+Subscriber: onSubscription
Publisher-->-Client: return
deactivate Client

Subscriber->>+Subscription: request(n)
Subscription-x+Publisher: request(n)
Subscription-->-Subscriber: request(n)
deactivate Subscriber

Publisher->>+Subscriber: onNext(t)
Subscriber-->-Publisher: return
Publisher->>+Subscriber: onNext(t)
Subscriber-->-Publisher: return
Publisher->>+Subscriber: onComplete(t)
Subscriber-->-Publisher: return
Publisher->>+Subscription: cancel(t)
Subscription-->-Publisher: return

deactivate Publisher

```


Publisher-->Client: 

Subscriber->>Subscription: request(n)

activate Subscription
Subscription->>Publisher: request(n)
deactivate Subscription
activate Publisher
Publisher->>Subscriber: onNext(t)
Publisher->>Subscriber: onNext(t)
Publisher->>Subscriber: onComplete(t)
Publisher->>Subscription: cancel(t)

deactivate Publisher

