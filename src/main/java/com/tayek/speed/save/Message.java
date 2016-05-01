package com.tayek.speed.save;

import static com.tayek.io.IO.l;
import java.util.regex.Pattern;
import com.tayek.Required;
import com.tayek.speed.*;
import com.tayek.utilities.Single;

interface Message {
    // looks like we can toss this soon!
    String from();
    String host();
    Integer service();
    String string();
    interface Factory {
        Message create(String string);
        Message from(String string);
        interface MetaFactory {
            Factory create(Required required,Single<Integer> single);
            class FImpl implements MetaFactory {
                @Override public Factory create(Required required,Single<Integer> single) {
                    return new MessageFactory(required,single);
                }
                private static class MessageFactory implements Message.Factory {
                    MessageFactory(Required required,Single<Integer> single) {
                        this.required=required;
                        this.messages=single;
                        l.warning(this.getClass().getSimpleName()+" constructed with: "+required);
                    }
                    @Override public MessageImpl from(String string) { // move to tablet?
                        String[] parts=string.split(Pattern.quote(""+Message.delimiter));
                        MessageImpl message=new MessageImpl(parts[0],parts[1],Integer.valueOf(parts[2]),parts[3]);
                        return message;
                    }
                    @Override public MessageImpl create(String string) {
                        return new MessageImpl(required.id,required.host,required.service,string);
                    }
                    private final Required required;
                    private final Single<Integer> messages;
                    private class MessageImpl implements Message {
                        private MessageImpl(String from,String host,int service,String string) {
                            this.from=from;
                            this.host=host;
                            this.service=service;
                            this.string=string;
                        }
                        @Override public String from() {
                            return from;
                        }
                        @Override public String host() {
                            return host;
                        }
                        @Override public Integer service() {
                            return service;
                        }
                        @Override public String string() {
                            return string;
                        }
                        @Override public String toString() {
                            return from+delimiter+host+delimiter+service+delimiter+string+delimiter+Writer.line;
                        }
                        private final String from,host,string; // order is different!
                        private final int service;
                    }
                }
            }
        }
    }
    //MetaFactory instance=new MetaFactory.FImpl(); // remove comment if we put this back!
    Character delimiter='|';
}
