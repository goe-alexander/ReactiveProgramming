package reactive_progression.observer;

public class ConcreteObserverA implements Observer<String>{
    @Override
    public void observe(String event) {
        System.out.println("Observer A observerd: " + event);
    }
}
