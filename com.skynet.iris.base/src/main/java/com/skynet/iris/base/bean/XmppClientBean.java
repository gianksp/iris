/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skynet.iris.base.bean;

import com.engine.interpretation.Interpreter;
import com.engine.interpretation.ObjectHandler;
import com.skynet.iris.common.service.XmppClientBeanRemote;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

/**
 *
 * @author gianksp
 */
@Stateless
public class XmppClientBean implements XmppClientBeanRemote {

    private Logger LOG = Logger.getLogger(XmppClientBean.class.getName());
    private Interpreter rs;
    
    public XmppClientBean() {
    }

    public XmppClientBean(final String client, String host, String bot, String password) {
        try {

            rs = new Interpreter();
            rs.setLogLevel(Level.OFF);

            // Create a handler for Perl objects.
            rs.setHandler(ObjectHandler.Handler.JAVASCRIPT);

            // Load and sort replies
            System.out.println(":: Loading replies");
            rs.loadDefaultDirectory();
            rs.sortReplies();

            XMPPConnection connection = new XMPPConnection(host);
            connection.connect();
            connection.login(bot, password); // TODO: change user and pass

            // register listeners
            ChatManager chatmanager = connection.getChatManager();
            connection.getChatManager().addChatListener(new ChatManagerListener() {
                public void chatCreated(final Chat chat, final boolean createdLocally) {
                    chat.addMessageListener(new MessageListener() {
                        public void processMessage(Chat chat, Message message) {
                            try {
                                System.out.println("Received message: "
                                        + (message != null ? message.getBody() : "NULL"));
                                chat.sendMessage(rs.reply(client, message.getBody()));
                            } catch (XMPPException ex) {
                                Logger.getLogger(XmppClientBean.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                }
            });

            // idle for 20 seconds
            /*final long start = System.nanoTime();
             while ((System.nanoTime() - start) / 1000000 < 20000) // do for 20 seconds
             {
             Thread.sleep(500);
             }
             connection.disconnect();*/
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(XmppClientBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMPPException ex) {
            Logger.getLogger(XmppClientBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
