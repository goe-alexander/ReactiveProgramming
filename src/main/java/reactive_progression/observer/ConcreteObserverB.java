package reactive_progression.observer;

public class ConcreteObserverB implements Observer<String>{
    @Override
    public void observe(String event) {
        System.out.println("Observer B observed :" + event);
    }
}
