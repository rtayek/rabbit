package com.tayek.tablet;
public interface Receiver {
    void receive(Message message);
    public static class DummyReceiver implements Receiver {
        @Override public void receive(Message message) {
            this.message=message;
        }
        public Message message;
        // maybe history belongs here?
    }
}
