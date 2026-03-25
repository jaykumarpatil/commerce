package se.magnus.api.core.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotification {
    private String to;
    private String from;
    private String subject;
    private String body;
    private java.util.List<String> cc;
    private java.util.List<String> bcc;
}
