/*
Copyright (c) 2015, levigo holding gmbh.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, 
   this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, 
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 * Neither the name of levigo holding gmbh nor the names of its contributors
   may be used to endorse or promote products derived from this software 
   without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

*/

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