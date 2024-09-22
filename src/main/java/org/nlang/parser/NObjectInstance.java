package org.nlang.parser;

import org.nlang.err.Err;
import org.nlang.lexer.Token;
import java.util.HashMap;
import java.util.Map;


public class NObjectInstance {

    private final Map<Object, Object> fields;


    public NObjectInstance() {
        this.fields = new HashMap<>();
    }

    public void defineField(Object key, Object value) {
        fields.put(String.valueOf(key), value);
    }

    public Object getField(Token key) {
        if (fields.containsKey(key.value)) {
            return fields.get(key.value);
        }
        throw Err.err("Error: Key doesn't exist", key);
    }

    public String formattedView(int indent) {
        final StringBuilder sb = new StringBuilder();
        sb.append("\t".repeat(indent));
        if (fields.size() > 1) {
            sb.append("{\n");
            fields.forEach((k, v) -> {
                sb.append("\t".repeat(indent + 1));
                if (v instanceof NObjectInstance sp) {
                    sb.append(String.format("%s : %s,\n", k.toString(), sp.formattedView(indent + 1)));
                } else {
                    sb.append(String.format("%s : %s,\n", k.toString(), v.toString()));
                }
            });
            sb.append("\t".repeat(indent));
            sb.append("}");
        } else {
            sb.append("{");
            fields.forEach((k, v) -> {
                sb.append(String.format(" %s : %s ", k.toString(), v.toString()));
            });
            sb.append("}");
        }
        return sb.toString();
    }

}
