package observer;

public class Main {
    public static void main(String[] args){
        Subject<String> subject = new ConcreteSubject();
        Observer<String> observerA = new ConcreteObserverA();
        Observer<String> observerB = new ConcreteObserverB();

        subject.notifyObservers("No listeners");

        subject.registerObserver(observerA);
        subject.notifyObservers("Message for A");
        subject.registerObserver(observerB);
        subject.notifyObservers("Message for A & B");
        subject.unregisterObserver(observerA);
        subject.notifyObservers("Message for B");
        subject.unregisterObserver(observerB);
        subject.notifyObservers("No listeners");
    }
}
