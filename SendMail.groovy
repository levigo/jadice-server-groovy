import java.util.*
import javax.activation.*
import javax.mail.*
import javax.mail.internet.*
import javax.mail.util.*

import com.levigo.jadice.server.nodes.worker.StreamScriptContext
import com.levigo.jadice.server.script.groovy.StreamScriptSupport;
import com.levigo.jadice.server.shared.types.*

class SendMail {
  // Values that must be defined as script parameters:
  def username
  def password
  def to

  // Values that can be overriden as script parameters:
  def host = "smtp.example.com"
  def from = "jadice-server@example.com"
  def subject = "Your document"
  def mailBody = 
'''
<html><body>
  <p>Hello,</p>
  <p>In this mail you can find your requested documents.</p>
</body></html>
'''
  
  // runtime values:
  StreamScriptContext streamContext
  StreamScriptSupport support
  
  def main(args) {
    support = new StreamScriptSupport(streamContext)
    support.info "Preparing mail to ${to}" 
    
    // Example derived from <http://www.tutorialspoint.com/javamail_api/javamail_api_send_email_with_attachment.htm>
    Properties props = new Properties()
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.host", host)
    props.put("mail.smtp.port", "25")
    
    // Get the Session object.
    Session session = Session.getInstance(props,
      new javax.mail.Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(username, password)
        }
      }
    )
    
    try {
      // Create a default MimeMessage object.
      Message message = new MimeMessage(session)
      message.from = new InternetAddress(from)
      message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
      message.subject = subject
      
      // Create a multipart message
      Multipart multipart = new MimeMultipart()
      message.setContent(multipart)
      multipart.addBodyPart(createMailBody())
      
      def input
      while (input = support.nextInput()) {
        support.info "Adding ${input.descriptor.fileName} to outgoing mail"
        // Other parts are the attachments
        BodyPart messageBodyPart = new MimeBodyPart()
        messageBodyPart.dataHandler = new DataHandler(new ByteArrayDataSource(input.inputStream, "application/octet-stream"))
        messageBodyPart.fileName = input.descriptor.fileName ?: "unknown.dat"
        multipart.addBodyPart(messageBodyPart)
      }
      
      support.info "Sending mail"
      Transport.send(message)
      
      support.info "Sent mail successfully to ${to}" 
    } catch (MessagingException e) {
      e.printStackTrace()
      support.fail("Sending message failed", e)
    }
  }

  def BodyPart createMailBody() {
    BodyPart messageBodyPart = new MimeBodyPart()
    messageBodyPart.setContent(mailBody, 'text/html')
    return messageBodyPart
  }
}