/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package vault.secrets;

import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.json.JsonObject;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.SslConfig;
import io.github.jopenlibs.vault.json.JsonValue;
import io.github.jopenlibs.vault.response.AuthResponse;
import io.github.jopenlibs.vault.response.LogicalResponse;
import io.github.jopenlibs.vault.response.UnwrapResponse;


import java.util.Map;

public class App {

    public static String ROLE_ID = "role_id";
    public static String SECRET_ID = "secret_id";

    public static String wrappedToken = "put_wrapped_token_here";

    public static String vaultUrl = "http://127.0.0.1:8200";

    public static void main(String[] args) throws VaultException {
        Vault vault = login(vaultUrl, wrappedToken);
        String password = read(vault, "/sca-dev/sca", "snr-conf");
        System.out.println(password);
    }

    /**
     * Get a Vault client.
     *
     * @param vaultUrl vault url
     * @param token vault token
     * @return vault client
     * @throws VaultException if error happens
     */
    private static Vault getVault(String vaultUrl, String token) throws VaultException {
        final VaultConfig config =
                new VaultConfig()
                        .address(vaultUrl)
                        .token(token)
                        .sslConfig(new SslConfig().build())
                        .build();
        return Vault.create(config);
    }

    /**
     * Log in to Vault using the wrapped token.
     * @param vaultUrl Vault URL
     * @param wrappedToken wrapped token
     * @return initialized vault client
     * @throws VaultException if error happens
     */
    public static Vault login(String vaultUrl, String wrappedToken) throws VaultException {

        Vault vault = getVault(vaultUrl, wrappedToken);
        UnwrapResponse unwrapped = vault.sys().wrapping().unwrap();
        JsonObject json = unwrapped.getData();

        JsonValue jsonRole = json.get(ROLE_ID);
        if (jsonRole == null) {
            throw new VaultException("No role found in wrapped token");
        }
        String role = jsonRole.asString();

        JsonValue jsonSecret = json.get(SECRET_ID);
        if (jsonSecret == null) {
            throw new VaultException("No secret found in wrapped token");
        }
        String secret = jsonSecret.asString();

        AuthResponse authResponse = vault.auth().loginByAppRole(role, secret);
        String clientToken = authResponse.getAuthClientToken();

        return getVault(vaultUrl, clientToken);
    }

    /**
     * Read a value by key from Vault.
     *
     * @param vault vault
     * @param path  path to the Vault secret
     * @param key   key in the secret
     * @return value corresponding to the key
     * @throws VaultException if error happens
     */
    public static String read(Vault vault, String path, String key) throws VaultException {
        LogicalResponse response = vault.logical().read(path);
        int status = response.getRestResponse().getStatus();
        if (status != 200) {
            byte[] body = response.getRestResponse().getBody();
            String msg = "Vault read by path '%s' failed with status code '%d' '%s'".formatted(path, status, new String(body));
            throw new VaultException(msg);
        }
        Map<String, String> data = response.getData();
        if (data == null) {
            String msg = "Vault read by path '%s' failed with null data".formatted(path);
            throw new VaultException(msg);
        }
        return data.get(key);
    }
}
