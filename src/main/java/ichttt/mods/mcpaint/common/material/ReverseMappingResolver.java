package ichttt.mods.mcpaint.common.material;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ichttt.mods.mcpaint.MCPaint;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fml.loading.FMLLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class ReverseMappingResolver {
    private static final String TSRG_URL = "https://raw.githubusercontent.com/MinecraftForge/MCPConfig/master/versions/release/%s/joined.tsrg";

    public static Map<String, String> loadMappingFor(Class<?> clazz, Function<String, String> officialRemapper) throws IOException, InterruptedException, ReflectiveOperationException {

        HttpClient client = HttpClient.newHttpClient();
        String url = locateVersionInformation(client);

        Stream<String> clientMappings = downloadClientMappings(client, url);
        Iterable<String> clientMappingIterator = clientMappings::iterator;
        String className = clazz.getName();
        String obfedName = null;
        ImmutableMap.Builder<String, String> obfToOfficialBuilder = ImmutableMap.builder();
        for (String s : clientMappingIterator) {
            if (s.startsWith("#")) continue;
            if (obfedName != null) {
                if (!s.startsWith(" ")) {
                    break;
                }
                String trimmed = s.trim();
                if (trimmed.startsWith(className + " ")) {
                    String[] split = trimmed.split(" ");
                    String fieldName = split[1].trim();
                    try {
                        Field declaredField = Material.class.getDeclaredField(fieldName);
                        int modifiers = declaredField.getModifiers();
                        if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers) || declaredField.getType() != Material.class)
                            throw new RuntimeException("Invalid modifiers or return type!");
                        obfToOfficialBuilder.put(split[split.length - 1], fieldName);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to find correct field " + fieldName + " from line " + s, e);
                    }
                }
            }
            if (s.startsWith(className + " ")) {
                obfedName = s.substring(className.length() + " -> ".length(), s.length() - 1).trim();
                MCPaint.LOGGER.info("Found obfed name " + obfedName);
            }
        }
        ImmutableMap<String, String> obfToOfficial = obfToOfficialBuilder.build();

        Stream<String> joinedTsrg = downloadJoinedTsrg(client);
        Iterable<String> joinedTsrgIterator = joinedTsrg::iterator;
        boolean checkedFormat = false;
        boolean foundClass = false;
        ImmutableMap.Builder<String, String> srgToOfficialBuilder = ImmutableMap.builder();
        for (String s : joinedTsrgIterator) {
            if (!checkedFormat) {
                if (!s.startsWith("tsrg2")) throw new RuntimeException("Unknown format " + s);
                checkedFormat = true;
                continue;
            }
            if (foundClass) {
                if (!s.startsWith("\t")) break;
                String trimmed = s.substring(1);
                if (trimmed.startsWith("\t")) continue;
                String[] split = trimmed.split(" ");
                if (split.length != 3) continue;
                String officialName = obfToOfficial.get(split[0]);
                if (officialName != null) {
                    srgToOfficialBuilder.put(split[1], officialRemapper.apply(officialName));
                }
            }
            if (s.startsWith(obfedName + " ")) {
                foundClass = true;
            }
        }
        ImmutableMap<String, String> srgToOfficial = srgToOfficialBuilder.build();
        if (srgToOfficial.size() != obfToOfficial.size())
            throw new RuntimeException("MISMATCH: " + srgToOfficial.size() + " entries in srgToOfficial, " + obfToOfficial.size() + " entries in obfToOfficial!");
        return srgToOfficial;
    }

    private static Stream<String> downloadJoinedTsrg(HttpClient client) throws IOException, InterruptedException {
        String downloadUrl = TSRG_URL.formatted(FMLLoader.versionInfo().mcVersion());
        MCPaint.LOGGER.info("Downloading tsrg file from " + downloadUrl);
        HttpResponse<Stream<String>> response = client.send(HttpRequest.newBuilder().uri(URI.create(downloadUrl)).build(), HttpResponse.BodyHandlers.ofLines());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch tsrg: Got response " + response);
        }
        return response.body();
    }

    private static Stream<String> downloadClientMappings(HttpClient client, String versionInfoUrl) throws IOException, InterruptedException {
        HttpResponse<InputStream> response = client.send(HttpRequest.newBuilder().uri(URI.create(versionInfoUrl)).build(), HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch version information: Got response " + response);
        }
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(response.body()));
        JsonObject downloads = jsonElement.getAsJsonObject().getAsJsonObject("downloads");
        JsonObject clientMappings = downloads.getAsJsonObject("client_mappings");
        String url = clientMappings.get("url").getAsString();

        MCPaint.LOGGER.info("Found client mappings {}", url);

        HttpResponse<Stream<String>> actualMappings = client.send(HttpRequest.newBuilder().uri(URI.create(url)).build(), HttpResponse.BodyHandlers.ofLines());
        if (actualMappings.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch mappings: Got response " + actualMappings);
        }
        return actualMappings.body();
    }

    private static String locateVersionInformation(HttpClient client) throws IOException, InterruptedException {
        HttpResponse<InputStream> response = client.send(HttpRequest.newBuilder().uri(URI.create("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json")).build(), HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch version manifest: Got response " + response);
        }

        String mcVersion = FMLLoader.versionInfo().mcVersion();

        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(response.body()));
        JsonArray versions = jsonElement.getAsJsonObject().getAsJsonArray("versions");
        for (JsonElement version : versions) {
            if (version.getAsJsonObject().get("id").getAsString().equals(mcVersion)) {
                String url = version.getAsJsonObject().get("url").getAsString();
                MCPaint.LOGGER.info("Found version manifest {}", url);
                return url;
            }
        }
        throw new RuntimeException("Failed to find current version " + mcVersion);
    }
}
