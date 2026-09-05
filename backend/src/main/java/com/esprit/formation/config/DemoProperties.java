package com.esprit.formation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.demo")
public class DemoProperties {

    private String adminEmail = "admin@formalearn.tn";
    private String adminPassword = "";
    private String learnerEmail = "apprenant@formalearn.tn";
    private String learnerPassword = "";
    private String learner2Email = "apprenant2@formalearn.tn";
    private String learner3Email = "apprenant3@formalearn.tn";

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getLearnerEmail() {
        return learnerEmail;
    }

    public void setLearnerEmail(String learnerEmail) {
        this.learnerEmail = learnerEmail;
    }

    public String getLearnerPassword() {
        return learnerPassword;
    }

    public void setLearnerPassword(String learnerPassword) {
        this.learnerPassword = learnerPassword;
    }

    public String getLearner2Email() {
        return learner2Email;
    }

    public void setLearner2Email(String learner2Email) {
        this.learner2Email = learner2Email;
    }

    public String getLearner3Email() {
        return learner3Email;
    }

    public void setLearner3Email(String learner3Email) {
        this.learner3Email = learner3Email;
    }

    public void requirePasswords() {
        if (adminPassword == null || adminPassword.isBlank()
                || learnerPassword == null || learnerPassword.isBlank()) {
            throw new IllegalStateException(
                    "DEMO_ADMIN_PASSWORD and DEMO_LEARNER_PASSWORD must be set (local .env, never commit).");
        }
    }
}
