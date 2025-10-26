package ma.emsi.elyacoubimohamedamine.tp0elyacoubimohamedamine.llm;

public class RequeteException extends Exception {

private String jsonRequete;
public RequeteException(String message) {
    super(message);
}
public RequeteException(String message, String jsonRequete) {
    super(message);
    this.jsonRequete = jsonRequete;
}

public String getJsonRequete() {
    return jsonRequete;
}
}
