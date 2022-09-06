package ichttt.mods.mcpaint.common.material;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MaterialDefinitions {
    private final String version;
    private final Map<String, String> srg2Mcp;

    public MaterialDefinitions(String version, Map<String, String> srg2Mcp) {
        this.version = version;
        this.srg2Mcp = srg2Mcp;
    }

    public MaterialDefinitions(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            this.version = reader.readLine();
            this.srg2Mcp = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] split = line.split(",", 2);
                this.srg2Mcp.put(split[0], split[1]);
            }
        }
    }

    public Map<String, String> getSrg2Mcp() {
        return srg2Mcp;
    }

    public String getVersion() {
        return version;
    }
}
