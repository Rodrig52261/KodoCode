package com.kodocode.api.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.kodocode.api.config.ApplicationProperties;
import com.kodocode.api.lead.BudgetRange;
import com.kodocode.api.lead.ContactLead;
import com.kodocode.api.lead.ServiceInterest;
import com.kodocode.api.support.TestProperties;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class EmailJsEmailServiceTest {

    @Test
    void sendsCompanyNotificationWithExpectedTemplateParameters() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        EmailJsEmailService service = new EmailJsEmailService(builder.build(), properties("private-key"));

        server.expect(requestTo("https://api.emailjs.com/api/v1.0/email/send"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.service_id").value("service_kodocode"))
                .andExpect(jsonPath("$.template_id").value("template_notification"))
                .andExpect(jsonPath("$.user_id").value("public-key"))
                .andExpect(jsonPath("$.accessToken").value("private-key"))
                .andExpect(jsonPath("$.template_params.to_email").value("company@example.com"))
                .andExpect(jsonPath("$.template_params.reply_to").value("ana@example.com"))
                .andExpect(jsonPath("$.template_params.name").value("Ana"))
                .andExpect(jsonPath("$.template_params.service_interest").value("CRM"))
                .andExpect(jsonPath("$.template_params.budget_range").value("Conversar antes de definir"))
                .andExpect(jsonPath("$.template_params.message").value(containsString("Empresa: Empresa")))
                .andExpect(jsonPath("$.template_params.message").value(containsString("Telefone: (11) 99999-9999")))
                .andExpect(jsonPath("$.template_params.message").value(containsString("Mensagem:\nPreciso organizar")))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

        service.notifyCompany(lead());

        server.verify();
    }

    @Test
    void sendsCustomerConfirmationWithoutOptionalPrivateKey() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        EmailJsEmailService service = new EmailJsEmailService(builder.build(), properties(""));

        server.expect(requestTo("https://api.emailjs.com/api/v1.0/email/send"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.template_id").value("template_confirmation"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.template_params.to_email").value("ana@example.com"))
                .andExpect(jsonPath("$.template_params.reply_to").value("company@example.com"))
                .andExpect(jsonPath("$.template_params.message").value("Preciso organizar o processo comercial da empresa."))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

        service.confirmToCustomer(lead());

        server.verify();
    }

    @Test
    void reportsProviderHttpFailureWithoutExposingItToTheController() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        EmailJsEmailService service = new EmailJsEmailService(builder.build(), properties(""));

        server.expect(requestTo("https://api.emailjs.com/api/v1.0/email/send"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).body("Invalid template"));

        assertThatThrownBy(() -> service.notifyCompany(lead()))
                .isInstanceOf(EmailJsDeliveryException.class)
                .hasMessageContaining("HTTP 400");
        server.verify();
    }

    @Test
    void rejectsInsecureProviderEndpoint() {
        ApplicationProperties defaults = TestProperties.create();
        ApplicationProperties insecure = new ApplicationProperties(
                defaults.jwt(), defaults.cookie(), defaults.cors(), defaults.security(), defaults.bootstrapAdmin(),
                defaults.contact(),
                new ApplicationProperties.Email(true, "company@example.com",
                        new ApplicationProperties.EmailJs(
                                "http://api.emailjs.com/api/v1.0/email/send",
                                "service_kodocode", "template_notification", "template_confirmation",
                                "public-key", "", Duration.ZERO)));

        assertThatThrownBy(() -> new EmailJsEmailService(RestClient.create(), insecure))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("HTTPS");
    }

    private ApplicationProperties properties(String privateKey) {
        ApplicationProperties defaults = TestProperties.create();
        return new ApplicationProperties(
                defaults.jwt(), defaults.cookie(), defaults.cors(), defaults.security(), defaults.bootstrapAdmin(),
                defaults.contact(),
                new ApplicationProperties.Email(true, "company@example.com",
                        new ApplicationProperties.EmailJs(
                                "https://api.emailjs.com/api/v1.0/email/send",
                                "service_kodocode",
                                "template_notification",
                                "template_confirmation",
                                "public-key",
                                privateKey,
                                Duration.ZERO)));
    }

    private ContactLead lead() {
        ContactLead lead = new ContactLead();
        lead.setId(UUID.fromString("30000000-0000-4000-8000-000000000099"));
        lead.setName("Ana");
        lead.setCompany("Empresa");
        lead.setEmail("ana@example.com");
        lead.setPhone("(11) 99999-9999");
        lead.setServiceInterest(ServiceInterest.CRM);
        lead.setBudgetRange(BudgetRange.DISCUSS_FIRST);
        lead.setMessage("Preciso organizar o processo comercial da empresa.");
        return lead;
    }
}
