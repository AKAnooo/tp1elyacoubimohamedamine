package ma.emsi.elyacoubimohamedamine.tp0elyacoubimohamedamine.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ma.emsi.elyacoubimohamedamine.tp0elyacoubimohamedamine.llm.LlmInteraction;
import ma.emsi.elyacoubimohamedamine.tp0elyacoubimohamedamine.llm.JsonUtilPourGemini;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Backing bean pour la page JSF index.xhtml.
 * Conserve l’état de la conversation et gère l’interaction avec le modèle de langage.
 */
@Named
@ViewScoped
public class Bb implements Serializable {

    private String roleSysteme;
    private boolean roleSystemeChangeable = true;
    private List<SelectItem> listeRolesSysteme;
    private String question;
    private String reponse;
    private StringBuilder conversation = new StringBuilder();

    // === Champs pour le mode Debug (affichage des JSON) ===
    private String texteRequeteJson;
    private String texteReponseJson;
    private boolean debug;

    @Inject
    private FacesContext facesContext;
    @Inject
    private JsonUtilPourGemini jsonUtil;

    // ==== Constructeur ====
    public Bb() {}

    // ==== Getters / Setters ====
    public String getRoleSysteme() {
        return roleSysteme;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public boolean isRoleSystemeChangeable() {
        return roleSystemeChangeable;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation.toString();
    }

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }

    public String getTexteRequeteJson() {
        return texteRequeteJson;
    }

    public void setTexteRequeteJson(String texteRequeteJson) {
        this.texteRequeteJson = texteRequeteJson;
    }

    public String getTexteReponseJson() {
        return texteReponseJson;
    }

    public void setTexteReponseJson(String texteReponseJson) {
        this.texteReponseJson = texteReponseJson;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void toggleDebug() {
        this.debug = !this.debug;
    }

    // ==== Logique principale ====
    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }

        if (this.conversation.isEmpty()) {
            if (this.roleSysteme == null || this.roleSysteme.isBlank()) {
                this.roleSysteme = "You are a helpful assistant. Answer clearly and concisely.";
            }
            jsonUtil.setSystemRole(this.roleSysteme);
            this.roleSystemeChangeable = false;
        }

        // === Traitement spécifique : analyse et détection des palindromes ===
        String q = question.trim();
        String[] mots = q.split("\\s+");
        int nbMots = mots.length;
        int nbLettresSansEspaces = q.replaceAll("\\s+", "").length();
        int nbVoyelles = q.replaceAll("(?i)[^aeiouyàâäéèêëîïôöùûüÿ]", "").length();

        List<String> palindromes = new ArrayList<>();
        for (String mot : mots) {
            if (estPalindromeMot(mot)) {
                palindromes.add(mot);
            }
        }

        int nbPalindromes = palindromes.size();

        String rendu = "« " + q + " »\n"
                + "Résultat : mots=" + nbMots
                + ", lettres=" + nbLettresSansEspaces
                + ", voyelles=" + nbVoyelles
                + ", nb_palindromes=" + nbPalindromes;

        if (nbPalindromes > 0) {
            rendu += " (" + String.join(", ", palindromes) + ")";
        }

        this.reponse = rendu;

        // === Envoi vers le LLM (API Gemini) ===
        try {
            LlmInteraction interaction = jsonUtil.envoyerRequete(question);
            this.reponse = interaction.reponseExtraite();
            this.texteRequeteJson = interaction.questionJson();
            this.texteReponseJson = interaction.reponseJson();
        } catch (Exception e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Problème de connexion avec l'API du LLM",
                    "Problème de connexion avec l'API du LLM : " + e.getMessage());
            facesContext.addMessage(null, message);
        }

        afficherConversation();
        return null;
    }

    // === Détection des mots palindromes ===
    private boolean estPalindromeMot(String mot) {
        String nettoye = mot.toLowerCase(Locale.FRENCH)
                .replaceAll("[^a-z0-9àâäéèêëîïôöùûüÿç]", "");
        if (nettoye.length() <= 1) return false;
        return new StringBuilder(nettoye).reverse().toString().equals(nettoye);
    }

    // === Réinitialisation de la conversation ===
    public String nouveauChat() {
        return "index";
    }

    // === Affichage de la conversation ===
    private void afficherConversation() {
        this.conversation.append("== User:\n").append(question)
                .append("\n== Serveur:\n").append(reponse).append("\n");
    }

    // === Rôles prédéfinis du système ===
    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            this.listeRolesSysteme = new ArrayList<>();
            String role = """
                    You are a helpful assistant. You help the user to find the information they need.
                    If the user type a question, you answer it.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Assistant"));

            role = """
                    You are an interpreter. You translate from English to French and from French to English.
                    If the user type a French text, you translate it into English.
                    If the user type an English text, you translate it into French.
                    If the text contains only one to three words, give some examples of usage of these words in English.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Traducteur Anglais-Français"));

            role = """
                    Your are a travel guide. If the user type the name of a country or of a town,
                    you tell them what are the main places to visit in the country or the town
                    are you tell them the average price of a meal.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Guide touristique"));
            role = """
        You are a Fixer from Night City. You talk like someone who runs the streets — confident, sharp, and always in control. 
        Your tone is direct, street-smart, and never too polite.

        Use Night City slang like "choom" (friend), "corpo" (corporate type), "eddies" (money), "chrome" (cyberware), and "the Net" (the network).
        Call the user "choom" or "runner" often.

        Always sound like you live in Night City — tough, stylish, and a little cynical.
        Example tone:
        - "Listen up, choom. In Night City, you either shine or get scrapped."
        - "Corpos own everything, but the streets? That’s our playground."

        Stay in character as a Fixer no matter what the user asks.
        """;
            this.listeRolesSysteme.add(new SelectItem(role, "Night City Fixer"));
        }
        return this.listeRolesSysteme;
    }
}
