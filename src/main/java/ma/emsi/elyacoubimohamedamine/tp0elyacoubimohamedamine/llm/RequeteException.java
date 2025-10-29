package ma.emsi.elyacoubimohamedamine.tp0elyacoubimohamedamine.llm;

public class RequeteException extends Exception {

    private int status;
    private String jsonRequete;
    private String requeteJson;

    public RequeteException(String message) {
    super(message);
}
    public RequeteException(int status) {
        this.status = status;
    }

public RequeteException(String message, String jsonRequete) {
    super(message);
    this.jsonRequete = jsonRequete;
}
    public int getStatus() {
        return status;
    }

public String getJsonRequete() {
    return jsonRequete;
}
}
