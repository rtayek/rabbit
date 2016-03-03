package com.tayek.tablet;
import com.tayek.tablet.io.Client.History;
public interface Sender {
    boolean send(Message message,History history);
    // maybe history belongs here?
}
