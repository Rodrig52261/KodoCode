package com.kodocode.api.email;

import com.kodocode.api.config.ApplicationProperties;
import com.kodocode.api.lead.ContactLead;
import java.util.LinkedHashMap;
import java.util.Map;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
@ConditionalOnProperty(prefix = "kodo.email", name = "enabled", havingValue = "true")
public class EmailJsEmailService implements EmailService {

    private final RestClient restClient;
    private final ApplicationProperties.Email properties;
    private final ApplicationProperties.EmailJs emailJs;
    private final EmailJsRateLimiter rateLimiter;

    @Autowired
    public EmailJsEmailService(ApplicationProperties applicationProperties) {
        this(timeoutRestClient(), applicationProperties);
    }

    private static RestClient timeoutRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder().requestFactory(requestFactory).build();
    }

    EmailJsEmailService(RestClient restClient, ApplicationProperties applicationProperties) {
        this.properties = applicationProperties.email();
        this.emailJs = properties == null ? null : properties.emailJs();
        validateConfiguration();
        this.restClient = restClient;
        this.rateLimiter = new EmailJsRateLimiter(emailJs.minimumRequestInterval());
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void notifyCompany(ContactLead lead) {
        Map<String, Object> parameters = commonParameters(lead);
        parameters.put("to_email", properties.notificationTo());
        parameters.put("reply_to", lead.getEmail());
        parameters.put("message", notificationMessage(lead));
        parameters.put("notification_message", notificationMessage(lead));
        send(emailJs.notificationTemplateId(), parameters);
    }

    @Override
    public void confirmToCustomer(ContactLead lead) {
        Map<String, Object> parameters = commonParameters(lead);
        parameters.put("to_email", lead.getEmail());
        parameters.put("reply_to", properties.notificationTo());
        send(emailJs.confirmationTemplateId(), parameters);
    }

    private void send(String templateId, Map<String, Object> parameters) {
        rateLimiter.acquire();
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("service_id", emailJs.serviceId());
        request.put("template_id", templateId);
        request.put("user_id", emailJs.publicKey());
        request.put("template_params", parameters);
        if (!isBlank(emailJs.privateKey())) request.put("accessToken", emailJs.privateKey());
        try {
            restClient.post()
                    .uri(emailJs.endpoint())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw new EmailJsDeliveryException(
                    "EmailJS recusou o envio com HTTP " + exception.getStatusCode().value() + ".", exception);
        } catch (RuntimeException exception) {
            throw new EmailJsDeliveryException("Nao foi possivel acessar o EmailJS.", exception);
        }
    }

    private Map<String, Object> commonParameters(ContactLead lead) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("lead_id", lead.getId() == null ? "" : lead.getId().toString());
        parameters.put("name", lead.getName());
        parameters.put("company", nullSafe(lead.getCompany()));
        parameters.put("email", lead.getEmail());
        parameters.put("phone", lead.getPhone());
        parameters.put("service_interest", serviceLabel(lead));
        parameters.put("service_interest_code", lead.getServiceInterest().name());
        parameters.put("budget_range", budgetLabel(lead));
        parameters.put("budget_range_code", lead.getBudgetRange().name());
        parameters.put("message", lead.getMessage());
        return parameters;
    }

    private String notificationMessage(ContactLead lead) {
        return "Empresa: " + nullSafe(lead.getCompany())
                + "\nE-mail: " + lead.getEmail()
                + "\nTelefone: " + lead.getPhone()
                + "\nServico de interesse: " + serviceLabel(lead)
                + "\nFaixa de orcamento: " + budgetLabel(lead)
                + "\nProtocolo: " + (lead.getId() == null ? "Nao informado" : lead.getId())
                + "\n\nMensagem:\n" + lead.getMessage();
    }

    private String serviceLabel(ContactLead lead) {
        return switch (lead.getServiceInterest()) {
            case LANDING_PAGE -> "Landing page";
            case INSTITUTIONAL_SITE -> "Site institucional";
            case CRM -> "CRM";
            case WHATSAPP_CHATBOT -> "Chatbot para WhatsApp";
            case CUSTOM_SYSTEM -> "Sistema personalizado";
            case UNDECIDED -> "Ainda nao sabe qual solucao precisa";
        };
    }

    private String budgetLabel(ContactLead lead) {
        return switch (lead.getBudgetRange()) {
            case UP_TO_2000 -> "Ate R$ 2.000";
            case FROM_2000_TO_5000 -> "De R$ 2.000 a R$ 5.000";
            case FROM_5000_TO_10000 -> "De R$ 5.000 a R$ 10.000";
            case ABOVE_10000 -> "Acima de R$ 10.000";
            case DISCUSS_FIRST -> "Conversar antes de definir";
        };
    }

    private void validateConfiguration() {
        if (properties == null || emailJs == null
                || isBlank(properties.notificationTo())
                || isBlank(emailJs.endpoint())
                || isBlank(emailJs.serviceId())
                || isBlank(emailJs.notificationTemplateId())
                || isBlank(emailJs.confirmationTemplateId())
                || isBlank(emailJs.publicKey())) {
            throw new IllegalStateException(
                    "Configure destinatario, endpoint, service ID, dois template IDs e public key do EmailJS.");
        }
        try {
            URI endpoint = URI.create(emailJs.endpoint());
            if (!"https".equalsIgnoreCase(endpoint.getScheme()) || endpoint.getHost() == null
                    || endpoint.getUserInfo() != null) {
                throw new IllegalStateException("O endpoint do EmailJS deve ser uma URL HTTPS valida.");
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("O endpoint do EmailJS deve ser uma URL HTTPS valida.", exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String nullSafe(String value) {
        return isBlank(value) ? "Nao informada" : value;
    }
}
