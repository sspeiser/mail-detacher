package net.dataops.maildetacher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DetachIT {
    static final String EMAIL = "joe@example.org";
    static final String PASSWORD = "hj3fi4joiajfe";
    static final String OTHEREMAIL = "mary@example.org";

    private GreenMail greenMail;

    @Before
    public void setUp() throws Exception {
        greenMail = new GreenMail(ServerSetupTest.IMAP.dynamicPort());
        greenMail.start();
    }

    @After
    public void tearDown() throws Exception {
        greenMail.stop();
    }

    @Test
    public void testGreenmailTestSetup() throws MessagingException, IOException {
        // Create user, as connect verifies pwd
        GreenMailUser user = greenMail.setUser(EMAIL, EMAIL, PASSWORD);

        MimeMessage message = new MimeMessage((Session) null);
        message.setFrom(OTHEREMAIL);
        message.setRecipients(Message.RecipientType.TO, EMAIL);
        message.setSubject("Message with attachment");
        message.setText("Actual plain text");

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        Multipart multipart = new MimeMultipart();

        // String file = ".gitignore";
        String fileName = "gitignore.txt";
        // DataSource source = new FileDataSource(file);
        messageBodyPart.setText("attachment content");
        // messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(fileName);
        multipart.addBodyPart(messageBodyPart);
        message.setContent(multipart);

        user.deliver(message);

        Properties props = new Properties();
        Session session = Session.getInstance(props);
        URLName urlName = new URLName("imap", greenMail.getImap().getBindTo(), greenMail.getImap().getPort(), "",
                user.getLogin(), user.getPassword());
        Store store = session.getStore(urlName);
        store.connect();

        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_ONLY);
        Message[] messages = folder.getMessages();
        assertNotNull(messages);
        assertEquals(1, messages.length);
        assertEquals(message.getSubject(), messages[0].getSubject());

    }

}