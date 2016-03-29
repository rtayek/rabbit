package com.tayek.tablet;
import java.util.Observable;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.io.*;
import static com.tayek.io.IO.*;
import com.tayek.tablet.io.Audio.Sound;
public class AudioObserver implements View {
    // maybe have audio class implement this?
    public AudioObserver(Model model) {
        this.model=model;
    }
    @Override public void update(Observable model,Object hint) {
        l.fine("hint: "+hint);
        if(model instanceof Model) if(model.equals(this.model)) if(hint instanceof Sound) Audio.audio.play((Sound)hint);
        else l.finer("not our hint: "+hint);
        else l.warning("not our model!");
        else l.warning("not a model!");
    }
    private final Model model;
}
