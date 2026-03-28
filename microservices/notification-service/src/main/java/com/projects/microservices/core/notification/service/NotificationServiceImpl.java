package com.projects.microservices.core.notification.service;

import static java.util.logging.Level.FINE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.projects.api.core.notification.*;
import com.projects.api.exceptions.BadRequestException;
import com.projects.microservices.core.notification.persistence.NotificationEntity;
import com.projects.microservices.core.notification.persistence.NotificationRepository;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Value("${sendgrid.api-key:}")
    private String sendGridApiKey;

    @Value("${twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token:}")
    private String twilioAuthToken;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    @Override
    public Mono<Notification> sendNotification(Notification notification) {
        if (notification.getType() == null || notification.getType().isEmpty()) {
            return Mono.error(new BadRequestException("Notification type is required"));
        }
        if (notification.getRecipient() == null || notification.getRecipient().isEmpty()) {
            return Mono.error(new BadRequestException("Recipient is required"));
        }

        NotificationEntity entity = notificationMapper.apiToEntity(notification);
        entity.setStatus("PENDING");
        
        return Mono.fromCallable(() -> {
            try {
                if ("EMAIL".equals(notification.getType())) {
                    sendEmailInternal(notification);
                } else if ("SMS".equals(notification.getType())) {
                    sendSMSInternal(notification);
                }
                
                entity.setStatus("SENT");
                entity.setSentAt(java.time.LocalDateTime.now());
            } catch (Exception e) {
                LOG.error("Failed to send notification: {}", e.getMessage());
                entity.setStatus("FAILED");
                entity.setErrorReason(e.getMessage());
            }
            
            return notificationRepository.save(entity)
                    .log(LOG.getName(), FINE)
                    .map(notificationMapper::entityToApi);
        }).flatMapMany(n -> n).next();
    }

    @Override
    public Mono<Void> sendEmail(EmailNotification notification) {
        if (notification.getTo() == null || notification.getTo().isEmpty()) {
            return Mono.error(new BadRequestException("Email recipient is required"));
        }

        if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
            LOG.warn("SendGrid API key not configured, using mock email");
            return Mono.empty();
        }

        LOG.info("Mock email would be sent to: {} with subject: {}", notification.getTo(), notification.getSubject());

        return Mono.empty();
    }

    @Override
    public Mono<Void> sendSMS(SMSNotification notification) {
        if (notification.getTo() == null || notification.getTo().isEmpty()) {
            return Mono.error(new BadRequestException("Phone number is required"));
        }

        if (twilioAccountSid == null || twilioAccountSid.isEmpty() ||
            twilioAuthToken == null || twilioAuthToken.isEmpty()) {
            LOG.warn("Twilio credentials not configured, using mock SMS");
            return Mono.empty();
        }

        LOG.info("Mock SMS would be sent to: {}", notification.getTo());

        return Mono.empty();
    }

    @Override
    public Mono<Notification> getNotification(String notificationId) {
        return notificationRepository.findByNotificationId(notificationId)
                .switchIfEmpty(Mono.error(new BadRequestException("Notification not found: " + notificationId)))
                .map(notificationMapper::entityToApi);
    }

    @Override
    public Flux<Notification> getNotificationsByUserId(String userId) {
        return notificationRepository.findByUserId(userId)
                .map(notificationMapper::entityToApi);
    }

    @Override
    public Mono<Notification> markAsRead(String notificationId) {
        return notificationRepository.findByNotificationId(notificationId)
                .switchIfEmpty(Mono.error(new BadRequestException("Notification not found: " + notificationId)))
                .flatMap(entity -> {
                    entity.setIsRead(true);
                    entity.setReadAt(java.time.LocalDateTime.now());
                    return notificationRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(notificationMapper::entityToApi);
                });
    }

    @Override
    public Mono<Void> deleteNotification(String notificationId) {
        return notificationRepository.findByNotificationId(notificationId)
                .switchIfEmpty(Mono.error(new BadRequestException("Notification not found: " + notificationId)))
                .flatMap(notificationRepository::delete)
                .then(Mono.empty());
    }

    private void sendEmailInternal(Notification notification) {
        // Mock email sending
        LOG.info("Sending email to: {}, subject: {}", notification.getRecipient(), notification.getSubject());
    }

    private void sendSMSInternal(Notification notification) {
        // Mock SMS sending
        LOG.info("Sending SMS to: {}", notification.getRecipient());
    }
}
