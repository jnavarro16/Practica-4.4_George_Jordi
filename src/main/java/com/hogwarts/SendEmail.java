package com.hogwarts;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;


public class SendEmail {

/*Ejecutamos el programa añadiendo en run-configurations las variables de entorno que seria:
    SMTP_USER=(correo)
    SMTP_PASS=(contraseña)
*/


    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    public void mandarCorreo(String destinatario, String nombreCliente) {
        String correoEnvia = System.getenv("SMTP_USER");
        String claveCorreo = System.getenv("SMTP_PASS");

        //validacion
        if (correoEnvia == null || claveCorreo == null) {
            System.err.println("Faltan variables de entorno SMTP_USER y/o SMTP_PASS");
            return;
        }

        //Configuración para enviar correo
        Properties properties = new Properties();
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        properties.put("mail.smtp.ssl.trust", SMTP_HOST);

        //Autenticacion
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(correoEnvia, claveCorreo);
            }
        });

        try {
            MimeMessage mimeMessage = new MimeMessage(session);

            //From / To
            mimeMessage.setFrom(new InternetAddress(correoEnvia));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario));

            //Asunto
            mimeMessage.setSubject("Bienvenido/a a nuestra app, " + nombreCliente + "!!", StandardCharsets.UTF_8.name());

            //Parte HTML
            String html = """
                    <html>
                      <body style="font-family: Arial, sans-serif;">
                        <h2>¡Hola %s! 👋</h2>
                        <p>Gracias por registrarte en nuestra aplicación.</p>
                        <p>Tu cuenta ya está activa. ¡Disfrutala!</p>
                        <hr/>
                        <p style="color: #666; font-size: 12px;">
                          Este mensaje es automático. No respondas a este correo.
                        </p>
                      </body>
                    </html>
                    """.formatted(escapeHtml(nombreCliente));

            MimeBodyPart parteHTML = new MimeBodyPart();
            parteHTML.setContent(html, "text/html; charset=utf-8");

            //Imagen adjunta
            MimeBodyPart imagenAdjunta = new MimeBodyPart();

            byte[] imagenBytes = leerRecursoComoBytes("/bienvenida.png");
            if(imagenBytes == null){
                System.err.println("No se ha encontrado el recurso /bienvenida.png en src/main/resources.");
                return;
            }

            ByteArrayDataSource dataSource = new ByteArrayDataSource(imagenBytes, "image/png");
            imagenAdjunta.setDataHandler(new DataHandler(dataSource));
            imagenAdjunta.setFileName("bienvenida.png");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(parteHTML);
            multipart.addBodyPart(imagenAdjunta);

            mimeMessage.setContent(multipart);

            //transportar
            Transport.send(mimeMessage);
            System.out.println("Correo de bienvenida enviado a " +destinatario+ " desde " +correoEnvia);

        } catch (Exception ex) {
            System.err.println("Error enviando el correo: " + ex.getMessage());
        }
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static byte[] leerRecursoComoBytes(String rutaRecurso) {
        try (InputStream is = SendEmail.class.getResourceAsStream(rutaRecurso)) {
            if (is == null) return null;
            return is.readAllBytes();
        } catch (Exception e) {
            System.err.println("Error leyendo recurso: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        String destinatario = "mr.georgemanea@gmail.com";
        String nombre = "Jordi Navarro";

        new SendEmail().mandarCorreo(destinatario, nombre);;
    }
}